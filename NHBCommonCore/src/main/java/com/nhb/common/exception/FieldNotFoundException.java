package com.nhb.common.exception;

public class FieldNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -2351468384498699448L;

	public FieldNotFoundException() {
		super();
	}

	public FieldNotFoundException(String message) {
		super(message);
	}
}
