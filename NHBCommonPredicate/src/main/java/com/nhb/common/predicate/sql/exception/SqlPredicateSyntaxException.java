package com.nhb.common.predicate.sql.exception;

public class SqlPredicateSyntaxException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SqlPredicateSyntaxException() {
		super();
	}

	public SqlPredicateSyntaxException(String message) {
		super(message);
	}

	public SqlPredicateSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlPredicateSyntaxException(Throwable cause) {
		super(cause);
	}
}
