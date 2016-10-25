package nhb.common.workflow.exception;

public class TaskNotExecutedException extends Throwable {

	private static final long serialVersionUID = 1L;

	public TaskNotExecutedException() {
		super();
	}

	public TaskNotExecutedException(String message) {
		super(message);
	}

	public TaskNotExecutedException(Throwable cause) {
		super(cause);
	}

	public TaskNotExecutedException(String message, Throwable cause) {
		super(message, cause);
	}
}
