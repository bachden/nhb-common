package com.nhb.messaging.socket.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhb.common.Loggable;
import com.nhb.common.data.PuElement;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

class NettySocketSession extends ChannelInboundHandlerAdapter implements Loggable {

	private NettySocketClient client;

	public NettySocketSession(NettySocketClient client) {
		this.client = client;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		this.client.receive((PuElement) msg);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.client.connectionEstablished();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		this.client.connectionClosed();
	}

	@Override
	public Logger getLogger() {
		return LoggerFactory.getLogger(this.getClass());
	}

	public Logger getLogger(String name) {
		return LoggerFactory.getLogger(name);
	}
}
