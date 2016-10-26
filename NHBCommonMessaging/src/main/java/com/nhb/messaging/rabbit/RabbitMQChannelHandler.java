package com.nhb.messaging.rabbit;

import java.io.IOException;

import com.rabbitmq.client.Channel;

public interface RabbitMQChannelHandler {

	void onChannelReady(Channel channel) throws IOException;
}
