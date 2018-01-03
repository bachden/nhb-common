package com.nhb.messaging.kafka.serialization;

import org.msgpack.MessagePack;

import com.nhb.common.BaseLoggable;

class MsgpackCodec extends BaseLoggable {

	private final MessagePack msgpack = new MessagePack();

	protected MessagePack getMsgpack() {
		return this.msgpack;
	}
}
