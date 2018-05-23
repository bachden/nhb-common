package com.nhb.messaging.zmq.producer;

import com.nhb.messaging.zmq.ZMQSocketType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ZeroMQProducerConfig {

	private String endpoint;
	private String responseEndpoint;

	// random binding options
	private int minPort = -1;
	private int maxPort = -1;

	private ZMQSocketType socketType;

	private int ringBufferSize;
	private String threadNamePattern;

	private int marshallerSize = 2;
	private int sendingDoneHandlerSize = 2;

	// data use for RPC producer
	private int unmarshallerSize = 2;
	private int handlerPoolSize = 1;
}
