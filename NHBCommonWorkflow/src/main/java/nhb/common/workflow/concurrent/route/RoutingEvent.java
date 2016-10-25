package nhb.common.workflow.concurrent.route;

import com.lmax.disruptor.RingBuffer;

import lombok.Getter;
import lombok.Setter;
import nhb.common.workflow.JobContext;
import nhb.common.workflow.async.TaskFuture;
import nhb.common.workflow.holder.TaskHolder;
import nhb.common.workflow.holder.WorkProcessorGroupHolder;

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