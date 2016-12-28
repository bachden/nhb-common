package com.nhb.eventdriven.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.nhb.common.BaseLoggable;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventDispatcher;
import com.nhb.eventdriven.EventHandler;

public class BaseEventDispatcher extends BaseLoggable implements EventDispatcher {

	protected Map<String, List<EventHandler>> listeners;

	@Override
	public void addEventListener(String eventType, EventHandler listener) {
		if (this.listeners == null) {
			this.listeners = new ConcurrentHashMap<String, List<EventHandler>>();
		}
		if (!this.listeners.containsKey(eventType)) {
			this.listeners.put(eventType, new CopyOnWriteArrayList<EventHandler>());
		}
		this.listeners.get(eventType).add(listener);
	}

	@Override
	public void removeEventListener(String eventType, EventHandler listener) {
		if (this.listeners != null && this.listeners.containsKey(eventType)) {
			if (listener == null) {
				this.listeners.remove(eventType);
			} else {
				this.listeners.get(eventType).remove(listener);
			}
		}
	}

	@Override
	public void removeAllEventListener() {
		this.listeners = null;
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
				try {
					for (EventHandler listener : tmpListeners) {
						listener.onEvent(event);
					}
				} catch (Exception e) {
					throw new RuntimeException("error while event handled: " + event.getType(), e);
				}
			}
		}
	}

	@Override
	public void dispatchEvent(String eventType, Object... data) {
		this.dispatchEvent(new BaseEvent(eventType, data));
	}
}
