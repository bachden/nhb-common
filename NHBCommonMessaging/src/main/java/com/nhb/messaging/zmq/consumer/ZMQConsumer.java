package com.nhb.messaging.zmq.consumer;

public interface ZMQConsumer {

	void init(ZMQConsumerConfig config);

	void start();

	void stop();

	boolean isRunning();
}
