package nhb.messaging.rabbit.producer;

import java.io.IOException;
import java.util.UUID;

import com.rabbitmq.client.AMQP.BasicProperties;

import nhb.common.data.PuElement;
import nhb.messaging.rabbit.RabbitMQQueueConfig;
import nhb.messaging.rabbit.connection.RabbitMQConnection;
import nhb.messaging.rabbit.connection.RabbitMQConnectionPool;

import com.rabbitmq.client.Channel;

public class RabbitMQRoutingProducer extends RabbitMQProducer<Boolean> {

	private String replyQueueName;

	public RabbitMQRoutingProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
		super(connection, queueConfig);
	}
	
	public RabbitMQRoutingProducer(RabbitMQConnectionPool connectionPool, RabbitMQQueueConfig queueConfig) {
		super(connectionPool, queueConfig);
	}

	@Override
	public Boolean publish(PuElement data) {
		return this.publish(data, this.getQueueConfig().getRoutingKey());
	}

	@Override
	public Boolean publish(PuElement data, String routingKey) {
		if (this.getChannel() == null) {
			throw new RuntimeException("RabbitMQ Brocker has not been connected yet, please start before publish");
		}
		return this.publish(routingKey, data.toBytes());
	}

	public Boolean publish(String routingKey, byte[] data) {
		String corrId = UUID.randomUUID().toString();
		BasicProperties props = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build();
		return this.publish(routingKey, props, data);
	}

	private Boolean publish(String routingKey, BasicProperties properties, byte[] data) {
		try {
			getChannel().basicPublish(getQueueConfig().getExchangeName(),
					routingKey == null ? this.getQueueConfig().getRoutingKey() : routingKey, properties, data);
			return true;
		} catch (IOException e) {
			getLogger().error("An error occurs while publishing data", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onChannelReady(Channel channel) throws IOException {
		channel.exchangeDeclare(getQueueConfig().getExchangeName(), getQueueConfig().getExchangeType());
	}

	@Override
	public Boolean forward(byte[] data, BasicProperties properties, String routingKey) {
		// if (properties == null) {
		// return this.publish(routingKey, data);
		// }
		return this.publish(routingKey, properties, data);
	}

	@Override
	protected void _start() {

	}

	@Override
	protected void _stop() {

	}
}
