package com.nhb.messaging.zmq;

import com.nhb.common.data.PuElement;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class ZMQEvent {

	private byte[] messageId;
	private String responseEndpoint;

	private PuElement data;
	private PuElement payload;
	private DefaultZMQFuture future;

	private boolean success = true;
	private Throwable failedCause;

	public void clear() {
		this.messageId = null;
		this.payload = null;
		this.data = null;
		this.future = null;
		this.success = true;
		this.failedCause = null;
	}
}
