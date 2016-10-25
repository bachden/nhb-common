package nhb.common.predicate.exception;

public class TypeNotSupported extends RuntimeException {

	private static final long serialVersionUID = 7895713124575518040L;

	public TypeNotSupported() {
		super();
	}

	public TypeNotSupported(Class<?> clazz) {
		super("Type '" + clazz.getName() + "' not supported");
	}
}
