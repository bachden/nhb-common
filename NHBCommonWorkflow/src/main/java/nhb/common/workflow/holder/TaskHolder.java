package nhb.common.workflow.holder;

import java.util.Set;

import nhb.common.Loggable;
import nhb.common.workflow.Task;
import nhb.common.workflow.async.TaskExecuteOptions;

public interface TaskHolder extends Loggable {

	void addTask(Task... tasks);

	default void addTask(Task task, TaskExecuteOptions options) {
		if (task != null) {
			this.addTask(task);
			this.setTaskExecuteOptions(task.getName(), options);
		}
	}

	<T extends Task> T getTask(String taskName);

	Task removeTask(String taskName);

	void setTaskExecuteOptions(String taskName, TaskExecuteOptions options);

	TaskExecuteOptions removeTaskExecuteOptions(String taskName);

	TaskExecuteOptions getTaskExecuteOptions(String taskName);

	boolean containsTask(String taskName);

	Set<String> getTaskList();
}
