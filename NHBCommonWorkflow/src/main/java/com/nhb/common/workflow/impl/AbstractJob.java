package com.nhb.common.workflow.impl;

import java.util.Set;

import com.nhb.common.workflow.Job;
import com.nhb.common.workflow.Task;
import com.nhb.common.workflow.async.TaskExecuteOptions;
import com.nhb.common.workflow.holder.EnvironmentVariableHolder;
import com.nhb.common.workflow.holder.TaskHolder;
import com.nhb.common.workflow.holder.WorkProcessorGroupHolder;

import lombok.Getter;

public abstract class AbstractJob implements Job {

	@Getter
	private final String name;

	private final WorkProcessorGroupHolder workProcessorGroupHolder;
	private final TaskHolder taskHolder = new BaseTaskHolder();
	private final EnvironmentVariableHolder environmentVariableHolder = new BaseEnvironmentVariableHolder();

	protected AbstractJob(String name, WorkProcessorGroupHolder workProcessorGroupHolder) {
		if (workProcessorGroupHolder == null) {
			throw new NullPointerException("WorkProcessorGroupHolder cannot be null");
		}
		this.name = name;
		this.workProcessorGroupHolder = workProcessorGroupHolder;
	}

	@Override
	public void addEnvironmentVariable(String varName, Object value) {
		environmentVariableHolder.addEnvironmentVariable(varName, value);
	}

	@Override
	public Object removeEnvironmentVariable(String varName) {
		return environmentVariableHolder.removeEnvironmentVariable(varName);
	}

	@Override
	public <T> T getEnvironmentVariable(String varName) {
		return environmentVariableHolder.getEnvironmentVariable(varName);
	}

	@Override
	public boolean containsEnvironmentVariable(String varName) {
		return environmentVariableHolder.containsEnvironmentVariable(varName);
	}

	@Override
	public void addTask(Task... tasks) {
		taskHolder.addTask(tasks);
	}

	@Override
	public <T extends Task> T getTask(String taskName) {
		return taskHolder.getTask(taskName);
	}

	@Override
	public Task removeTask(String taskName) {
		return taskHolder.removeTask(taskName);
	}

	@Override
	public Set<String> getTaskList() {
		return taskHolder.getTaskList();
	}

	@Override
	public boolean containsTask(String taskName) {
		return taskHolder.containsTask(taskName);
	}

	public WorkProcessorGroupHolder getWorkProcessorGroupHolder() {
		return workProcessorGroupHolder;
	}

	@Override
	public void setTaskExecuteOptions(String taskName, TaskExecuteOptions options) {
		this.taskHolder.setTaskExecuteOptions(taskName, options);
	}

	@Override
	public TaskExecuteOptions removeTaskExecuteOptions(String taskName) {
		return this.taskHolder.removeTaskExecuteOptions(taskName);
	}

	@Override
	public TaskExecuteOptions getTaskExecuteOptions(String taskName) {
		return this.taskHolder.getTaskExecuteOptions(taskName);
	}
}
