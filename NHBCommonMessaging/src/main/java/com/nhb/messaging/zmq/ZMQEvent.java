package com.nhb.messaging.zmq;

import com.lmax.disruptor.EventFactory;
import com.nhb.common.data.PuElement;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class ZMQEvent {

	public static final EventFactory<ZMQEvent> EVENT_FACTORY = new EventFactory<ZMQEvent>() {

		@Override
		public ZMQEvent newInstance() {
			return new ZMQEvent();
		}
	};

	private PuElement data;
	private PuElement payload;
	private DefaultZMQFuture future;

	private boolean success;
	private Throwable failedCause;

	public void clear() {
		this.payload = null;
		this.data = null;
		this.future = null;
		this.success = false;
		this.failedCause = null;
	}
}
