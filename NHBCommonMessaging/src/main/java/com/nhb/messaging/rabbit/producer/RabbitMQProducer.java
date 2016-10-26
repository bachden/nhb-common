package com.nhb.messaging.rabbit.producer;

import com.nhb.common.data.PuElement;
import com.nhb.messaging.MessageProducer;
import com.nhb.messaging.rabbit.RabbitMQChannelWrapper;
import com.nhb.messaging.rabbit.RabbitMQQueueConfig;
import com.nhb.messaging.rabbit.connection.RabbitMQConnection;
import com.nhb.messaging.rabbit.connection.RabbitMQConnectionPool;
import com.rabbitmq.client.AMQP.BasicProperties;

public abstract class RabbitMQProducer<T> extends RabbitMQChannelWrapper implements MessageProducer<T> {

	private RabbitMQQueueConfig queueConfig;

	public RabbitMQProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
		super(connection);
		this.queueConfig = queueConfig;
	}

	public RabbitMQProducer(RabbitMQConnectionPool connectionPool, RabbitMQQueueConfig queueConfig) {
		super(connectionPool.getConnection());
		this.queueConfig = queueConfig;
	}

	public abstract T forward(byte[] data, BasicProperties properties, String routingKey);

	@Override
	public T publish(PuElement data) {
		// tobe override by subclass
		throw new UnsupportedOperationException(
				"Method publish(PuObject) doesn't supported in " + this.getClass().getName() + " class");
	}

	@Override
	public T publish(PuElement data, String key) {
		// tobe override by subclass
		throw new UnsupportedOperationException("Method publish(PuObject data, String key) doesn't supported in "
				+ this.getClass().getName() + " class");
	}

	public RabbitMQQueueConfig getQueueConfig() {
		return queueConfig;
	}
}
