package com.nhb.messaging.socket;

import com.nhb.common.vo.HostAndPort;
import com.nhb.eventdriven.impl.BaseEventDispatcher;

public abstract class BaseSocketClient extends BaseEventDispatcher implements SocketClient {

	private HostAndPort serverAddress;

	public HostAndPort getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(HostAndPort serverAddress) {
		this.serverAddress = serverAddress;
	}
}
