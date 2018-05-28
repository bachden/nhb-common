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

	private final AtomicBoolean hasCallback = new AtomicBoolean(false);

	private volatile V value;
	private Throwable failedCause;
	private Future<?> sourceFuture;
	private volatile Callback<V> callback;

	private final CountDownLatch doneSignal;

	private final AtomicBoolean done = new AtomicBoolean(false);
	private final AtomicBoolean cancelled = new AtomicBoolean(false);

	private Future<?> monitorTimeoutFuture;
	private AtomicBoolean timeoutFlag = new AtomicBoolean(false);

	public BaseRPCFuture() {
		this.doneSignal = new CountDownLatch(1);
	}

	@Override
	public boolean isDone() {
		// prevent race condition
		while (this.isInDoneProcessing.get()) {
			LockSupport.parkNanos(10);
		}
		return this.done.get();
	}

	private final AtomicBoolean isInDoneProcessing = new AtomicBoolean(false);

	private void doComplete() {
		if (isInDoneProcessing.compareAndSet(false, true)) {
			if (monitorTimeoutFuture != null) {
				this.monitorTimeoutFuture.cancel(true);
				this.monitorTimeoutFuture = null;
			}
			if (sourceFuture != null) {
				this.sourceFuture = null;
			}
			this.doneSignal.countDown();
			if (this.callback != null) {
				try {
					this.callback.apply(this.value);
				} catch (Exception e) {
					getLogger().error("Error while execute callback", e);
				}
			}
			isInDoneProcessing.set(false);
		} else {
			throw new IllegalStateException("Future is in done processing");
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (this.done.compareAndSet(false, true) && this.cancelled.compareAndSet(false, true)) {
			if (this.sourceFuture != null && !this.sourceFuture.isCancelled()) {
				this.sourceFuture.cancel(mayInterruptIfRunning);
			}
			if (this.getFailedCause() == null) {
				this.setFailedCause(new ExecutionCancelledException());
			}
			doComplete();
			return true;
		}
		return false;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled.get();
	}

	@Override
	public void setAndDone(V value) {
		if (this.done.compareAndSet(false, true)) {
			this.value = value;
			this.doComplete();
		}
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		if (!this.isDone()) {
			this.doneSignal.await();
			if (timeoutFlag.get() && this.getFailedCause() == null) {
				synchronized (this) {
					if (timeoutFlag.get() && this.getFailedCause() == null) {
						this.setFailedCause(new TimeoutException());
					}
				}
			}
			if (timeoutFlag.get() || this.isCancelled()) {
				return null;
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
	public void setCallback(Callback<V> callable) {
		if (callable != null && callable != this.callback && this.hasCallback.compareAndSet(false, true)) {
			this.callback = callable;
			if (this.isDone()) {
				this.callback.apply(this.value);
			}
		}
	}

	@Override
	public Callback<V> getCallback() {
		return this.callback;
	}

	public Future<?> getCancelFuture() {
		return sourceFuture;
	}

	public void setCancelFuture(Future<?> cancelFuture) {
		this.sourceFuture = cancelFuture;
	}

	public Throwable getFailedCause() {
		return failedCause;
	}

	public void setFailedCause(Throwable failedCause) {
		this.failedCause = failedCause;
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
									setFailedCause(exceptionTobeThrown);
									cancel(false);
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
