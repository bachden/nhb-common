package com.nhb.common.workflow.concurrent.route;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkerPool;
import com.nhb.common.Loggable;
import com.nhb.common.workflow.JobContext;
import com.nhb.common.workflow.async.impl.BaseTaskFuture;
import com.nhb.common.workflow.holder.TaskHolder;
import com.nhb.common.workflow.holder.WorkProcessorGroupHolder;

public class RoutingBus implements ExceptionHandler<RoutingEvent>, Loggable {

	private ExecutorService executor;
	private RingBuffer<RoutingEvent> ringBuffer;
	private TaskHolder taskHolder;
	private WorkProcessorGroupHolder workProcessorGroupHolder;

	private WorkerPool<RoutingEvent> workerPool;

	private String threadNamePattern;

	public RoutingBus(int nThreads, int ringBufferSize, String threadNamePattern) {

		this.threadNamePattern = threadNamePattern;
		this.ringBuffer = RingBuffer.createMultiProducer(new EventFactory<RoutingEvent>() {

			@Override
			public RoutingEvent newInstance() {
				return new RoutingEvent();
			}

		}, ringBufferSize);

		RoutingHandler[] handlers = new RoutingHandler[nThreads];
		for (int i = 0; i < nThreads; i++) {
			handlers[i] = new RoutingHandler();
		}

		this.workerPool = new WorkerPool<RoutingEvent>(this.ringBuffer, this.ringBuffer.newBarrier(), this, handlers);
	}

	public RoutingBus(int nThreads, int ringBufferSize, String threadNamePattern, TaskHolder taskHolder,
			WorkProcessorGroupHolder workProcessorGroupHolder) {

		this(nThreads, ringBufferSize, threadNamePattern);
		this.taskHolder = taskHolder;
		this.workProcessorGroupHolder = workProcessorGroupHolder;
	}

	public void start() {
		if (!this.workerPool.isRunning()) {
			synchronized (this) {
				if (!this.workerPool.isRunning()) {
					this.executor = Executors.newCachedThreadPool(
							new ThreadFactoryBuilder().setNameFormat(this.threadNamePattern).build());
					workerPool.start(this.executor);
				}
			}
		}
	}

	public void shutdown() {
		if (this.workerPool.isRunning()) {
			synchronized (this) {
				if (this.workerPool.isRunning()) {
					workerPool.drainAndHalt();
				}
				this.executor.shutdown();
				try {
					if (!this.executor.awaitTermination(3, TimeUnit.SECONDS)) {
						this.executor.shutdownNow();
					}
				} catch (InterruptedException e) {
					getLogger().error("Cannot shutdown executor", e);
				}
			}
		}
	}

	public void submit(JobContext jobContext, final BaseTaskFuture jobDoneFuture) {
		RoutingHandler.continueAsyncProcess(this.ringBuffer, jobContext, jobDoneFuture, taskHolder,
				workProcessorGroupHolder);
	}

	@Override
	public void handleEventException(Throwable failedCause, long sequence, RoutingEvent event) {
		RoutingHandler.endAsyncWithFailedCause(ringBuffer, event.getContext(), event.getJobDoneFuture(), failedCause);
	}

	@Override
	public void handleOnShutdownException(Throwable failedCause) {
		getLogger().error("Error while shutdown RoutingBus");
	}

	@Override
	public void handleOnStartException(Throwable failedCause) {
		getLogger().error("Error while start RoutingBus");
	}

	public void setTaskHolder(TaskHolder taskHolder) {
		this.taskHolder = taskHolder;
	}

	public void setWorkProcessorGroupHolder(WorkProcessorGroupHolder processorGroupHolder) {
		this.workProcessorGroupHolder = processorGroupHolder;
	}
}
