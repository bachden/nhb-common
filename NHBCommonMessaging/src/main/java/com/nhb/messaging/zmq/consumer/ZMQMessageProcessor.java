package com.nhb.messaging.zmq.consumer;

import com.nhb.common.async.CompletableFuture;
import com.nhb.common.data.PuElement;

public interface ZMQMessageProcessor {

	void process(PuElement data, CompletableFuture<PuElement> future);
}
