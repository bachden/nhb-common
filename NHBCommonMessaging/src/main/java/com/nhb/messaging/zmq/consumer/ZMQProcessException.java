package com.nhb.messaging.zmq.consumer;

public class ZMQProcessException extends Exception {
	private static final long serialVersionUID = 1L;

	public ZMQProcessException() {

	}

	public ZMQProcessException(String message) {
		super(message);
	}

	public ZMQProcessException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZMQProcessException(Throwable cause) {
		super(cause);
	}
}
