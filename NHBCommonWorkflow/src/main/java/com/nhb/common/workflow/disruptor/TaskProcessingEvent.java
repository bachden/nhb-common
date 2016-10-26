package com.nhb.common.workflow.disruptor;

import com.lmax.disruptor.EventFactory;
import com.nhb.common.workflow.JobContext;
import com.nhb.common.workflow.Task;
import com.nhb.common.workflow.async.TaskListener;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class TaskProcessingEvent {

	private Task task;
	private JobContext jobContext;
	private TaskListener listener;

	private TaskProcessingEvent() {
		
	}

	public void copy(TaskProcessingEvent other) {
		if (other == null) {
			throw new RuntimeException("Cannot copy data from null object");
		}
		this.fill(other.getTask(), other.getJobContext(), other.getListener());
	}

	public void fill(Task task, JobContext jobContext, TaskListener listener) {
		this.setTask(task);
		this.setJobContext(jobContext);
		this.setListener(listener);
	}

	private static final EventFactory<TaskProcessingEvent> _factory = new EventFactory<TaskProcessingEvent>() {

		@Override
		public TaskProcessingEvent newInstance() {
			return new TaskProcessingEvent();
		}
	};

	public static final EventFactory<TaskProcessingEvent> factory() {
		return _factory;
	}
}
