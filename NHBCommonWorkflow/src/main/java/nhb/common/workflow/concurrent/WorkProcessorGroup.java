package nhb.common.workflow.concurrent;

import nhb.common.workflow.JobContext;
import nhb.common.workflow.Task;
import nhb.common.workflow.async.TaskFuture;

public interface WorkProcessorGroup {

	String getName();

	void start();

	void shutdown();

	boolean isRunning();

	TaskFuture execute(JobContext context, Task task);
}
