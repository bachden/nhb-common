package com.nhb.messaging.rabbit;

import java.io.IOException;

import com.nhb.messaging.rabbit.connection.RabbitMQConnection;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import lombok.Getter;

public abstract class RabbitMQConsumer extends RabbitMQChannelWrapper {

	@Getter
	private RabbitMQQueueConfig queueConfig;

	public RabbitMQConsumer(RabbitMQQueueConfig queueConfig, RabbitMQConnection connection) {
		super(connection);
		this.queueConfig = queueConfig;
	}

	@Override
	protected void onChannelReady(Channel channel) throws IOException {
		this.initQueue();
		channel.basicConsume(this.getQueueConfig().getQueueName(), getQueueConfig().isAutoAck(),
				new DefaultConsumer(channel) {

					@Override
					public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
							byte[] body) throws IOException {
						RabbitMQConsumer.this.handleDelivery(consumerTag, envelope, properties, body);
					}
				});
	}

	protected abstract void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
			byte[] body);

	private void initQueue() throws IOException {
		Channel channel = this.getChannel();

		// declare queue
		String queueName = queueConfig.getQueueName();
		if (queueName != null && queueName.trim().length() > 0) {
			channel.queueDeclare(queueName, queueConfig.isDurable(), queueConfig.isExclusive(),
					queueConfig.isAutoDelete(), queueConfig.getArguments());
		} else {
			queueName = channel.queueDeclare().getQueue();
			this.queueConfig.setQueueName(queueName);
		}

		// setting Qos (perfect count) value
		if (queueConfig.getQos() >= 0) {
			channel.basicQos(queueConfig.getQos());
		}

		// bind to an exchange
		if (!queueConfig.getExchangeName().isEmpty()) {
			channel.exchangeDeclare(queueConfig.getExchangeName(), queueConfig.getExchangeType());
			if (queueConfig.getRoutingKey() != null) {
				// only execute by when routing key is specific
				this.queueBind(queueConfig.getRoutingKey());
			}
		}
	}

	public void queueBind(String rountingKey) throws IOException {
		if (this.getQueueConfig().getExchangeName().isEmpty()) {
			getLogger().warn("Exchange name is empty", new Exception());
		}
		String queueName = this.getQueueConfig().getQueueName();
		queueName = queueName == null ? "" : queueName;
		String exchangeName = this.getQueueConfig().getExchangeName();
		getChannel().queueBind(queueName, exchangeName, rountingKey);
	}

	public void queueUnbind(String rountingKey) throws IOException {
		if (getQueueConfig().getExchangeName().isEmpty()) {
			getLogger().warn("Exchange name is empty", new Exception());
		}
		String queueName = this.getQueueConfig().getQueueName();
		queueName = queueName == null ? "" : queueName;
		String exchangeName = this.getQueueConfig().getExchangeName();
		getChannel().queueUnbind(queueName, exchangeName, rountingKey);
	}

	@Override
	protected void _stop() {
		// do nothing
	}

	@Override
	protected void _start() {
		// do nothing
	}

}
