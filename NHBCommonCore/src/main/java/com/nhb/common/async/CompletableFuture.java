package com.nhb.common.async;

public interface CompletableFuture<V> {

	void setFailedCause(Throwable cause);

	void setAndDone(V data);
}
