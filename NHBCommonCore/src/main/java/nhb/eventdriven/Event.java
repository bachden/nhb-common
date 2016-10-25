package nhb.eventdriven;

public interface Event {

	public void setType(String type);

	public String getType();

	public void setCallback(Callable callback);

	public Callable getCallback();

	public <T extends EventDispatcher> T getTarget();

	public void setTarget(EventDispatcher target);

	public <T extends EventDispatcher> T getCurrentTarget();

	public void setCurrentTarget(EventDispatcher target);
}
