package nhb.messaging.socket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import nhb.common.BaseLoggable;
import nhb.common.data.PuElement;
import nhb.messaging.socket.SocketSender;

public class NettySocketSender extends BaseLoggable implements SocketSender {

	private Channel channel;

	public NettySocketSender(Channel channel) {
		this.channel = channel;
	}

	@Override
	public void send(PuElement message) {
		this.send(message, false);
	}

	@Override
	public void send(PuElement message, boolean sync) {
		ChannelFuture future = channel.writeAndFlush(message);
		if (sync) {
			try {
				future.sync();
			} catch (InterruptedException e) {
				throw new RuntimeException("cannot sync channel promise", e);
			}
		}
	}

}
