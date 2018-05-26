package com.nhb.messaging.zmq;

public interface ZMQReceiver {

	boolean isRunning();

	void init(ZMQSocketRegistry registry, ZMQReceiverConfig config);

	void start();

	void stop();

	String getEndpoint();
}
