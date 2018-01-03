package com.nhb.common.workflow;

import com.nhb.common.workflow.async.TaskFuture;

public interface Task {

	String getName();

	@SuppressWarnings("unchecked")
	default <T extends JobContext> T castContext(JobContext context) {
		return (T) context;
	}

	TaskFuture execute(JobContext context);
}
