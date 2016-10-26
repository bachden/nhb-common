package com.nhb.messaging.rabbit.connection;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.nhb.common.BaseLoggable;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitMQConnection extends BaseLoggable implements Closeable {

	private Connection sourceConnection;
	private List<Channel> channels;
	private ShutdownListener channelShutdownListener = new ShutdownListener() {

		@Override
		public void shutdownCompleted(ShutdownSignalException ex) {
			getLogger().info(ex.getMessage());
		}
	};

	public RabbitMQConnection(Connection newConnection) {
		this.sourceConnection = newConnection;
		this.channels = new ArrayList<>();
	}

	@Override
	public void close() throws IOException {
		for (Channel channel : new ArrayList<>(channels)) {
			this.releaseChannel(channel);
		}
	}

	public Connection getSourceConnection() {
		return sourceConnection;
	}

	public void setSourceConnection(Connection sourceConnection) {
		this.sourceConnection = sourceConnection;
	}

	public Channel createChannel() throws IOException {
		Channel result = this.sourceConnection.createChannel();
		if (result != null) {
			this.channels.add(result);
			result.addShutdownListener(this.channelShutdownListener);
		} else {
			throw new RuntimeException("create channel error, null");
		}
		return result;
	}

	public void releaseChannel(Channel channel) {
		if (channel != null) {
			if (!this.channels.contains(channel)) {
				throw new RuntimeException("cannot close not-owned channel");
			}
			try {
				channel.removeShutdownListener(this.channelShutdownListener);
				channel.close();
				this.channels.remove(channel);
			} catch (IOException e) {
				getLogger().error("cannot close channel", e);
			} catch (TimeoutException e) {
				getLogger().error("cannot close channel", e);
			}
		}
	}
}
