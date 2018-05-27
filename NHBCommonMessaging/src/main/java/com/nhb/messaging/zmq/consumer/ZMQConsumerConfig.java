package com.nhb.messaging.zmq.consumer;

import com.nhb.messaging.zmq.ZMQSocketOptions;
import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.ZMQSocketType;
import com.nhb.messaging.zmq.ZMQSocketWriter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZMQConsumerConfig {

	private ZMQSocketRegistry socketRegistry;

	private int bufferCapacity = 1024;

	private String threadNamePattern = "worker-%d";
	private int queueSize = 1024;

	private int sendWorkerSize = 1;
	private int sendingDoneHandlerSize = 1;
	private ZMQSocketType sendSocketType = ZMQSocketType.PUSH_CONNECT;
	private ZMQSocketOptions sendSocketOptions;
	private ZMQSocketWriter socketWriter = ZMQSocketWriter.newDefaultWriter();

	private String receiveEndpoint;
	private ZMQSocketType receiveSocketType = ZMQSocketType.PULL_BIND;
	private int receiveWorkerSize = 1;

	private ZMQMessageProcessor messageProcessor = ZMQMessageProcessor.DEBUG_MESSAGE_PROCESSOR;
}
