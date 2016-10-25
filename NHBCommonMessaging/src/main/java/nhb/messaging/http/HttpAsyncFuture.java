package nhb.messaging.http;

import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;

import nhb.common.async.RPCFuture;
import nhb.common.data.PuElement;

public interface HttpAsyncFuture extends RPCFuture<HttpResponse> {

	PuElement getPuElement() throws InterruptedException, ExecutionException;
}
