package com.nhb.messaging.zmq.pool;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.nhb.messaging.zmq.ZMQSocket;
import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.ZMQSocketType;

public class ZMQSocketPool extends GenericObjectPool<ZMQSocket> {

	public ZMQSocketPool(ZMQSocketRegistry socketRegistry, String address, ZMQSocketType socketType) {
		super(new ZMQSocketPooledObjectFactory(socketRegistry, address, socketType));
	}

	public ZMQSocketPool(ZMQSocketRegistry socketRegistry, String address, ZMQSocketType socketType,
			GenericObjectPoolConfig config) {
		super(new ZMQSocketPooledObjectFactory(socketRegistry, address, socketType), config);
	}
}
