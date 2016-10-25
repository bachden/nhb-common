package nhb.common.data.exception;

public class InvalidTypeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidTypeException(String message) {
		super(message);
	}

	public InvalidTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidTypeException(Throwable cause) {
		super(cause);
	}
}
