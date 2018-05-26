package com.nhb.messaging.zmq;

import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuValue;
import com.nhb.common.data.exception.InvalidDataException;

public interface ZMQPayloadExtractor {

	static ZMQPayloadExtractor DEFAULT_PUARRAY_PAYLOAD_EXTRACTOR = new ZMQPayloadExtractor() {

		@Override
		public void extractPayload(ZMQEvent event) {
			if (event != null) {
				PuElement payload = event.getPayload();
				if (payload instanceof PuArray) {
					PuArray arr = (PuArray) payload;
					if (arr.size() > 0) {
						PuValue value = arr.get(0);
						if (value.getType() == PuDataType.PUARRAY) {
							event.setData(value.getPuArray());
						} else if (value.getType() == PuDataType.PUOBJECT) {
							event.setData(value.getPuObject());
						} else {
							event.setData(value);
						}
						return;
					}
					throw new InvalidDataException("PuArray payload must have atleast 1 element");
				}
				throw new InvalidDataException("Payload must be instance of PuArray");
			}
			throw new NullPointerException("Cannot extract payload from null event");
		}
	};

	/**
	 * read data and fill info to event
	 * 
	 * @param data
	 * @param event
	 */
	void extractPayload(ZMQEvent event);
}
