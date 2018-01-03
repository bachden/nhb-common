package com.nhb.messaging.zmq;

/**
 * Use to create new socket to connect to predefine address
 * 
 * @author bachden
 *
 */
public class ZMQSocketFactory {

	private final ZMQSocketRegistry registry;
	private final String address;
	private final ZMQSocketType type;

	public ZMQSocketFactory(ZMQSocketRegistry registry, String address, ZMQSocketType type) {
		if (!type.isClient()) {
			throw new IllegalArgumentException("Factory cannot be used with server socket type (using bind)");
		}

		this.registry = registry;
		this.address = address;
		this.type = type;
	}

	public ZMQSocket newSocket() {
		return this.registry.openSocket(this.address, this.type);
	}
}
