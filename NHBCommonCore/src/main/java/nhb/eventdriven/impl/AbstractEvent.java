package nhb.eventdriven.impl;

import nhb.eventdriven.Callable;
import nhb.eventdriven.Event;
import nhb.eventdriven.EventDispatcher;

public class AbstractEvent implements Event {

	private String type;
	private Callable callable;
	private EventDispatcher target;

	private EventDispatcher currentTarget;

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
		this.callable = callback;
	}

	@Override
	public Callable getCallback() {
		return this.callable;
	}

	@SuppressWarnings("unchecked")
	@Override
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
