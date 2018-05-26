package com.nhb.messaging.zmq;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ZMQSenderConfig {

	@Builder.Default
	private String threadNamePattern = "sender-%d";

	@Builder.Default
	private int sendWorkerSize = 1;

	@Builder.Default
	private int sendingDoneHandlerSize = 1;

	@Builder.Default
	private int queueSize = 1024;

	@Builder.Default
	private int bufferCapacity = 1024;

	private String endpoint;

	private ZMQSocketType socketType;

	@Builder.Default
	private final ZMQSocketOptions socketOptions = ZMQSocketOptions.builder().build();

	private ZMQPayloadBuilder payloadBuilder;

	private ZMQSendingDoneHandler sendingDoneHandler;

	public void validate() {
		// throw exception if config is invalid
	}
}
