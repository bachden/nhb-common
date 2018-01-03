package com.nhb.eventdriven.impl;

import java.util.HashMap;

import com.nhb.eventdriven.Callable;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventDispatcher;

public class BaseEvent extends HashMap<String, Object> implements Event {

	private static final long serialVersionUID = -2206533823729431514L;
	private String type;
	private Callable callback;
	private EventDispatcher target;
	private EventDispatcher currentTarget;

	public BaseEvent() {

	}

	public BaseEvent(String type) {
		this.setType(type);
	}

	public BaseEvent(String type, Object... dataKeyValues) {
		this(type);
		for (int i = 0; i < dataKeyValues.length; i += 2) {
			this.put(dataKeyValues[i].toString(), dataKeyValues[i + 1]);
		}
	}

	public BaseEvent(String type, Callable callback) {
		this(type);
		this.setCallback(callback);
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public void setCallback(Callable callback) {
		this.callback = callback;
	}

	@Override
	public Callable getCallback() {
		return this.callback;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends EventDispatcher> T getTarget() {
		return (T) this.target;
	}

	@Override
	public void setTarget(EventDispatcher target) {
		this.target = target;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends EventDispatcher> T getCurrentTarget() {
		return (T) this.currentTarget;
	}

	@Override
	public void setCurrentTarget(EventDispatcher target) {
		this.currentTarget = target;
	}

}
