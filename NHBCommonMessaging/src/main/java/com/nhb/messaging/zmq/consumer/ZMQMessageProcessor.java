package com.nhb.messaging.zmq.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhb.common.async.CompletableFuture;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuValue;

public interface ZMQMessageProcessor {

	static ZMQMessageProcessor DEBUG_MESSAGE_PROCESSOR = new ZMQMessageProcessor() {
		private final Logger logger = LoggerFactory.getLogger("ZMQMessageProcessor.DEBUG_MESSAGE_PROCESSOR");

		@Override
		public void process(PuElement data, CompletableFuture<PuElement> future) {
			logger.debug("Processing message: {}", data);
			future.setAndDone(PuValue.fromObject("message processed"));
		}
	};

	void process(PuElement data, CompletableFuture<PuElement> future);
}
