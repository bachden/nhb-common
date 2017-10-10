package com.nhb.messaging.zmq.pool;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.zeromq.ZMQ.Socket;

import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.ZMQSocketType;

public class ZMQSocketPool extends GenericObjectPool<Socket> {

	public ZMQSocketPool(ZMQSocketRegistry socketRegistry, String address, ZMQSocketType socketType) {
		super(new ZMQSocketPooledObjectFactory(socketRegistry, address, socketType));
	}
}
