package nhb.common.transaction;

import nhb.common.data.PuObject;

public interface DistributedTransactionTask {

	PuObject getFailureDetails();

	boolean isSuccessful();

	void execute(PuObject data);

	void rollback();
	
	DistributedTransactionJob getJob();
}
