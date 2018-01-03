package com.nhb.common.workflow.concurrent.route;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.nhb.common.Loggable;
import com.nhb.common.workflow.JobContext;
import com.nhb.common.workflow.Task;
import com.nhb.common.workflow.async.CancelDispatcher;
import com.nhb.common.workflow.async.TaskExecuteOptions;
import com.nhb.common.workflow.async.TaskFuture;
import com.nhb.common.workflow.async.TaskListener;
import com.nhb.common.workflow.concurrent.WorkProcessorGroup;
import com.nhb.common.workflow.exception.TaskNotExecutedException;
import com.nhb.common.workflow.holder.TaskHolder;
import com.nhb.common.workflow.holder.WorkProcessorGroupHolder;
import com.nhb.common.workflow.statemachine.State;

class RoutingHandler implements WorkHandler<RoutingEvent>, Loggable {

	public static void continueAsyncProcess(RingBuffer<RoutingEvent> ringBuffer, JobContext jobContext,
			TaskFuture jobDoneFuture, TaskHolder taskHolder, WorkProcessorGroupHolder workProcessorGroupHolder) {
		long sequence = ringBuffer.next();
		try {
			RoutingEvent event = ringBuffer.get(sequence);
			event.clear();
			event.setRingBuffer(ringBuffer);
			event.setContext(jobContext);
			event.setJobDoneFuture(jobDoneFuture);
			event.setTaskHolder(taskHolder);
			event.setWorkProcessorGroupHolder(workProcessorGroupHolder);
		} finally {
			ringBuffer.publish(sequence);
		}
	}

	public static void endAsyncWithSuccess(RingBuffer<RoutingEvent> ringBuffer, JobContext jobContext,
			TaskFuture jobDoneFuture) {
		long sequence = ringBuffer.next();
		try {
			RoutingEvent event = ringBuffer.get(sequence);
			event.clear();
			event.setRingBuffer(ringBuffer);
			event.setContext(jobContext);
			event.setJobDoneFuture(jobDoneFuture);
		} finally {
			ringBuffer.publish(sequence);
		}
	}

	public static void endAsyncWithFailedCause(RingBuffer<RoutingEvent> ringBuffer, JobContext jobContext,
			TaskFuture jobDoneFuture, Throwable failedCause) {
		long sequence = ringBuffer.next();
		try {
			RoutingEvent event = ringBuffer.get(sequence);
			event.clear();
			event.setRingBuffer(ringBuffer);
			event.setContext(jobContext);
			event.setJobDoneFuture(jobDoneFuture);
			event.setFailedCause(failedCause);
		} finally {
			ringBuffer.publish(sequence);
		}
	}

