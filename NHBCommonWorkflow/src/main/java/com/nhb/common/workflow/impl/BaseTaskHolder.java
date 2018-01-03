package com.nhb.common.workflow.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.nhb.common.workflow.Task;
import com.nhb.common.workflow.async.TaskExecuteOptions;
import com.nhb.common.workflow.holder.TaskHolder;

public class BaseTaskHolder implements TaskHolder {

	private final Map<String, Task> tasks = new ConcurrentHashMap<>();
	private final Map<String, TaskExecuteOptions> taskExecuteOptions = new ConcurrentHashMap<>();

	@Override
	public void addTask(Task... tasks) {
		if (tasks != null && tasks.length > 0) {
			for (Task task : tasks) {
				if (task != null) {
					this.tasks.put(task.getName(), task);
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Task> T getTask(String taskName) {
		return (T) this.tasks.get(taskName);
	}

	@Override
	public Task removeTask(String taskName) {
		return this.tasks.remove(taskName);
	}

	@Override
	public Set<String> getTaskList() {
		return new HashSet<>(this.tasks.keySet());
	}

	@Override
	public boolean containsTask(String taskName) {
		return this.tasks.containsKey(taskName);
	}

	@Override
	public void setTaskExecuteOptions(String taskName, TaskExecuteOptions options) {
		this.taskExecuteOptions.put(taskName, options);
	}

	@Override
	public TaskExecuteOptions removeTaskExecuteOptions(String taskName) {
		return this.taskExecuteOptions.remove(taskName);
	}

	@Override
	public TaskExecuteOptions getTaskExecuteOptions(String taskName) {
		return this.taskExecuteOptions.get(taskName);
	}

}
