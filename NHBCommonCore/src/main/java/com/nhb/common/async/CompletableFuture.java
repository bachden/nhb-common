package com.nhb.common.async;

public interface CompletableFuture<V> {

    @Deprecated
	void setFailedCause(Throwable cause);
	
	void setFailedAndDone(Throwable cause);

	void setAndDone(V data);
}
