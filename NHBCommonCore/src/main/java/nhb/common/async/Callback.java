package nhb.common.async;
public interface Callback<T> {

	void apply(T result);
}
