package com.nhb.messaging.zmq;

public interface ZMQReceiver {

	boolean isRunning();
	
	boolean isInitialized();

	void init(ZMQSocketRegistry registry, ZMQReceiverConfig config);

	void start();

	void stop();

	String getEndpoint();
}
