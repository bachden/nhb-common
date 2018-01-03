package com.nhb.common.scripting.exception;

public class ScriptRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ScriptRuntimeException() {

	}

	public ScriptRuntimeException(String message) {
		super(message);
	}

	public ScriptRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptRuntimeException(Throwable cause) {
		super(cause);
	}
}
