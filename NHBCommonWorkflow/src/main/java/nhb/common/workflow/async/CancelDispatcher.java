package nhb.common.workflow.async;

public interface CancelDispatcher {

	void addCancelListener(CancelListener listener);

	void dispatchCancel();
}
