package com.nhb.messaging.zmq;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ZMQReceiverConfig {

	private String endpoint;
	private ZMQSocketType socketType;

	@Builder.Default
	private int bufferCapacity = 1024;

	@Builder.Default
	private int queueSize = 1024;

	@Builder.Default
	private String threadNamePattern = "recever-%d";

	@Builder.Default
	private int poolSize = 1;

	private ZMQPayloadExtractor payloadExtractor;

	private ZMQReceivedMessageHandler receivedMessageHandler;

	public void validate() {
		// throw exception to indicate invalid config
	}
}
