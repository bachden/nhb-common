package com.nhb.common.async;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import com.nhb.common.async.exception.ExecutionCancelledException;
import com.nhb.common.async.executor.DisruptorAsyncTaskExecutor;
import com.nhb.eventdriven.impl.BaseEventDispatcher;

import lombok.Getter;
import lombok.Setter;

public class BaseRPCFuture<V> extends BaseEventDispatcher implements RPCFuture<V>, CompletableFuture<V> {

	private static final ScheduledExecutorService monitoringExecutorService = Executors.newScheduledThreadPool(1);
	private static DisruptorAsyncTaskExecutor executor = DisruptorAsyncTaskExecutor.createSingleProducerExecutor(2048,
			4, "RPCFuture Timeout Callback Thread #%d");

	static {
		executor.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				monitoringExecutorService.shutdown();
				try {
					if (monitoringExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
						monitoringExecutorService.shutdownNow();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					executor.shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private volatile V value;

	@Getter
	@Setter // setter should be removed...
	private Future<?> cancelFuture;

	@Getter
	private volatile Throwable failedCause;

	@Getter
	private volatile Callback<V> callback;

	private Future<?> monitorTimeoutFuture;
	private final CountDownLatch doneSignal = new CountDownLatch(1);

	private volatile boolean complete = false;
	private final AtomicBoolean done = new AtomicBoolean(false);
	private final AtomicBoolean cancelled = new AtomicBoolean(false);
	private final AtomicBoolean hasCallback = new AtomicBoolean(false);
	private final AtomicBoolean timeoutFlag = new AtomicBoolean(false);

	private void waitForDoneProgress() {
		while (this.done.get() && !this.complete) {
			LockSupport.parkNanos(10);
		}
	}

	@Override
	public boolean isDone() {
		this.waitForDoneProgress();
		return this.done.get();
	}

	@Override
	public boolean isCancelled() {
		this.waitForDoneProgress();
		return this.cancelled.get();
	}

	private void doComplete(V value) {
		this.value = value;
		if (monitorTimeoutFuture != null) {
			this.monitorTimeoutFuture.cancel(true);
			this.monitorTimeoutFuture = null;
		}

		if (cancelFuture != null) {
			this.cancelFuture = null;
		}

		this.doneSignal.countDown();
		if (this.callback != null) {
			try {
				this.callback.apply(this.value);
			} catch (Exception e) {
				getLogger().error("Error while execute callback", e);
			}
		}

		this.complete = true;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (this.done.compareAndSet(false, true) && this.cancelled.compareAndSet(false, true)) {
			if (this.cancelFuture != null && !this.cancelFuture.isCancelled()) {
				this.cancelFuture.cancel(mayInterruptIfRunning);
			}
			if (this.failedCause == null) {
				this.failedCause = new ExecutionCancelledException();
			}
			doComplete(null);
			return true;
		}
		return false;
	}

	@Override
	public void setAndDone(V value) {
		if (this.done.compareAndSet(false, true)) {
			this.doComplete(value);
		} else {
			throw new IllegalStateException("Future were done or in-done-progress");
		}
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		if (!this.isDone()) {
			this.doneSignal.await();
			if (timeoutFlag.get()) {
				if (this.failedCause == null) {
					synchronized (this) {
						if (this.failedCause == null) {
							this.failedCause = new TimeoutException();
						}
					}
				}
				if (this.isCancelled()) {
					return null;
				}
			}
		}
		return this.value;
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		this.setTimeout(timeout, unit);
		V result = this.get();
		if (timeoutFlag.get()) {
			if (this.getFailedCause() instanceof TimeoutException) {
				throw (TimeoutException) this.getFailedCause();
			} else {
				throw new TimeoutException();
			}
		}
		if (this.isCancelled()) {
			return null;
		}
		return result;
	}

	@Override
	public final void setCallback(Callback<V> callback) {
		if (callback != null && this.hasCallback.compareAndSet(false, true)) {
			if (this.isDone()) {
				callback.apply(this.value);
			}
			this.callback = callback;
		}
	}

	@Override
	@Deprecated
	public void setFailedCause(Throwable cause) {
		if (this.isDone() || this.isCancelled()) {
			throw new IllegalStateException("Cannot set failedCause for done or cancelled future");
		}
		this.failedCause = cause;
	}

	@Override
	public void setFailedAndDone(Throwable cause) {
		if (this.cancel(false)) {
			this.failedCause = cause;
		}
	}

	@Override
	public void setTimeout(long timeout, TimeUnit unit) {
		if (this.isDone() || this.isCancelled()) {
			return;
		}
		if (this.monitorTimeoutFuture == null) {
			synchronized (this) {
				if (this.monitorTimeoutFuture == null) {
					final TimeoutException exceptionTobeThrown = new TimeoutException();
					this.monitorTimeoutFuture = monitoringExecutorService.schedule(new Runnable() {
						@Override
						public void run() {
							if (timeoutFlag.compareAndSet(false, true)) {
								executor.execute(() -> {
									setFailedAndDone(exceptionTobeThrown);
								});
							}
						}
					}, timeout, unit);
				}
			}
		} else {
			throw new IllegalStateException("Timeout has been set");
		}
	}

}
