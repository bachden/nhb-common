package com.nhb.messaging.http;

import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;

public interface HttpAsyncFuture extends RPCFuture<HttpResponse> {

	PuElement getPuElement() throws InterruptedException, ExecutionException;
}
