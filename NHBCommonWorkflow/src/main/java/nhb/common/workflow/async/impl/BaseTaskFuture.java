package nhb.common.workflow.async.impl;

import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import nhb.common.Loggable;
import nhb.common.workflow.JobContext;
import nhb.common.workflow.async.CancelDispatcher;
import nhb.common.workflow.async.TaskFuture;
import nhb.common.workflow.async.TaskListener;

public class BaseTaskFuture implements Loggable, TaskFuture {

	private static ScheduledExecutorService timeoutMonitorScheduledExecutor;

	protected static ScheduledExecutorService getTimeoutMonitor() {
		if (timeoutMonitorScheduledExecutor == null) {
			synchronized (BaseTaskFuture.class) {
				if (timeoutMonitorScheduledExecutor == null) {
					timeoutMonitorScheduledExecutor = Executors.newScheduledThreadPool(4);
				}
			}
		}
		return timeoutMonitorScheduledExecutor;
	}

	private final Collection<TaskListener> listeners = new CopyOnWriteArrayList<>();

	private final AtomicBoolean isDone = new AtomicBoolean(false);
	// private final CountDownLatch doneSignal = new CountDownLatch(1);

	private ScheduledFuture<?> timeoutFuture;
	private final AtomicBoolean isTimeout = new AtomicBoolean(false);

	private final JobContext context;
	private Throwable failedCause;

	public BaseTaskFuture(JobContext context) {
		this.context = context;
	}

	@Override
	public void addListener(TaskListener listener) {
		this.listeners.add(listener);
		if (this.isDone.get()) {
			if (this.failedCause != null) {
				listener.onFailure(context, this.failedCause);
			} else {
				listener.onSuccess(getContext());
			}
		}
	}

	@Override
	public void setTimeout(long timeout, TimeUnit timeUnit) {
		if (this.timeoutFuture == null) {
			synchronized (this) {
				if (this.timeoutFuture == null) {
					this.timeoutFuture = getTimeoutMonitor().schedule(new Runnable() {

						@Override
						public void run() {
							if (isTimeout.compareAndSet(false, true)) {
								timeoutFuture = null;
								dispatchFailure(new TimeoutException());
							}
						}
					}, timeout, timeUnit);
				}
			}
		}
	}

	@Override
	public void cancel() {
		if (this.timeoutFuture != null) {
			this.timeoutFuture.cancel(false);
			this.timeoutFuture = null;
		}
		if (this.getContext() instanceof CancelDispatcher) {
			((CancelDispatcher) this.getContext()).dispatchCancel();
		}
		this.dispatchFailure(new CancellationException("Task cancelled while running"));
	}

	@Override
	public void dispatchSuccess() {
		if (this.isDone.compareAndSet(false, true)) {
			this.callbackSuccess();
			// this.doneSignal.countDown();
		}
	}

	@Override
	public void dispatchFailure(Throwable cause) {
		if (this.isDone.compareAndSet(false, true)) {
			this.failedCause = cause;
			this.callbackFailure();
			// this.doneSignal.countDown();
		}
	}

	private void callbackSuccess() {
		try {
			for (TaskListener listener : this.listeners) {
				listener.onSuccess(this.getContext());
			}
		} catch (Exception e) {
			getLogger().error("Error while dispatching success event", e);
		}
	}

	private void callbackFailure() {
		try {
			for (TaskListener listener : this.listeners) {
				listener.onFailure(this.getContext(), this.failedCause);
			}
		} catch (Exception e) {
			getLogger().error("Error while dispatching failure event", e);
		}
	}

	public JobContext getContext() {
		return context;
	}

}
