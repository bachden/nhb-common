package nhb.common.serializable.exception;

public class ClassDoesNotRegisteredException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClassDoesNotRegisteredException(String message) {
		super(message);
	}

	public ClassDoesNotRegisteredException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClassDoesNotRegisteredException(Throwable cause) {
		super(cause);
	}
}
