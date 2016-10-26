package com.nhb.common.workflow.concurrent;

import com.nhb.common.workflow.JobContext;
import com.nhb.common.workflow.Task;
import com.nhb.common.workflow.async.TaskFuture;

public interface WorkProcessorGroup {

	String getName();

	void start();

	void shutdown();

	boolean isRunning();

	TaskFuture execute(JobContext context, Task task);
}
