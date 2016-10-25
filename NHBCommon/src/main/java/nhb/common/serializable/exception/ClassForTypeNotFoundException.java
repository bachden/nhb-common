package nhb.common.serializable.exception;

public class ClassForTypeNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClassForTypeNotFoundException(String message) {
		super(message);
	}

	public ClassForTypeNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClassForTypeNotFoundException(Throwable cause) {
		super(cause);
	}
}
