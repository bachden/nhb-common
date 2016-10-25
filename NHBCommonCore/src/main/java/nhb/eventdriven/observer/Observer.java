package nhb.eventdriven.observer;

import nhb.eventdriven.Event;
import nhb.eventdriven.EventHandler;
import nhb.eventdriven.impl.BaseEvent;
import nhb.eventdriven.impl.BaseEventDispatcher;

public class Observer extends BaseEventDispatcher {

	public void notifyCommand(String command, Object... data) {
		super.dispatchEvent(new BaseEvent(command, data));
	}

	public void registerCommand(String command, EventHandler listener) {
		super.addEventListener(command, listener);
	}

	public void deregisterCommand(String command, EventHandler handler) {
		super.removeEventListener(command, handler);
	}

	@Override
	@Deprecated
	public void removeEventListener(String eventType, EventHandler listener) {
		throw new UnsupportedOperationException("user deregisterCommand method instead");
	}

	@Override
	@Deprecated
	public void addEventListener(String eventType, EventHandler listener) {
		throw new UnsupportedOperationException("user registerCommand method instead");
	}

	@Override
	@Deprecated
	public void dispatchEvent(Event event) {
		throw new UnsupportedOperationException("user notifyCommand method instead");
	}

	@Override
	@Deprecated
	public void dispatchEvent(String eventType, Object... data) {
		throw new UnsupportedOperationException("user notifyCommand method instead");
	}
}
