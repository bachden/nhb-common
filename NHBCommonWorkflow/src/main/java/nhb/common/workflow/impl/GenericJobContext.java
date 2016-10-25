package nhb.common.workflow.impl;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import nhb.common.Loggable;
import nhb.common.data.PuObject;
import nhb.common.workflow.Job;
import nhb.common.workflow.JobAware;
import nhb.common.workflow.JobContext;
import nhb.common.workflow.async.CancelDispatcher;
import nhb.common.workflow.async.CancelListener;
import nhb.common.workflow.statemachine.StateMachine;

@Builder
public class GenericJobContext implements JobContext, JobAware, CancelDispatcher, Loggable {

	@Setter
	@Getter
	private int id;

	@Setter
	@Getter
	private Job parentJob;

	@Setter
	@Getter
	private StateMachine stateMachine;

	@Setter
	@Getter
	private PuObject output;

	@Setter
	@Getter
	private PuObject input;

	private final Set<CancelListener> cancelListeners = new CopyOnWriteArraySet<>();

	@Override
	public void dispatchCancel() {
		for (CancelListener cancelListener : cancelListeners) {
			try {
				cancelListener.cancel();
			} catch (Exception e) {
				getLogger().error("Error while canceling", e);
			}
		}
	}

	@Override
	public void addCancelListener(CancelListener listener) {
		this.cancelListeners.add(listener);
	}

	@Override
	public void clear() {
		this.cancelListeners.clear();
	}
}
