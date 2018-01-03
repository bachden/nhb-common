package com.nhb.messaging.zmq.pool;

import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.nhb.messaging.zmq.ZMQSocket;

public class PooledZMQSocket extends DefaultPooledObject<ZMQSocket> {

	public PooledZMQSocket(ZMQSocket socket) {
		super(socket);
	}
}
