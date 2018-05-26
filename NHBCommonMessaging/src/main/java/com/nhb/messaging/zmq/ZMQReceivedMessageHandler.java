package com.nhb.messaging.zmq;

public interface ZMQReceivedMessageHandler {

	void onReceive(ZMQEvent message);
}