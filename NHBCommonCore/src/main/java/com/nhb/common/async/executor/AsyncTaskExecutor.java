package com.nhb.common.async.executor;

public interface AsyncTaskExecutor {

	void start();

	void shutdown() throws Exception;

	void execute(Runnable runnable);
}
