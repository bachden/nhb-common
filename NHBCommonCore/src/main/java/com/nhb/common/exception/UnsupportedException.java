package com.nhb.common.exception;

public class UnsupportedException extends RuntimeException {

	private static final long serialVersionUID = 6158754352658066075L;

	public UnsupportedException() {
		super();
	}

	public UnsupportedException(String message) {
		super(message);
	}

	public UnsupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedException(Throwable cause) {
		super(cause);
	}
}
