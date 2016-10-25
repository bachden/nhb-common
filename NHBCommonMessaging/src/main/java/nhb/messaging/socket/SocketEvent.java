package nhb.messaging.socket;

import nhb.common.data.PuElement;
import nhb.eventdriven.impl.AbstractEvent;

public class SocketEvent extends AbstractEvent {

	public static final String CONNECTED = "connected";
	public static final String DISCONNECTED = "disconnected";
	public static final String MESSAGE = "message";

	private PuElement data;

	public SocketEvent() {
		super();
	}

	public SocketEvent(String type) {
		this();
		this.setType(type);
	}

	public SocketEvent(String type, PuElement data) {
		this(type);
		this.setData(data);
	}

	public PuElement getData() {
		return data;
	}

	public void setData(PuElement data) {
		this.data = data;
	}
}
