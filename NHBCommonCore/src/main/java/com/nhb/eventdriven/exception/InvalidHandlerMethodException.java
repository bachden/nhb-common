package com.nhb.eventdriven.exception;

public class InvalidHandlerMethodException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidHandlerMethodException() {

	}

	public InvalidHandlerMethodException(String message) {
		super(message);
	}

	public InvalidHandlerMethodException(String message, Throwable cause) {
		super(message, cause);
	}
}
