package com.nhb.common.transaction.impl;

import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuObject;
import com.nhb.common.transaction.DistributedTransactionJob;
import com.nhb.common.transaction.DistributedTransactionTask;

public abstract class AbstractDistributedTransactionTask extends BaseLoggable implements DistributedTransactionTask {

	private boolean successful = false;
	private PuObject failureDetails;
	private DistributedTransactionJob job;

	@Override
	public boolean isSuccessful() {
		return this.successful;
	}

	protected void setSuccessful(boolean value) {
		this.successful = value;
	}

	public PuObject getFailureDetails() {
		return failureDetails;
	}

	protected void setFailureDetails(PuObject failureDetails) {
		this.failureDetails = failureDetails;
	}

	@Override
	public DistributedTransactionJob getJob() {
		return job;
	}

	public void setJob(DistributedTransactionJob job) {
		this.job = job;
	}

}
