package com.nhb.messaging.zmq.producer;

import java.io.Closeable;
import java.io.IOException;

import com.nhb.common.Loggable;
import com.nhb.common.data.PuElement;
import com.nhb.messaging.MessageProducer;
import com.nhb.messaging.StartableProducer;
import com.nhb.messaging.zmq.ZMQFuture;

import lombok.Getter;
import lombok.Setter;

public abstract class ZMQProducer implements MessageProducer<ZMQFuture>, Loggable, StartableProducer, Closeable {

	@Setter
	@Getter
	private String name;

	@Override
	public ZMQFuture publish(PuElement data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ZMQFuture publish(PuElement data, String routingKey) {
		throw new UnsupportedOperationException();
	}

	public void init(ZMQProducerConfig config) {

	}

	@Override
	public void close() throws IOException {
		this.stop();
	}

	public abstract void stop();

	public abstract long getSentCount();
}
