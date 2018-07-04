package com.nhb.messaging.zmq;

import com.nhb.common.data.exception.InvalidDataException;

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

	private String endpoint;

	private ZMQSocketType socketType;

	@Builder.Default
	private final ZMQSocketOptions socketOptions = ZMQSocketOptions.builder().build();

	private ZMQPayloadBuilder payloadBuilder;

	private ZMQSendingDoneHandler sendingDoneHandler;

	@Builder.Default
	private ZMQSocketWriter socketWriter = ZMQSocketWriter.newDefaultWriter();

	@Builder.Default
	private ZMQIdGenerator idGenerator = ZMQIdGenerator.DEFAULT_TIMEBASED_UUID_GENERATOR;

	@Builder.Default
	private boolean sentCountEnabled = false;

	public void validate() {
		if (endpoint == null) {
			throw new NullPointerException("Endpoint cannot be null");
		} else if (sendWorkerSize <= 0) {
			throw new InvalidDataException("sendSocketSize cannot be <= 0");
		} else if (sendingDoneHandlerSize <= 0) {
			throw new InvalidDataException("sendingDoneHandlerSize cannot be <= 0");
		} else if (queueSize < 0 || Integer.bitCount(queueSize) != 1) {
			throw new InvalidDataException("queueSize must positive and is power of 2");
		} else if (socketType == null) {
			throw new NullPointerException("Socket type cannot be null");
		} else if (sendingDoneHandler == null) {
			throw new NullPointerException("sendingDoneHandler cannot be null");
		} else if (socketWriter == null) {
			throw new NullPointerException("socketWriter cannot be null");
		}
	}
}
