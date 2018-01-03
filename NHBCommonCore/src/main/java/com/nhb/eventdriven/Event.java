package com.nhb.eventdriven;

public interface Event {

	void setType(String type);

	String getType();

	void setCallback(Callable callback);

	Callable getCallback();

	<T extends EventDispatcher> T getTarget();

	void setTarget(EventDispatcher target);

	<T extends EventDispatcher> T getCurrentTarget();

	void setCurrentTarget(EventDispatcher target);

	@SuppressWarnings("unchecked")
	default <T extends Event> T cast() {
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	default <T extends Event> T as(Class<T> cls) {
		return (T) this;
	}
}
