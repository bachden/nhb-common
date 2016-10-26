package com.nhb.messaging.socket.netty;

import java.io.IOException;

import com.nhb.common.data.PuElement;
import com.nhb.common.exception.UnsupportedTypeException;
import com.nhb.common.vo.HostAndPort;
import com.nhb.messaging.TransportProtocol;
import com.nhb.messaging.socket.BaseSocketClient;
import com.nhb.messaging.socket.SocketEvent;
import com.nhb.messaging.socket.netty.codec.MsgpackDecoder;
import com.nhb.messaging.socket.netty.codec.MsgpackEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.DefaultThreadFactory;

public class NettySocketClient extends BaseSocketClient {

	private boolean useLengthPrepender = true;
	private EventLoopGroup group = null;
	private ChannelFuture channelFuture = null;
	private NettySocketSender socketSender;

	private TransportProtocol protocol = TransportProtocol.TCP;

	public void connect() throws IOException {
		if (this.getServerAddress() == null) {
			throw new RuntimeException("Server address is not defined");
		}
		this._connect(getServerAddress().getHost(), getServerAddress().getPort(), getServerAddress().isUseSSL());
	}

	public void connect(String host, int port) throws IOException {
		this.connect(new HostAndPort(host, port, false));
	}

	public void connect(String host, int port, boolean useSSL) throws IOException {
		this.connect(new HostAndPort(host, port, useSSL));
	}

	public void connect(HostAndPort address) throws IOException {
		this.setServerAddress(address);
		this.connect();
	}

	private void _initChannel(Channel ch) {
		if (isUseLengthPrepender()) {
			ch.pipeline().addLast(new LengthFieldPrepender(4), new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
		}
		ch.pipeline().addLast(MsgpackEncoder.newInstance(), MsgpackDecoder.newInstance());
		ch.pipeline().addLast(new NettySocketSession(NettySocketClient.this));
	}

	protected void _connect(String host, int port, boolean useSSL) throws IOException {

		if (this.isConnected()) {
			throw new RuntimeException("Connection still alive");
		}

		// Configure the client.

		Bootstrap bootstrap = new Bootstrap();

		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		switch (this.getProtocol()) {
		case TCP:
			group = new NioEventLoopGroup(3);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.group(group).handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					_initChannel(ch);
				}
			});
			break;
		case UDT:
			group = new NioEventLoopGroup(1, new DefaultThreadFactory("connect"), NioUdtProvider.BYTE_PROVIDER);
			bootstrap.group(group).channelFactory(NioUdtProvider.BYTE_CONNECTOR)
					.handler(new ChannelInitializer<UdtChannel>() {
						@Override
						public void initChannel(final UdtChannel ch) throws Exception {
							_initChannel(ch);
						}
					});
			break;
		case UDP:
			throw new UnsupportedTypeException("UDP protocol is not supported right now");
		}
		// bootstrap.group(group);
		// Start the client.
		channelFuture = bootstrap.connect(host, port);
		try {
			if (channelFuture.await().isSuccess()) {
				this.socketSender = new NettySocketSender(channelFuture.channel());
			} else {
				throw new IOException("Connect to " + host + ":" + port + " fail");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void send(PuElement message) {
		this.send(message, false);
	}

	public void send(PuElement message, boolean sync) {
		this.socketSender.send(message, sync);
	}

	void connectionEstablished() {
		this.dispatchEvent(new SocketEvent(SocketEvent.CONNECTED));
	}

	void connectionClosed() {
		this.dispatchEvent(new SocketEvent(SocketEvent.DISCONNECTED));
	}

	void receive(PuElement msg) {
		this.dispatchEvent(new SocketEvent(SocketEvent.MESSAGE, msg));
	}

	public boolean isConnected() {
		return this.channelFuture != null && this.channelFuture.channel().isActive();
	}

	public boolean isUseLengthPrepender() {
		return useLengthPrepender;
	}

	public void setUseLengthPrepender(boolean useLengthPrepender) {
		this.useLengthPrepender = useLengthPrepender;
	}

	@Override
	public void close() throws Exception {
		if (this.isConnected()) {
			this.channelFuture.channel().close();
		}
		if (this.group != null) {
			this.group.shutdownGracefully();
		}
	}

	public TransportProtocol getProtocol() {
		return protocol;
	}

	public void setProtocol(TransportProtocol protocol) {
		this.protocol = protocol;
	}

}
