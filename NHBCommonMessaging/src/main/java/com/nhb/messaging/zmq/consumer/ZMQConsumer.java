package com.nhb.messaging.zmq.consumer;

public interface ZMQConsumer {

	boolean isInitialized();

	void init(ZMQConsumerConfig config);

	void start();

	void stop();

	boolean isRunning();
}
