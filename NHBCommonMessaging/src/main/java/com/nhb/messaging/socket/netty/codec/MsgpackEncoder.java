package com.nhb.messaging.socket.netty.codec;

import com.nhb.common.data.PuElement;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MsgpackEncoder extends MessageToByteEncoder<PuElement> {

	public static final MsgpackEncoder newInstance() {
		return new MsgpackEncoder();
	}

	@Override
	public boolean acceptOutboundMessage(Object msg) throws Exception {
		return msg instanceof PuElement;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, PuElement in, ByteBuf out) throws Exception {
		in.writeTo(new ByteBufOutputStream(out));
	}

}
