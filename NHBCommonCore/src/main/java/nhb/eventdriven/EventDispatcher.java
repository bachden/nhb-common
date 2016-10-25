package nhb.eventdriven;

public interface EventDispatcher {
	public void addEventListener(String eventType, EventHandler listener);

	public void removeEventListener(String eventType, EventHandler listener);

	public void removeAllEventListener();

	public void dispatchEvent(Event event);

	public void dispatchEvent(String eventType, Object... data);
}
