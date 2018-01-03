package com.nhb.messaging.socket;

import com.nhb.common.data.PuElement;

public interface SocketSender {

	void send(PuElement message, boolean sync);

	void send(PuElement message);
}
