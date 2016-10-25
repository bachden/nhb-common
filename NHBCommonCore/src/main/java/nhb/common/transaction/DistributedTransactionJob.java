package nhb.common.transaction;

import java.util.List;

import nhb.common.data.PuObject;

public interface DistributedTransactionJob {
	PuObject getFailureDetails();

	PuObject getResult();

	boolean execute(final PuObject data);

	List<DistributedTransactionTask> getTasks();

	void rollback();
}
