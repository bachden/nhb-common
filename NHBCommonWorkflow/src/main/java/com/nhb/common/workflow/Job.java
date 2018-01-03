package com.nhb.common.workflow;

import com.nhb.common.Loggable;
import com.nhb.common.workflow.async.TaskFuture;
import com.nhb.common.workflow.holder.EnvironmentVariableHolder;
import com.nhb.common.workflow.holder.TaskHolder;

public interface Job extends EnvironmentVariableHolder, TaskHolder, Loggable {

	String getName();

	TaskFuture execute(JobContext context);
}
