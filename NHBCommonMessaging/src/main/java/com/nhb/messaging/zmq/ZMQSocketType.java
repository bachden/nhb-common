package com.nhb.messaging.zmq;

import org.zeromq.ZMQ;

import lombok.Getter;

public enum ZMQSocketType {

	REQ(ZMQ.REQ, true),
	REP(ZMQ.REP),
	XREP(ZMQ.REP, true),
	PUB(ZMQ.PUB, true),
	SUB(ZMQ.SUB),
	DEALER(ZMQ.DEALER),
	ROUTER(ZMQ.ROUTER),
	PUSH(ZMQ.PUSH, true),
	PULL(ZMQ.PULL),
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
}
