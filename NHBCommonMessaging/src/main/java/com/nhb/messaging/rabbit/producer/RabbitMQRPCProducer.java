package com.nhb.messaging.rabbit.producer;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.msgpkg.PuElementTemplate;
import com.nhb.messaging.rabbit.RabbitMQQueueConfig;
import com.nhb.messaging.rabbit.connection.RabbitMQConnection;
import com.nhb.messaging.rabbit.connection.RabbitMQConnectionPool;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RabbitMQRPCProducer extends RabbitMQProducer<RPCFuture<PuElement>> {

	private String replyQueueName;
	private Consumer consumer;

	private Map<String, BaseRPCFuture<PuElement>> futures = new ConcurrentHashMap<>();

	public RabbitMQRPCProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
		super(connection, queueConfig);
	}

	public RabbitMQRPCProducer(RabbitMQConnectionPool connectionPool, RabbitMQQueueConfig queueConfig) {
		super(connectionPool, queueConfig);
	}

	@Override
	public RPCFuture<PuElement> publish(PuElement data) {
		if (this.getChannel() == null) {
			throw new RuntimeException("RabbitMQ Brocker has not been connected yet, please start before publish");
		}
		return this.publish(data.toBytes());
	}

	public BaseRPCFuture<PuElement> publish(byte[] data) {
		String corrId = UUID.randomUUID().toString();
		BasicProperties properties = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName)
				.build();
		return this.publish(properties, data);
	}

	private BaseRPCFuture<PuElement> publish(BasicProperties properties, byte[] data) {
		try {
			BaseRPCFuture<PuElement> future = new BaseRPCFuture<PuElement>();
			this.futures.put(properties.getCorrelationId(), future);
			getChannel().basicPublish("", getQueueConfig().getQueueName(), properties, data);
			return future;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onChannelReady(Channel channel) throws IOException {
		this.replyQueueName = channel.queueDeclare().getQueue();
		this.consumer = new DefaultConsumer(channel) {

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String corrId = properties.getCorrelationId();
				BaseRPCFuture<PuElement> future = futures.get(corrId);
				if (future != null) {
					try {
						future.set(PuElementTemplate.getInstance().read(body));
						future.done();
					} finally {
						RabbitMQRPCProducer.this.futures.remove(corrId);
					}
				} else {
					getLogger().debug("Future not found for corrId: " + corrId);
				}
			}
		};
		channel.basicConsume(replyQueueName, true, this.consumer);
	}

	@Override
	public RPCFuture<PuElement> forward(byte[] data, BasicProperties properties, String routingKey) {
		if (properties == null) {
			return this.publish(data);
		}
		return this.publish(properties, data);
	}

	@Override
	protected void _start() {
		getLogger().info("RabbitMQRPCProducer has started successfully!!!");
	}

	@Override
	protected void _stop() {
		for (BaseRPCFuture<?> future : this.futures.values()) {
			future.cancel(true);
		}
	}
}
