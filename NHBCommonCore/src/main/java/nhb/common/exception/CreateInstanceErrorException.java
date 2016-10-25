package nhb.common.exception;

public class CreateInstanceErrorException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CreateInstanceErrorException(String message) {
		super(message);
	}

	public CreateInstanceErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public CreateInstanceErrorException(Throwable cause) {
		super(cause);
	}
}
