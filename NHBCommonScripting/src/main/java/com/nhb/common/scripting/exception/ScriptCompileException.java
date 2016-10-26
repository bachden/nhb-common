package com.nhb.common.scripting.exception;

public class ScriptCompileException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ScriptCompileException() {

	}

	public ScriptCompileException(String message) {
		super(message);
	}

	public ScriptCompileException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptCompileException(Throwable cause) {
		super(cause);
	}
}
