package com.nhb.messaging.zmq.threadlocal;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.ZMQSocketType;

import lombok.Getter;

@Getter
public class ZMQSocketThreadLocal extends ThreadLocal<ZMQ.Socket> {

	private final ZMQSocketRegistry socketRegistry;
	private final String address;
	private final ZMQSocketType type;

	public ZMQSocketThreadLocal(ZMQSocketRegistry socketRegistry, String address, ZMQSocketType type) {
		this.socketRegistry = socketRegistry;
		this.address = address;
		this.type = type;
	}

	@Override
	protected Socket initialValue() {
		return this.socketRegistry.openSocket(getAddress(), getType());
	}

}
