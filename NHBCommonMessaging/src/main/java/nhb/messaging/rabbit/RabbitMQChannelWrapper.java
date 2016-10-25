package nhb.messaging.rabbit;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import nhb.eventdriven.impl.BaseEventDispatcher;
import nhb.messaging.rabbit.connection.RabbitMQConnection;

public abstract class RabbitMQChannelWrapper extends BaseEventDispatcher implements Closeable {

	private AtomicBoolean isConnected = new AtomicBoolean(false);
	private Channel channel;
	private AtomicBoolean isStopping = new AtomicBoolean(false);
	private RabbitMQConnection connection;
	private ShutdownListener channelShutdownListener = new ShutdownListener() {

		@Override
		public void shutdownCompleted(ShutdownSignalException ex) {
			getLogger().error("Channel shutted downs, error", ex);
		}
	};

	public RabbitMQChannelWrapper(RabbitMQConnection connection) {
		this.connection = connection;
	}

	public final void start() {
		if (!this.isConnected()) {
			synchronized (this) {
				if (!this.isConnected()) {
					this.connect();
					this._start();
				}
			}
		}
	}

	public final boolean isConnected() {
		return this.isConnected.get();
	}

	private void connect() {
		try {
			this.channel = connection.createChannel();
			this.channel.addShutdownListener(this.channelShutdownListener);
			this.isConnected.set(true);
			this.onChannelReady(this.channel);
		} catch (IOException e) {
			try {
				getLogger().error("Unable to create channel", e);
				this.channel.close();
			} catch (IOException e1) {
				getLogger().error("Unable to close channel due an error occur: ", e1);
			} catch (TimeoutException e1) {
				getLogger().error("Unable to close channel, timeout: ", e1);
			}
			this.channel = null;
			this.isConnected.set(false);
		}
	}

	protected abstract void onChannelReady(Channel channel) throws IOException;

	protected abstract void _stop();

	protected abstract void _start();

	@Override
	public final void close() {
		if (!this.isConnected()) {
			return;
		}
		this.isStopping.set(true);
		this._stop();
		if (this.channel != null && this.channel.isOpen()) {
			try {
				this.channel.close();
				this.channel.removeShutdownListener(channelShutdownListener);
			} catch (Exception e) {
				getLogger().debug("Channel close error", e);
			}
			this.channel = null;
		}
		this.isStopping.set(false);
		this.isConnected.set(false);
	}

	protected Channel getChannel() {
		return this.channel;
	}

}
