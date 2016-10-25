package nhb.messaging.socket;

import nhb.common.vo.HostAndPort;
import nhb.eventdriven.impl.BaseEventDispatcher;

public abstract class BaseSocketClient extends BaseEventDispatcher implements SocketClient {

	private HostAndPort serverAddress;

	public HostAndPort getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(HostAndPort serverAddress) {
		this.serverAddress = serverAddress;
	}
}
