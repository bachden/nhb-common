package com.nhb.common.async;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.nhb.eventdriven.impl.BaseEventDispatcher;

public class BaseRPCFuture<V> extends BaseEventDispatcher implements RPCFuture<V> {

	private static ScheduledExecutorService monitoringExecutorService = Executors.newScheduledThreadPool(4);

	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				monitoringExecutorService.shutdown();
				try {
					if (monitoringExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
						monitoringExecutorService.shutdownNow();
					}
				} catch (Exception ex) {

				}
			}
		});
	}

	private Throwable failedCause;

	private volatile Callback<V> callable;
	private volatile V value;
	private volatile boolean done = false;
	private volatile boolean cancelled = false;

	private Future<?> monitorFuture;
	private Future<?> cancelFuture;

	private boolean timeoutFlag = false;

	private CountDownLatch doneSignal;

	public BaseRPCFuture() {
		this.doneSignal = new CountDownLatch(1);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (this.cancelled) {
			return true;
		}
		if (this.isDone()) {
			return false;
		}
		try {
			if (this.cancelFuture != null) {
				this.cancelled = this.cancelFuture.cancel(mayInterruptIfRunning);
			} else {
				this.cancelled = true;
			}
			return this.cancelled;
		} finally {
			if (this.monitorFuture != null) {
				this.monitorFuture.cancel(true);
				this.monitorFuture = null;
			}
			this.cancelFuture = null;
			this.doneSignal.countDown();
			if (this.getCallback() != null) {
				this.getCallback().apply(null);
			}
		}
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	public void done() {
		if (this.done || this.cancelled || this.timeoutFlag) {
			return;
		}
		// System.out.println("operator is done, wake up all waiting
		// threads...");
		this.done = true;
		if (monitorFuture != null) {
			this.monitorFuture.cancel(false);
			this.monitorFuture = null;
		}
		if (cancelFuture != null) {
			this.cancelFuture = null;
		}
		this.doneSignal.countDown();
		if (this.callable != null) {
			try {
				this.callable.apply(this.value);
			} catch (Exception e) {
				getLogger().error("Error while execute callback", e);
			}
		}
	}

	@Override
	public boolean isDone() {
		return this.done;
	}

	public void set(V value) {
		this.value = value;
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		if (this.done == false) {
			this.doneSignal.await();
			if (timeoutFlag || cancelled) {
				return null;
			}
		}
		return this.value;
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		this.setTimeout(timeout, unit);
		V result = this.get();
		if (timeoutFlag) {
			if (this.getFailedCause() instanceof TimeoutException) {
				throw (TimeoutException) this.getFailedCause();
			} else {
				throw new TimeoutException();
			}
		}
		if (this.cancelled) {
			return null;
		}
		return result;
	}

	@Override
	public void setCallback(Callback<V> callable) {
		if (callable == this.callable) {
			return;
		}
		this.callable = callable;
		if (this.isDone() && this.callable != null) {
			this.callable.apply(this.value);
		}
	}

	@Override
	public Callback<V> getCallback() {
		return this.callable;
	}

	public Future<?> getCancelFuture() {
		return cancelFuture;
	}

	public void setCancelFuture(Future<?> cancelFuture) {
		this.cancelFuture = cancelFuture;
	}

	public Throwable getFailedCause() {
		return failedCause;
	}

	public void setFailedCause(Throwable failedCause) {
		this.failedCause = failedCause;
	}

	@Override
	public void setTimeout(long timeout, TimeUnit unit) {
		if (this.isDone()) {
			return;
		}
		if (this.monitorFuture == null) {
			synchronized (this) {
				if (this.monitorFuture == null) {
					final TimeoutException exceptionTobeThrown = new TimeoutException();
					this.monitorFuture = monitoringExecutorService.schedule(new Runnable() {
						@Override
						public void run() {
							if (monitorFuture == null) {
								return;
							}
							setFailedCause(exceptionTobeThrown);
							timeoutFlag = true;
							cancel(true);
						}
					}, timeout, unit);
				}
			}
		} else {
			throw new IllegalStateException("Timeout has been set");
		}
	}

}
