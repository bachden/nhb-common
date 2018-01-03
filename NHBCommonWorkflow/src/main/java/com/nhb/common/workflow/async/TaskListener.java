package com.nhb.common.workflow.async;

import com.nhb.common.workflow.JobContext;

public interface TaskListener {

	@SuppressWarnings("unchecked")
	default <T extends JobContext> T castContext(JobContext context) {
		return (T) context;
	}

	void onSuccess(JobContext context);

	void onFailure(JobContext context, Throwable cause);
}
