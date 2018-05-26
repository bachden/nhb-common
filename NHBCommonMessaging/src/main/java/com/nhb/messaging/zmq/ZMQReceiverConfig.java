package com.nhb.messaging.zmq;

import com.nhb.common.data.exception.InvalidDataException;

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
		if (endpoint == null) {
			throw new NullPointerException("Endpoint cannot be null");
		} else if (socketType == null) {
			throw new NullPointerException("Socket type cannot be null");
		} else if (bufferCapacity <= 0) {
			throw new InvalidDataException("buffer capacity must be positive");
		} else if (queueSize <= 0 || Integer.bitCount(queueSize) != 1) {
			throw new InvalidDataException("queueSize must be positive and is power of 2");
		} else if (poolSize <= 0) {
			throw new InvalidDataException("poolSize must be positive");
		} else if (payloadExtractor == null) {
			throw new NullPointerException("payload extractor cannot be null");
		} else if (receivedMessageHandler == null) {
			throw new NullPointerException("receivedMessageHandler cannot be null");
		}
	}
}
