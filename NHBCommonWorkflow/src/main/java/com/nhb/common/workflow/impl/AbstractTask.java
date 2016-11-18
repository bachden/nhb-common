package com.nhb.common.workflow.impl;

import com.nhb.common.Loggable;
import com.nhb.common.workflow.JobContext;
import com.nhb.common.workflow.Task;

import lombok.Getter;

public abstract class AbstractTask implements Loggable, Task {

	@Getter
	private final String name;

	public AbstractTask(String name) {
		this.name = name;
	}

	@SuppressWarnings("unchecked")
	protected <T extends JobContext> T castJobContext(JobContext context) {
		return (T) context;
	}
}
