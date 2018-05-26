package com.nhb.messaging.zmq;

import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuObject;

public interface ZMQPayloadBuilder {

	void buildPayload(ZMQEvent event);

	static ZMQPayloadBuilder defaultPuArrayPayloadBuilder() {
		return new ZMQPayloadBuilder() {

			@Override
			public void buildPayload(ZMQEvent event) {
				PuArray payload = new PuArrayList();
				payload.addFrom(event.getData());
				event.setPayload(payload);
			}
		};
	}

	static ZMQPayloadBuilder defaultPuObjectPayloadBuilder() {
		return new ZMQPayloadBuilder() {

			@Override
			public void buildPayload(ZMQEvent event) {
				PuObject payload = new PuObject();
				payload.set("data", event.getData());
				event.setPayload(payload);
			}
		};
	}
}
