package com.nhb.eventdriven.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventHandler;

public class ParallelEventDispatcher extends BaseEventDispatcher implements Closeable {

	private final boolean isSelfCreatedExecutor;
	private final ExecutorService executor;

	public ParallelEventDispatcher() {
		this.executor = Executors.newCachedThreadPool();
		this.isSelfCreatedExecutor = true;
	}

	public ParallelEventDispatcher(ExecutorService threadPool) {
		this.executor = threadPool;
		this.isSelfCreatedExecutor = false;
	}

	@Override
	public void close() throws IOException {
		if (this.isSelfCreatedExecutor) {
			this.executor.shutdown();
			try {
				if (this.executor.awaitTermination(3, TimeUnit.SECONDS)) {
					this.executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				getLogger().error("Error while shuting down executor");
			}
		}
	}

	@Override
	public void dispatchEvent(Event event) {
		String eventType = event.getType();
		if (eventType != null && this.listeners != null && this.listeners.containsKey(eventType)) {
			if (event.getTarget() == null) {
				event.setTarget(this);
			}
			event.setCurrentTarget(this);
			List<EventHandler> tmpListeners = this.listeners.get(eventType);
			if (tmpListeners.size() > 0) {
				for (EventHandler listener : tmpListeners) {
					this.executor.submit(new Runnable() {

						@Override
						public void run() {
							try {
								listener.onEvent(event);
							} catch (Exception e) {
								throw new RuntimeException("error while event handled: " + event.getType(), e);
							}
						}
					});
				}
			}
		}
	}

}
