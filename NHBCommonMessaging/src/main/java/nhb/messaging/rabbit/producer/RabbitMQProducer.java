package nhb.messaging.rabbit.producer;

import com.rabbitmq.client.AMQP.BasicProperties;

import nhb.common.data.PuElement;
import nhb.messaging.MessageProducer;
import nhb.messaging.rabbit.RabbitMQChannelWrapper;
import nhb.messaging.rabbit.RabbitMQQueueConfig;
import nhb.messaging.rabbit.connection.RabbitMQConnection;
import nhb.messaging.rabbit.connection.RabbitMQConnectionPool;

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
