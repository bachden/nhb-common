package com.nhb.messaging.zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ZMQReceivedMessageHandler {

	static ZMQReceivedMessageHandler IGNORE_RECEIVED_DATA_HANDLER = new ZMQReceivedMessageHandler() {

		@Override
		public void onReceive(ZMQEvent message) {
			// do nothing
		}
	};

	static ZMQReceivedMessageHandler DEBUG_RECEIVED_DATA_HANDLER = new ZMQReceivedMessageHandler() {

		private final Logger logger = LoggerFactory.getLogger("ZMQReceivedMessageHandler.PRINT_RECEIVED_DATA_HANDLER");

		@Override
		public void onReceive(ZMQEvent event) {
			logger.debug("[RECEIVED] <<< {}", event.getData());
		}
	};

	void onReceive(ZMQEvent message);
}