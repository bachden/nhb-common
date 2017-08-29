package com.nhb.common.async.translator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.async.Callback;
import com.nhb.common.async.RPCCallback;
import com.nhb.common.async.RPCFuture;

public abstract class RPCFutureTranslator<FromType, ToType> extends AbstractFutureTranslator<FromType, ToType>
		implements RPCFuture<ToType> {

	private static ScheduledExecutorService monitoringExecutorService;

	protected static ScheduledExecutorService getScheduledExecutorService() {
		if (monitoringExecutorService == null) {
			synchronized (RPCFutureTranslator.class) {
				monitoringExecutorService = Executors.newScheduledThreadPool(4);
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
		}
		return monitoringExecutorService;
	}

	private Callback<ToType> callback;
	private Object monitorFuture;

	@SuppressWarnings("unchecked")
	public RPCFutureTranslator(RPCFuture<FromType> future) {
		super(future);
		if (this.getSourceFuture() instanceof RPCCallback) {
			((RPCCallback<FromType>) this.getSourceFuture()).setCallback(new Callback<FromType>() {

				@Override
				public void apply(FromType result) {
					if (getCallback() != null) {
						getCallback().apply(parseAndSaveResult(result));
					}
				}
			});
		}
	}

	@Override
	public void setCallback(Callback<ToType> callable) {
		this.callback = callable;
		if (this.getSourceFuture().isDone()) {
			ToType result = null;
			try {
				result = this.get();
				if (result == null && !this.isAllowNullResult()) {
					this.setFailedCause(((RPCFuture<FromType>) this.getSourceFuture()).getFailedCause());
				}
			} catch (Exception e) {
				this.setFailedCause(e);
			}
			this.callback.apply(result);
		}
	}

	public static void main(String[] args) {
		BaseRPCFuture<String> sourceFuture = new BaseRPCFuture<>();
		sourceFuture.setFailedCause(new RuntimeException("this is error"));
		sourceFuture.setAndDone(null);

		final RPCFutureTranslator<String, Integer> futureTranslator = new RPCFutureTranslator<String, Integer>(
				sourceFuture) {

			@Override
			protected Integer translate(String sourceResult) throws Exception {
				return Integer.valueOf(sourceResult);
			}
		};

		futureTranslator.setCallback(new Callback<Integer>() {

			@Override
			public void apply(Integer result) {
				if (result != null) {
					System.out.println("Result: " + result);
				} else {
					System.out.println("Error...");
					futureTranslator.getFailedCause().printStackTrace();
				}
			}
		});
	}

	@Override
	public Callback<ToType> getCallback() {
		return this.callback;
	}

	@Override
	public void setTimeout(long timeout, TimeUnit unit) {
		if (this.getSourceFuture() instanceof RPCFuture) {
			((RPCFuture<FromType>) this.getSourceFuture()).setTimeout(timeout, unit);
		} else {
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
}
