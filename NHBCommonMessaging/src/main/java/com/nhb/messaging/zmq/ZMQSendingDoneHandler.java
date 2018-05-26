package com.nhb.messaging.zmq;

public interface ZMQSendingDoneHandler {

	void onSendingDone(ZMQEvent request);
}
