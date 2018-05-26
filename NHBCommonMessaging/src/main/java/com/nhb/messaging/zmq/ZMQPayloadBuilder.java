package com.nhb.messaging.zmq;

import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuObject;

public interface ZMQPayloadBuilder {

	void buildPayload(ZMQEvent event);

	static ZMQPayloadBuilder DEFAULT_PUARRAY_PAYLOAD_BUILDER = new ZMQPayloadBuilder() {

		@Override
		public void buildPayload(ZMQEvent event) {
			if (event != null) {
				PuArray payload = new PuArrayList();
				payload.addFrom(event.getData());
				event.setPayload(payload);
			}
		}
	};

	static ZMQPayloadBuilder DEFAULT_PUOBJECT_PAYLOAD_BUILDER = new ZMQPayloadBuilder() {

		@Override
		public void buildPayload(ZMQEvent event) {
			if (event != null) {
				PuObject payload = new PuObject();
				payload.set("data", event.getData());
				event.setPayload(payload);
			}
		}
	};
}
