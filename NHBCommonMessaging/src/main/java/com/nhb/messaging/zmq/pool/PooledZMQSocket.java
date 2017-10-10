package com.nhb.messaging.zmq.pool;

import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.zeromq.ZMQ.Socket;

public class PooledZMQSocket extends DefaultPooledObject<Socket> {

	public PooledZMQSocket(Socket socket) {
		super(socket);
	}
}
