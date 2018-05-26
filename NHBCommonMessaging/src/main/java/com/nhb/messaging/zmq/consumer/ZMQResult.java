package com.nhb.messaging.zmq.consumer;

import com.nhb.common.data.PuElement;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ZMQResult {
	
	private byte[] messageId;
	private String responseEndpoint;

	private boolean success;
	private Throwable failedCause;
	private PuElement result;
}
