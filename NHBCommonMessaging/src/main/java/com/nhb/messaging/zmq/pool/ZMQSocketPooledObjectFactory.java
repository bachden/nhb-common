package com.nhb.messaging.zmq.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.msgpack.annotation.NotNullable;

import com.nhb.messaging.zmq.ZMQSocket;
import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.ZMQSocketType;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ZMQSocketPooledObjectFactory extends BasePooledObjectFactory<ZMQSocket> {

	@NotNullable
	private final ZMQSocketRegistry socketRegistry;
	@NotNullable
	private final String address;
	@NotNullable
	private final ZMQSocketType socketType;

	@Override
	public ZMQSocket create() throws Exception {
		return this.socketRegistry.openSocket(address, socketType);
	}

	@Override
	public PooledObject<ZMQSocket> wrap(ZMQSocket obj) {
		return new PooledZMQSocket(obj);
	}

	@Override
	public void destroyObject(PooledObject<ZMQSocket> p) throws Exception {
		p.getObject().close();
	}
}
