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
	private ZMQSocketOptions sendSocketOptions;
	private ZMQSocketType sendSocketType = ZMQSocketType.PUSH_CONNECT;
	private ZMQSocketWriter socketWriter = ZMQSocketWriter.newDefaultWriter();
	private boolean respondedCountEnabled = false;

	private String receiveEndpoint;
	private int receiveWorkerSize = 1;
	private ZMQSocketType receiveSocketType = ZMQSocketType.PULL_BIND;
	private boolean receivedCountEnabled = false;

	private ZMQMessageProcessor messageProcessor = ZMQMessageProcessor.DEBUG_MESSAGE_PROCESSOR;
}
