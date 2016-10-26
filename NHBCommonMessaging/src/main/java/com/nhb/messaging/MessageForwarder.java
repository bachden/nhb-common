package com.nhb.messaging;

public interface MessageForwarder {

	<T extends MessageProducer<?>> void forwardAndForget(T producer);

	<T extends MessageProducer<?>> void forwardAndForget(T producer, String routingKey);

	<E, T extends MessageProducer<E>> E forward(T producer);

	<E, T extends MessageProducer<E>> E forward(T producer, String routingKey);
}
