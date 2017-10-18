package com.nhb.messaging.zmq.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.msgpack.annotation.NotNullable;
import org.zeromq.ZMQ.Socket;

import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.ZMQSocketType;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ZMQSocketPooledObjectFactory extends BasePooledObjectFactory<Socket> {

	@NotNullable
	private final ZMQSocketRegistry socketRegistry;
	@NotNullable
	private final String address;
	@NotNullable
	private final ZMQSocketType socketType;

	@Override
	public Socket create() throws Exception {
		return this.socketRegistry.openSocket(address, socketType);
	}

	@Override
	public PooledObject<Socket> wrap(Socket obj) {
		return new PooledZMQSocket(obj);
	}

	@Override
	public void destroyObject(PooledObject<Socket> p) throws Exception {
		p.getObject().close();
	}
}
