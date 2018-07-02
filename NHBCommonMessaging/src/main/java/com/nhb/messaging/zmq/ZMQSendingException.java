package com.nhb.messaging.zmq;

public class ZMQSendingException extends RuntimeException {

	public ZMQSendingException() {
		super();
	}

	public ZMQSendingException(String string) {
		super(string);
	}

	public ZMQSendingException(String string, Throwable cause) {
		super(string, cause);
	}

	public ZMQSendingException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = 1762232187103847844L;
}
