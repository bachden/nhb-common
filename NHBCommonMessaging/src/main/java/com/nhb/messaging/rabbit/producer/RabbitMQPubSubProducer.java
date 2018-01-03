package com.nhb.messaging.rabbit.producer;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.nhb.common.data.PuElement;
import com.nhb.messaging.rabbit.RabbitMQQueueConfig;
import com.nhb.messaging.rabbit.connection.RabbitMQConnection;
import com.nhb.messaging.rabbit.connection.RabbitMQConnectionPool;
import com.rabbitmq.client.Channel;

public class RabbitMQPubSubProducer extends RabbitMQProducer<Boolean> {

	public RabbitMQPubSubProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
		super(connection, queueConfig);
	}

	public RabbitMQPubSubProducer(RabbitMQConnectionPool connectionPool, RabbitMQQueueConfig queueConfig) {
		super(connectionPool, queueConfig);
	}

	@Override
	public Boolean publish(PuElement data) {
		if (data != null) {
			return this.publish(data.toBytes());
		}
		return false;
	}

	public boolean publish(byte[] data) {
		try {
			getChannel().basicPublish(getQueueConfig().getExchangeName(), getQueueConfig().getRoutingKey(), null, data);
			return true;
		} catch (Exception e) {
			getLogger().error("An error occurs while publishing message", e);
			throw new RuntimeException("Error while publishing message", e);
		}
	}

	@Override
	protected void onChannelReady(Channel channel) throws IOException {
		channel.exchangeDeclare(this.getQueueConfig().getExchangeName(), this.getQueueConfig().getExchangeType());
	}

	@Override
	public Boolean forward(byte[] data, BasicProperties properties, String routingKey) {
		return this.publish(data);
	}

	@Override
	protected void _start() {

	}

	@Override
	protected void _stop() {

	}
}