	@Override
	public void onEvent(RoutingEvent event) throws Exception {

		TaskFuture jobDoneFuture = event.getJobDoneFuture();
		Throwable exception = event.getFailedCause();

		if (exception != null) {
			if (jobDoneFuture != null) {
				jobDoneFuture.dispatchFailure(exception);
			} else {
				getLogger().error("Error while trying to execute job context", exception);
			}
		} else {
			JobContext jobContext = event.getContext();
			if (jobContext == null) {
				throw new NullPointerException("Cannot execute null job context");
			} else if (jobContext.getStateMachine().isFinal()) {
				if (jobDoneFuture != null) {
					jobDoneFuture.dispatchSuccess();
				}
			} else {
				
				State state = jobContext.getStateMachine().getCurrentState();
				if (state == null) {
					exception = new NullPointerException("Current state cannot be null");
				} else {
					String taskName = state.getName();
					TaskHolder taskHolder = event.getTaskHolder();
					Task task = taskHolder.getTask(taskName);
					if (task == null) {
						exception = new RuntimeException(
								"Task not found for name '" + taskName + "', job: " + jobContext.getJobName());
					} else {
						// System.out.println("Process task " + task.getName() +
						// ", job id: " + jobContext.getId());
						WorkProcessorGroupHolder workProcessorGroupHolder = event.getWorkProcessorGroupHolder();
						WorkProcessorGroup workProcessorGroup = null;

						TaskExecuteOptions options = taskHolder.getTaskExecuteOptions(taskName);
						final boolean isAsyncTask = options == null ? false : options.isAsync();

						TaskListener taskDoneListener = new TaskListener() {

							@Override
							public void onSuccess(JobContext context) {
								// if state machine is in final state,
								// dispatch job done
								// getLogger().debug(
								// "Task " + task.getName() + " complete
								// success, job id: " + jobContext.getId());
								if (context instanceof CancelDispatcher) {
									context.clear();
								}
								if (context.getStateMachine().isFinal()) {
									if (isAsyncTask) {
										if (event.getRingBuffer() == null) {
											System.err.println("something went wrong, ring buffer is null, taskName: "
													+ task.getName());
											new Exception().printStackTrace();
											System.exit(0);
											return;
										}
										endAsyncWithSuccess(event.getRingBuffer(), jobContext, jobDoneFuture);
									} else {
										RoutingEvent continuousEvent = new RoutingEvent();
										continuousEvent.setJobDoneFuture(jobDoneFuture);
										continuousEvent.setContext(jobContext);
										try {
											onEvent(continuousEvent);
										} catch (Exception e) {
											if (jobDoneFuture != null) {
												jobDoneFuture.dispatchFailure(e);
											} else {
												getLogger().error("Error while handle next task");
											}
										}
									}
								} else {
									if (isAsyncTask) {
										// else, continue execute task...
										// System.out.println("Continue async
										// task : " + task.getName());
										continueAsyncProcess(event.getRingBuffer(), jobContext, jobDoneFuture,
												taskHolder, workProcessorGroupHolder);
									} else {
										// getLogger()
										// .debug("Continue process synchronous,
										// job id: " + jobContext.getId());
										RoutingEvent continuousEvent = new RoutingEvent();
										continuousEvent.setRingBuffer(event.getRingBuffer());
										continuousEvent.setJobDoneFuture(jobDoneFuture);
										continuousEvent.setContext(jobContext);
										continuousEvent.setTaskHolder(taskHolder);
										continuousEvent.setWorkProcessorGroupHolder(workProcessorGroupHolder);
										try {
											onEvent(continuousEvent);
										} catch (Exception e) {
											if (jobDoneFuture != null) {
												jobDoneFuture.dispatchFailure(e);
											} else {
												getLogger().error("Error while handle next task");
											}
										}
									}
								}
							}

							@Override
							public void onFailure(JobContext context, Throwable failedCause) {
								if (isAsyncTask) {
									endAsyncWithFailedCause(event.getRingBuffer(), jobContext, jobDoneFuture,
											failedCause);
								} else {
									RoutingEvent continuousEvent = new RoutingEvent();
									continuousEvent.setJobDoneFuture(jobDoneFuture);
									continuousEvent.setContext(jobContext);
									continuousEvent.setFailedCause(failedCause);
									try {
										onEvent(continuousEvent);
									} catch (Exception e) {
										if (jobDoneFuture != null) {
											jobDoneFuture.dispatchFailure(e);
										} else {
											getLogger().error("Error while handle next task");
										}
									}
								}
							}
						};

						TaskFuture taskFuture = null;
						if (isAsyncTask) {
							workProcessorGroup = options.getWorkProcessorGroupName() == null ? null
									: workProcessorGroupHolder
											.getWorkProcessorGroup(options.getWorkProcessorGroupName());
							if (workProcessorGroup == null && options.getWorkProcessorGroupName() != null) {
								getLogger().warn(
										"The worker pool named '{}' config to execute on state '{}', job '{}' was not found, the task will be executed on routing bus",
										options.getWorkProcessorGroupName(), taskName, jobContext.getJobName(),
										new NullPointerException());
							}
						}

						if (isAsyncTask && workProcessorGroup != null) {
							if (!workProcessorGroup.isRunning()) {
								workProcessorGroup.start();
							}
							taskFuture = workProcessorGroup.execute(jobContext, task);
							if (taskFuture != null) {
								taskFuture.addListener(taskDoneListener);
							} else if (exception == null) {
								exception = new TaskNotExecutedException(
										"Cannot get task execution future while processing task " + state);
							}
						} else {
							// if the workProcessorGroup == null, trying to
							// execute on routing bus
							try {
								// getLogger().debug("Execute task " +
								// task.getName() + " synchronous");
								taskFuture = task.execute(jobContext);
								// getLogger().debug("task execute done...");
								if (taskFuture == null) {
									taskDoneListener.onSuccess(jobContext);
								} else {
									taskFuture.addListener(taskDoneListener);
								}
							} catch (Exception e) {
								taskDoneListener.onFailure(jobContext, e);
							}
						}
					}
				}

				if (exception != null) {
					if (jobDoneFuture != null) {
						jobDoneFuture.dispatchFailure(exception);
					} else {
						getLogger().error("Error while trying to execute job context", exception);
					}
				}
			}
		}
	}

}
