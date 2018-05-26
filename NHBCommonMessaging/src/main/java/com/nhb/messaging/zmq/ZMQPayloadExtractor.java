package com.nhb.messaging.zmq;

public interface ZMQPayloadExtractor {

	/**
	 * read data and fill info to event
	 * 
	 * @param data
	 * @param event
	 */
	void extractPayload(ZMQEvent event);
}
