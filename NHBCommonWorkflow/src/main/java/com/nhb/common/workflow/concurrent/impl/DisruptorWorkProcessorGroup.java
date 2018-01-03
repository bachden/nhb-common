package com.nhb.common.workflow.concurrent.impl;

import com.nhb.common.workflow.JobContext;
import com.nhb.common.workflow.Task;
import com.nhb.common.workflow.async.TaskFuture;
import com.nhb.common.workflow.async.TaskListener;
import com.nhb.common.workflow.async.impl.BaseTaskFuture;
import com.nhb.common.workflow.concurrent.WorkProcessorGroup;
import com.nhb.common.workflow.disruptor.TaskProcessingWorkerPool;

import lombok.Getter;

public class DisruptorWorkProcessorGroup implements WorkProcessorGroup {

	@Getter
	private final String name;

	private TaskProcessingWorkerPool workerPool;

	public DisruptorWorkProcessorGroup(String name, int workerPoolSize, int ringBufferSize) {
		this.name = name;
		this.workerPool = new TaskProcessingWorkerPool(workerPoolSize, ringBufferSize, this.name + " processor #%d");
	}

	public void start() {
		this.workerPool.start();
	}

	public void shutdown() {
		this.workerPool.shutdown();
	}

	@Override
	public boolean isRunning() {
		return this.workerPool.isRunning();
	}

	@Override
	public TaskFuture execute(final JobContext context, final Task task) {
		if (context != null && task != null) {
			final BaseTaskFuture future = new BaseTaskFuture(context);
			this.workerPool.execute(task, context, new TaskListener() {

				@Override
				public void onSuccess(final JobContext context) {
					future.dispatchSuccess();
				}

				@Override
				public void onFailure(final JobContext context, final Throwable cause) {
					future.dispatchFailure(cause);
				}
			});
			return future;
		}
		return null;
	}

}
