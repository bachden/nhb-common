package com.nhb.messaging.zmq;

public interface ZMQReceiver {

	boolean isRunning();

	boolean isInitialized();

	void init(ZMQSocketRegistry registry, ZMQReceiverConfig config);

	void setReceivedCountEnabled(boolean enabled);

	long getReceivedCount();

	void start();

	void stop();

	String getEndpoint();
}
