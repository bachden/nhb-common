package com.nhb.common.workflow.disruptor;

import com.lmax.disruptor.WorkHandler;
import com.nhb.common.Loggable;
import com.nhb.common.workflow.async.TaskFuture;

class TaskHandler implements Loggable, WorkHandler<TaskProcessingEvent> {

	static final TaskHandler newInstance() {
		return new TaskHandler();
	}

	@Override
	public void onEvent(TaskProcessingEvent event) throws Exception {
		try {
			TaskFuture future = event.getTask().execute(event.getJobContext());
			if (future != null) {
				if (event.getListener() != null) {
					future.addListener(event.getListener());
				}
			} else {
				if (event.getListener() != null) {
					event.getListener().onSuccess(event.getJobContext());
				}
			}
		} catch (Exception cause) {
			if (event.getListener() != null) {
				event.getListener().onFailure(event.getJobContext(), cause);
			} else {
				getLogger().error("TaskProcessingEvent failure listener not found, error while handling task: "
						+ event.getTask().getName(), cause);
			}
		}
	}

}
