package nhb.common.async;

public interface RPCCallback<CallbackType> {

	void setCallback(Callback<CallbackType> callable);

	Callback<CallbackType> getCallback();
}
