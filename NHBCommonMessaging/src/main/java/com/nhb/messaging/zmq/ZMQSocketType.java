package com.nhb.messaging.zmq;

import org.zeromq.ZMQ;

import lombok.Getter;

public enum ZMQSocketType {

	REQ_BIND(ZMQ.REQ),
	REQ_CONNECT(ZMQ.REQ, true),

	REP_BIND(ZMQ.REP),
	REP_CONNECT(ZMQ.REP, true),

	PUB_BIND(ZMQ.PUB),
	PUB_CONNECT(ZMQ.XPUB, true),

	SUB_BIND(ZMQ.SUB),
	SUB_CONNECT(ZMQ.SUB, true),

	XPUB_BIND(ZMQ.XPUB),
	XPUB_CONNECT(ZMQ.XPUB, true),

	XSUB_BIND(ZMQ.XSUB),
	XSUB_CONNECT(ZMQ.XSUB, true),

	DEALER_BIND(ZMQ.DEALER),
	DEALER_CONNECT(ZMQ.DEALER, true),

	ROUTER_BIND(ZMQ.ROUTER),
	ROUTER_CONNECT(ZMQ.ROUTER, true),

	PUSH_BIND(ZMQ.PUSH),
	PUSH_CONNECT(ZMQ.PUSH, true),

	PULL_BIND(ZMQ.PULL),
	PULL_CONNECT(ZMQ.PULL, true),

	STREAM_BIND(ZMQ.STREAM),
	STREAM_CONNECT(ZMQ.STREAM, true);

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
