package nhb.messaging.socket;

import nhb.common.data.PuElement;

public interface SocketSender {

	void send(PuElement message, boolean sync);

	void send(PuElement message);
}
