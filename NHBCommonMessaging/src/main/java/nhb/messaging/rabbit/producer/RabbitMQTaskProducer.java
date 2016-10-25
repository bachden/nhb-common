package nhb.messaging.rabbit.producer;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;

import nhb.common.data.PuElement;
import nhb.messaging.rabbit.RabbitMQQueueConfig;
import nhb.messaging.rabbit.connection.RabbitMQConnection;
import nhb.messaging.rabbit.connection.RabbitMQConnectionPool;

import com.rabbitmq.client.Channel;

public class RabbitMQTaskProducer extends RabbitMQProducer<Boolean> {

	public RabbitMQTaskProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
		super(connection, queueConfig);
	}

	public RabbitMQTaskProducer(RabbitMQConnectionPool connectionPool, RabbitMQQueueConfig queueConfig) {
		super(connectionPool, queueConfig);
	}

	@Override
	public Boolean publish(PuElement data) {
		if (this.getChannel() == null) {
			throw new RuntimeException("RabbitMQ Brocker has not been connected yet, please start before publish");
		}
		if (data != null) {
			return this.publish(data.toBytes());
		}
		return false;
	}

	public boolean publish(byte[] data) {
		return this.publish(data, null);
	}

	private boolean publish(byte[] data, BasicProperties properties) {
		try {
			getChannel().basicPublish("", getQueueConfig().getQueueName(), properties, data);
			return true;
		} catch (IOException e) {
			throw new RuntimeException("Error while publishing message", e);
		}
	}

	@Override
	protected void onChannelReady(Channel channel) throws IOException {
		channel.queueDeclare(getQueueConfig().getQueueName(), false, false, false, null);
	}

	@Override
	public Boolean forward(byte[] data, BasicProperties properties, String routingKey) {
		return this.publish(data, properties);
	}

	@Override
	protected void _start() {

	}

	@Override
	protected void _stop() {

	}
}
