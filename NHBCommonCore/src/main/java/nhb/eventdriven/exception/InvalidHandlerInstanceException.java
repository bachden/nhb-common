package nhb.eventdriven.exception;

public class InvalidHandlerInstanceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidHandlerInstanceException() {

	}

	public InvalidHandlerInstanceException(String message) {
		super(message);
	}

	public InvalidHandlerInstanceException(String message, Throwable cause) {
		super(message, cause);
	}
}
