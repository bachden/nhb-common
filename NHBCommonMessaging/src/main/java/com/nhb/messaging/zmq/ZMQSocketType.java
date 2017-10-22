package com.nhb.messaging.zmq;

import org.zeromq.ZMQ;

import lombok.Getter;

public enum ZMQSocketType {

	REQ(ZMQ.REQ, true),
	REP(ZMQ.REP),
	XREP(ZMQ.REP, true),
	PUB(ZMQ.PUB),
	SUB(ZMQ.SUB, true),
	DEALER(ZMQ.DEALER),
	ROUTER(ZMQ.ROUTER),
	PUSH_PRODUCER(ZMQ.PUSH),
	PULL_CONSUMER(ZMQ.PULL, true),
	PUSH_RESPONDER(ZMQ.PUSH, true),
	PULL_RECEIVER(ZMQ.PULL),
	XPUB(ZMQ.XPUB, true),
	XSUB(ZMQ.XSUB),
	STREAM(ZMQ.STREAM);

	@Getter
	private final int flag;

	@Getter
	private final boolean client;

	private ZMQSocketType(int flag) {
		this(flag, false);
	}

	private ZMQSocketType(int flag, boolean client) {
		this.flag = flag;
		this.client = client;
	}

	public static ZMQSocketType fromFlag(int flag) {
		for (ZMQSocketType val : values()) {
			if (flag == val.getFlag()) {
				return val;
			}
		}
		return null;
	}
}
