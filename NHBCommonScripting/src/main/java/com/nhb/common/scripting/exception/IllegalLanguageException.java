package com.nhb.common.scripting.exception;

public class IllegalLanguageException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IllegalLanguageException() {

	}

	public IllegalLanguageException(String message) {
		super(message);
	}

	public IllegalLanguageException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalLanguageException(Throwable cause) {
		super(cause);
	}
}
