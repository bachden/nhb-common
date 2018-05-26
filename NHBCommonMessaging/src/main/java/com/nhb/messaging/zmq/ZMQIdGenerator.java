package com.nhb.messaging.zmq;

import com.nhb.common.utils.UUIDUtils;

public interface ZMQIdGenerator {

	static ZMQIdGenerator newDefault() {
		return new ZMQIdGenerator() {

			@Override
			public byte[] generateId() {
				return UUIDUtils.timebasedUUIDAsBytes();
			}
		};
	}

	byte[] generateId();
}
