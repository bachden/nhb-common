package com.nhb.common.async;

public interface CompletableFuture<V> {

	void setAndDone(V data);
}
