package com.nhb.common.workflow.impl;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.nhb.common.Loggable;
import com.nhb.common.workflow.Job;
import com.nhb.common.workflow.JobAware;
import com.nhb.common.workflow.JobContext;
import com.nhb.common.workflow.async.CancelDispatcher;
import com.nhb.common.workflow.async.CancelListener;
import com.nhb.common.workflow.statemachine.StateMachine;

import lombok.Getter;
import lombok.Setter;

public class BasicJobContext implements JobContext, JobAware, CancelDispatcher, Loggable {

	@Setter
	@Getter
	private int id;

	@Setter
	@Getter
	private Job parentJob;

	@Setter
	@Getter
	private StateMachine stateMachine;

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
