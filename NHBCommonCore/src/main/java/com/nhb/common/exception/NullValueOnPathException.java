package com.nhb.common.exception;

public class NullValueOnPathException extends RuntimeException {

	private static final long serialVersionUID = 8637723187478551681L;

	public NullValueOnPathException(String message) {
		super(message);
	}

	public NullValueOnPathException(String message, Throwable cause) {
		super(message, cause);
	}

	public NullValueOnPathException(Throwable cause) {
		super(cause);
	}
}