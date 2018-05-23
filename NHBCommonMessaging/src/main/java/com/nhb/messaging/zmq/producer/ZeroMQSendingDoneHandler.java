package com.nhb.messaging.zmq.producer;

interface ZeroMQSendingDoneHandler {

	void onSendingDone(ZeroMQRequest request);
}
