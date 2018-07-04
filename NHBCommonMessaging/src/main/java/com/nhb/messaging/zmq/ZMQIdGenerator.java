package com.nhb.messaging.zmq;

import com.nhb.common.utils.UUIDUtils;

public interface ZMQIdGenerator {

	static ZMQIdGenerator DEFAULT_TIMEBASED_UUID_GENERATOR = UUIDUtils::timebasedUUIDAsBytes;

	byte[] generateId();
}
