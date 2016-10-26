package com.nhb.common.exception;

public class InvalidFormatException extends RuntimeException {

	private static final long serialVersionUID = 334375332800942195L;

	public InvalidFormatException() {
		super();
	}

	public InvalidFormatException(String message) {
		super(message);
	}
}
