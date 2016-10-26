package com.nhb.common.workflow.concurrent.route;

import com.lmax.disruptor.RingBuffer;
import com.nhb.common.workflow.JobContext;
import com.nhb.common.workflow.async.TaskFuture;
import com.nhb.common.workflow.holder.TaskHolder;
import com.nhb.common.workflow.holder.WorkProcessorGroupHolder;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
class RoutingEvent {

	private RingBuffer<RoutingEvent> ringBuffer;

	private TaskHolder taskHolder;

	private JobContext context;

	private TaskFuture jobDoneFuture;

	private WorkProcessorGroupHolder workProcessorGroupHolder;

	private Throwable failedCause;

	public void clear() {
		this.setContext(null);
		this.setJobDoneFuture(null);
		this.setFailedCause(null);
	}
}