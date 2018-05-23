package com.nhb.messaging.zmq.producer;

import com.lmax.disruptor.EventFactory;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuElement;

class ZeroMQRequest {

	public static final EventFactory<ZeroMQRequest> EVENT_FACTORY = new EventFactory<ZeroMQRequest>() {

		@Override
		public ZeroMQRequest newInstance() {
			return new ZeroMQRequest();
		}
	};

	byte[] rawData;
	PuArray metadata;
	PuElement data;

	DefaultZeroMQRPCFuture future;

	boolean success;
	Throwable failedCause;

	public void clear() {
		this.rawData = null;
		this.metadata = null;
		this.data = null;
		this.future = null;
		this.success = false;
		this.failedCause = null;
	}
}