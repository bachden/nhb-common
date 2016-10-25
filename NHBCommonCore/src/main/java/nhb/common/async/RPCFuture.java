package nhb.common.async;

import java.util.concurrent.Future;

public interface RPCFuture<V> extends Future<V>, RPCCallback<V> {

	Throwable getFailedCause();
}
