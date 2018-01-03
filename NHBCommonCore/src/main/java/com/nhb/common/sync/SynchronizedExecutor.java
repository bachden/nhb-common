package com.nhb.common.sync;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SynchronizedExecutor<ReturnType> {

	private final ExecutorService executorService = Executors.newFixedThreadPool(1);

	private Future<ReturnType> currentFuture = null;

	/**
	 * Be careful, when a task is executing, every call will be got the same
	 * future and the result will be returned from the first executed task
	 * </br>
	 * </br>
	 * Until the executed task complete, the future will be reset to null
	 * 
	 * @param task
	 *            to be executed
	 * @return the future to get the result
	 */
	public synchronized Future<ReturnType> execute(final Callable<ReturnType> task) {
		if (currentFuture == null && task != null) {
			this.currentFuture = this.executorService.submit(new Callable<ReturnType>() {

				@Override
				public ReturnType call() throws Exception {
					try {
						return task.call();
					} finally {
						currentFuture = null;
					}
				}
			});
		}
		return currentFuture;
	}
}
