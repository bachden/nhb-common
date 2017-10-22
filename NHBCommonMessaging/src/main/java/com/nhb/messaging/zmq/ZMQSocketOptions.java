package com.nhb.messaging.zmq;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ZMQSocketOptions {

	@Builder.Default
	private int minPort = -1;

	@Builder.Default
	private int maxPort = -1;
}
