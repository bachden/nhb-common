package com.nhb.messaging.zmq.producer;

import com.lmax.disruptor.EventFactory;
import com.nhb.common.data.PuElement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PACKAGE)
class ZeroMQResponse {
	static EventFactory<ZeroMQResponse> EVENT_FACTORY = new EventFactory<ZeroMQResponse>() {

		@Override
		public ZeroMQResponse newInstance() {
			return new ZeroMQResponse();
		}
	};

	byte[] rawData;
	private byte[] messageId;
	private PuElement data;

	boolean error = false;
	Throwable failedCause;

	public void clear() {
		this.rawData = null;
		this.messageId = null;
		this.data = null;
		this.error = false;
		this.failedCause = null;
	}
}