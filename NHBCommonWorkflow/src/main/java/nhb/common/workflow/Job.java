package nhb.common.workflow;

import nhb.common.Loggable;
import nhb.common.workflow.async.TaskFuture;
import nhb.common.workflow.holder.EnvironmentVariableHolder;
import nhb.common.workflow.holder.TaskHolder;

public interface Job extends EnvironmentVariableHolder, TaskHolder, Loggable {

	String getName();

	TaskFuture execute(JobContext context);
}
