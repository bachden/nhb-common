package com.nhb.messaging.http;

import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;

public interface HttpAsyncFuture extends RPCFuture<HttpResponse> {

	PuElement getPuElement() throws InterruptedException, ExecutionException;

	HttpUriRequest getRequest();

	HttpContext getContext();
}
