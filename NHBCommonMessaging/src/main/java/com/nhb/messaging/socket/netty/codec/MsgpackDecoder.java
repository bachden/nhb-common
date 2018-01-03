package com.nhb.messaging.socket.netty.codec;

import java.io.EOFException;
import java.util.List;

import org.msgpack.MessagePack;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhb.common.Loggable;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.msgpkg.PuElementTemplate;
import com.nhb.common.exception.UnsupportedTypeException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MsgpackDecoder extends ByteToMessageDecoder implements Loggable {

	public static final MsgpackDecoder newInstance() {
		return new MsgpackDecoder();
	}

	@Override
	public Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}

	@Override
	public Logger getLogger(String name) {
		return LoggerFactory.getLogger(name);
	}

	private final MessagePack msgpack;

	private MsgpackDecoder() {
		this.msgpack = new MessagePack();
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		in.markReaderIndex();
		ByteBufInputStream stream = new ByteBufInputStream(in);
		try {
			Unpacker unpacker = msgpack.createUnpacker(stream);
			PuElement v = PuElementTemplate.getInstance().read(unpacker, null);
			out.add(v);
		} catch (EOFException e) {
			in.resetReaderIndex();
		} catch (UnsupportedTypeException ex) {
			// in.resetReaderIndex();
			// ignore this error
		}
	}
}
