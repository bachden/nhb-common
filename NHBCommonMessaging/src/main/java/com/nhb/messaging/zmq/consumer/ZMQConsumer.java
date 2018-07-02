package com.nhb.messaging.zmq.consumer;

public interface ZMQConsumer {

	boolean isInitialized();

	void init(ZMQConsumerConfig config);

	long getReceivedCount();

	void start();

	void stop();

	boolean isRunning();
}
