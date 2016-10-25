package nhb.messaging.http.producer;

import java.io.Closeable;
import java.io.IOException;

import nhb.common.data.PuElement;
import nhb.common.data.PuObjectRO;
import nhb.messaging.MessageProducer;
import nhb.messaging.http.HttpAsyncFuture;
import nhb.messaging.http.HttpClientHelper;
import nhb.messaging.http.HttpMethod;

public abstract class HttpMessageProducer<T> implements MessageProducer<T>, Closeable {

	private final HttpClientHelper httpClientHelper = new HttpClientHelper();

	private HttpMethod method = HttpMethod.GET;
	private String endpoint;

	@Override
	public T publish(PuElement data) {
		throw new UnsupportedOperationException("Method doesn't supported");
	}

	@Override
	public T publish(PuElement data, String routingKey) {
		throw new UnsupportedOperationException("Method doesn't supported");
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	protected PuElement executeGet(PuObjectRO params) {
		return HttpClientHelper.handleResponse(this.httpClientHelper.executeGet(getEndpoint(), params));
	}

	protected PuElement executePost(PuObjectRO params) {
		return HttpClientHelper.handleResponse(this.httpClientHelper.executePost(getEndpoint(), params));
	}

	protected HttpAsyncFuture executeAsyncGet(PuObjectRO params) {
		return this.httpClientHelper.executeAsyncGet(getEndpoint(), params);
	}

	protected HttpAsyncFuture executeAsyncPost(PuObjectRO params) {
		return this.httpClientHelper.executeAsyncPost(getEndpoint(), params);
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public void setMethod(String methodName) {
		this.method = HttpMethod.fromName(methodName);
	}

	@Override
	public void close() throws IOException {
		this.httpClientHelper.close();
	}

	public boolean isUsingMultipath() {
		return this.httpClientHelper.isUsingMultipath();
	}

	public void setUsingMultipath(boolean useMultipath) {
		this.httpClientHelper.setUsingMultipath(useMultipath);
	}

	public boolean isFollowRedirect() {
		return this.httpClientHelper.isFollowRedirect();
	}

	public void setFollowRedirect(boolean followRedirect) {
		this.httpClientHelper.setFollowRedirect(followRedirect);
	}
}
