package com.nhb.messaging.zmq;

import java.util.Collection;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ZMQSocketOptions {

	@Builder.Default
	private int minPort = -1;

	@Builder.Default
	private int maxPort = -1;

	private Collection<byte[]> topics;

	@Builder.Default
	private long pubSubSleepingTime = 200;
}
