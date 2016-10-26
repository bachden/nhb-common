package com.nhb.common.workflow.async;

import java.util.concurrent.TimeUnit;

import com.nhb.common.workflow.JobContext;

public interface TaskFuture {

	/**
	 * Add listener to handle complete event
	 * 
	 * @param listener
	 *            the object to handle complete event, success or failure
	 * @return listening id, use to remove listener if necessary
	 */
	void addListener(TaskListener listener);

	/**
	 * Set timeout for this future, apply TimeoutException to all complete
	 * listener's onFailure and throw that exception to thread which waiting in
	 * sync() method calling
	 * 
	 * @param timeout
	 *            the timeout value
	 * @param timeUnit
	 *            the time unit of the timeout value
	 */
	void setTimeout(long timeout, TimeUnit timeUnit);

	void cancel();

	JobContext getContext();

	void dispatchSuccess();

	void dispatchFailure(Throwable failureCause);
}
