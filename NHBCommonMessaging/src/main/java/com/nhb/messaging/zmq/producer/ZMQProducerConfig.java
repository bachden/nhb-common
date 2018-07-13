package com.nhb.messaging.zmq.producer;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.nhb.messaging.zmq.ZMQSocketOptions;
import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.ZMQSocketType;
import com.nhb.messaging.zmq.ZMQSocketWriter;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ZMQProducerConfig {

	private ZMQSocketRegistry socketRegistry;

	private int bufferCapacity = 1024;

	private int queueSize = 1024;
	private String threadNamePattern = "worker-%d";

	private String sendEndpoint;
	private int sendWorkerSize = 1;
	private int sendingDoneHandlerSize = 1;
	private ZMQSocketType sendSocketType = ZMQSocketType.PUSH_CONNECT;
	private ZMQSocketOptions sendSocketOptions;
	private ZMQSocketWriter socketWriter = ZMQSocketWriter.newNonBlockingWriter();
	private boolean sentCountEnabled = false;

	private String receiveEndpoint;
	private int receiveWorkerSize = 1;
	private ZMQSocketType receiveSocketType = ZMQSocketType.PULL_BIND;
	private boolean receivedCountEnable = false;
	private WaitStrategy receiveWaitStrategy = new BlockingWaitStrategy();

}
