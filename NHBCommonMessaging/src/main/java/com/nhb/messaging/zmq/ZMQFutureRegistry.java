package com.nhb.messaging.zmq;

import java.util.Map;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import com.nhb.common.vo.ByteArrayWrapper;

public class ZMQFutureRegistry {

	private final Map<ByteArrayWrapper, DefaultZMQFuture> registry = new NonBlockingHashMap<>();

	private ByteArrayWrapper wrap(byte[] messageId) {
		return ByteArrayWrapper.newInstanceWithJavaSafeHashCodeCalculator(messageId);
	}

	public void put(byte[] key, DefaultZMQFuture future) {
		this.registry.put(wrap(key), future);
	}

	public DefaultZMQFuture remove(byte[] key) {
		return this.registry.remove(wrap(key));
	}

	public boolean containsKey(byte[] key) {
		return this.registry.containsKey(wrap(key));
	}

	public DefaultZMQFuture get(byte[] key) {
		return this.registry.get(wrap(key));
	}

	public int remaining() {
		return this.registry.size();
	}
}