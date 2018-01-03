package com.nhb.messaging.rabbit;

import java.io.IOException;

import com.nhb.messaging.rabbit.connection.RabbitMQConnection;
import com.rabbitmq.client.Channel;

public class RabbitMQChannelHandleDelegate extends RabbitMQChannelWrapper {

	private RabbitMQChannelHandler handler;

	public RabbitMQChannelHandleDelegate(RabbitMQConnection connection, RabbitMQChannelHandler handler) {
		super(connection);
		this.handler = handler;
	}

	@Override
	protected void onChannelReady(Channel channel) throws IOException {
		if (this.handler != null) {
			this.handler.onChannelReady(channel);
		}
	}

	@Override
	public Channel getChannel() {
		return super.getChannel();
	}

	@Override
	protected void _stop() {

	}

	@Override
	protected void _start() {

	}
}
