package nhb.messaging.rabbit.producer;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.rabbitmq.client.AMQP.BasicProperties;

import nhb.common.async.BaseRPCFuture;
import nhb.common.async.RPCFuture;
import nhb.common.data.PuElement;
import nhb.common.data.msgpkg.PuElementTemplate;
import nhb.messaging.rabbit.RabbitMQQueueConfig;
import nhb.messaging.rabbit.connection.RabbitMQConnection;
import nhb.messaging.rabbit.connection.RabbitMQConnectionPool;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RabbitMQRoutingRPCProducer extends RabbitMQProducer<RPCFuture<PuElement>> {

	private String replyQueueName;
	private Consumer consumer;

	private Map<String, BaseRPCFuture<PuElement>> futures = new ConcurrentHashMap<>();

	public RabbitMQRoutingRPCProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
		super(connection, queueConfig);
	}

	public RabbitMQRoutingRPCProducer(RabbitMQConnectionPool connectionPool, RabbitMQQueueConfig queueConfig) {
		super(connectionPool, queueConfig);
	}

	@Override
	public RPCFuture<PuElement> publish(PuElement data) {
		return this.publish(data, this.getQueueConfig().getRoutingKey());
	}

	@Override
	public RPCFuture<PuElement> publish(PuElement data, String routingKey) {
		if (this.getChannel() == null) {
			throw new RuntimeException("RabbitMQ Brocker has not been connected yet, please start before publish");
		}
		return this.publish(routingKey, data.toBytes());
	}

	public BaseRPCFuture<PuElement> publish(String routingKey, byte[] data) {
		String corrId = UUID.randomUUID().toString();
		BasicProperties properties = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName)
				.build();
		return this.publish(routingKey, properties, data);
	}

	private BaseRPCFuture<PuElement> publish(String routingKey, BasicProperties properties, byte[] data) {
		try {
			BaseRPCFuture<PuElement> future = new BaseRPCFuture<PuElement>();
			this.futures.put(properties.getCorrelationId(), future);
			getChannel().basicPublish(getQueueConfig().getExchangeName(),
					routingKey == null ? this.getQueueConfig().getRoutingKey() : routingKey, properties, data);
			return future;
		} catch (IOException e) {
			getLogger().error("An error occurs while publishing data", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onChannelReady(Channel channel) throws IOException {
		channel.exchangeDeclare(getQueueConfig().getExchangeName(), getQueueConfig().getExchangeType());
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
						RabbitMQRoutingRPCProducer.this.futures.remove(corrId);
					}
				}
			}
		};
		channel.basicConsume(replyQueueName, true, this.consumer);
	}

	@Override
	public RPCFuture<PuElement> forward(byte[] data, BasicProperties properties, String routingKey) {
		if (properties == null) {
			return this.publish(routingKey, data);
		}
		return this.publish(routingKey, properties, data);
	}

	@Override
	protected void _start() {

	}

	@Override
	protected void _stop() {
		for (BaseRPCFuture<?> future : this.futures.values()) {
			future.cancel(true);
		}
	}
}
