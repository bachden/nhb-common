package com.nhb.common.transaction.impl;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuObject;
import com.nhb.common.transaction.DistributedTransactionJob;
import com.nhb.common.transaction.DistributedTransactionTask;

public abstract class AbstractDistributedTransactionJob extends BaseLoggable implements DistributedTransactionJob {

	private PuObject result;
	private PuObject failureDetails;
	private List<DistributedTransactionTask> tasks = new CopyOnWriteArrayList<>();

	@Override
	public List<DistributedTransactionTask> getTasks() {
		return this.tasks;
	}

	public void addTask(DistributedTransactionTask task) {
		this.getTasks().add(task);
		if (task instanceof AbstractDistributedTransactionTask) {
			((AbstractDistributedTransactionTask) task).setJob(this);
		}
	}

	public void addTasks(DistributedTransactionTask... tasks) {
		this.addTasks(Arrays.asList(tasks));
		for (DistributedTransactionTask task : tasks) {
			if (task instanceof AbstractDistributedTransactionTask) {
				((AbstractDistributedTransactionTask) task).setJob(this);
			}
		}
	}

	public void addTasks(List<DistributedTransactionTask> tasks) {
		this.getTasks().addAll(tasks);
		for (DistributedTransactionTask task : tasks) {
			if (task instanceof AbstractDistributedTransactionTask) {
				((AbstractDistributedTransactionTask) task).setJob(this);
			}
		}
	}

	@Override
	public void rollback() {
		for (int i = this.getTasks().size() - 1; i >= 0; i--) {
			if (this.getTasks().get(i).isSuccessful()) {
				this.getTasks().get(i).rollback();
			}
		}
	}

	@Override
	public boolean execute(final PuObject data) {
		for (DistributedTransactionTask task : this.getTasks()) {
			try {
				task.execute(data);
			} catch (Exception ex) {
				this.setFailureDetails(task.getFailureDetails());
				this.rollback();
				return false;
			}
		}
		this.onComplete();
		return true;
	}
	
	protected abstract void onComplete();

	@Override
	public PuObject getFailureDetails() {
		return failureDetails;
	}

	protected void setFailureDetails(PuObject failureDetails) {
		this.failureDetails = failureDetails;
	}

	public PuObject getResult() {
		return result;
	}

	protected void setResult(PuObject result) {
		this.result = result;
	}
}
