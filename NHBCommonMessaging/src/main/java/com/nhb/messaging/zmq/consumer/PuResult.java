package com.nhb.messaging.zmq.consumer;

import com.nhb.common.data.PuDummy;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuValue;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
class PuResult extends PuDummy {

	private static final long serialVersionUID = 1L;

	private boolean success;
	private byte[] messageId;
	private PuElement result;

	public PuResult(byte[] messageId, String message) {
		this.messageId = messageId;
		this.success = false;
		this.result = PuValue.fromObject(message);
	}

	public PuResult(byte[] messageId, PuElement result) {
		this.messageId = messageId;
		this.success = true;
		this.result = result;
	}
}