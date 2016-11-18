package com.nhb.common.async;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface RPCFuture<V> extends Future<V>, RPCCallback<V> {

	Throwable getFailedCause();

	void setTimeout(long value, TimeUnit timeUnit);
}
