package com.nhb.messaging.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.util.EntityUtils;

import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.data.PuXmlHelper;

public class HttpClientHelper extends BaseLoggable implements Closeable {

	private CloseableHttpClient httpClient;
	private CloseableHttpAsyncClient httpAsyncClient;
	private boolean usingMultipath = true;
	private boolean followRedirect = true;

	private HttpClient getSyncClient() {
		if (this.httpClient == null) {
			synchronized (this) {
				if (this.httpClient == null) {
					if (this.isFollowRedirect()) {
						this.httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy())
								.build();
					} else {
						this.httpClient = HttpClients.createDefault();
					}
				}
			}
		}
		return this.httpClient;
	}

	private HttpAsyncClient getAsyncClient() {
		if (this.httpAsyncClient == null) {
			synchronized (this) {
				if (this.httpAsyncClient == null) {
					if (this.isFollowRedirect()) {
						this.httpAsyncClient = HttpAsyncClientBuilder.create()
								.setRedirectStrategy(new LaxRedirectStrategy()).build();
					} else {
						this.httpAsyncClient = HttpAsyncClients.createDefault();
					}
					((CloseableHttpAsyncClient) this.httpAsyncClient).start();
				}
			}
		}
		return this.httpAsyncClient;
	}

	public HttpAsyncFuture executeAsync(RequestBuilder builder, PuObjectRO params) {
		if (params != null) {
			if (builder.getMethod().equalsIgnoreCase("get") || this.isUsingMultipath()) {
				for (Entry<String, PuValue> entry : params) {
					builder.addParameter(entry.getKey(), entry.getValue().getString());
				}
			} else {
				String json = params.toJSON();
				getLogger().debug("Sending using Json body");
				builder.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
				builder.addHeader("Content-Type", "application/json");
			}
		}
		getLogger().debug("\n------- " + builder.getMethod() + " -------\nURI: {}\nPARAMS: {}\n-----------------------",
				builder.getUri().toString(), params != null ? params : params);
		HttpAsyncFutureImpl future = new HttpAsyncFutureImpl();
		Future<HttpResponse> cancelFuture = getAsyncClient().execute(builder.build(), future);
		future.setCancelFuture(cancelFuture);
		return future;
	}

	public HttpResponse execute(RequestBuilder builder, PuObjectRO params) {
		try {
			if (params != null) {
				if (builder.getMethod().equalsIgnoreCase("get") || this.isUsingMultipath()) {
					for (Entry<String, PuValue> entry : params) {
						builder.addParameter(entry.getKey(), entry.getValue().getString());
					}
				} else {
					String json = params.toJSON();
					try {
						builder.setEntity(new StringEntity(json));
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException("Unable to send data", e);
					}
				}
			}
			getLogger().info("\n------- REQUEST -------\nURI: {}\nPARAMS: {}\n-----------------------",
					builder.getUri().toString(), params);
			return getSyncClient().execute(builder.build());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public HttpResponse executeGet(String uri, PuObjectRO params) {
		try {
			return execute(RequestBuilder.get().setUri(new URI(uri)), params);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error while creating URI instance", e);
		}
	}

	public HttpResponse executePost(String uri, PuObjectRO params) {
		try {
			return execute(RequestBuilder.post().setUri(new URI(uri)), params);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error while creating URI instance", e);
		}
	}

	public HttpAsyncFuture executeAsyncGet(String uri, PuObjectRO params) {
		try {
			return executeAsync(RequestBuilder.get().setUri(new URI(uri)), params);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error while creating URI instance", e);
		}
	}

	public HttpAsyncFuture executeAsyncPost(String uri, PuObjectRO params) {
		try {
			return executeAsync(RequestBuilder.post().setUri(new URI(uri)), params);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error while creating URI instance", e);
		}
	}

	public static PuElement handleResponse(HttpResponse response) {
		PuElement result = null;
		if (response != null) {
			try {
				String responseText = EntityUtils.toString(response.getEntity(), "utf-8");
				if (responseText != null) {
					responseText = responseText.trim();
					try {
						if (responseText.startsWith("[")) {
							result = PuArrayList.fromJSON(responseText);
						} else if (responseText.startsWith("{")) {
							result = PuObject.fromJSON(responseText);
						} else if (responseText.startsWith("<")) {
							result = PuXmlHelper.parseXml(responseText);
						} else {
							result = new PuValue(responseText, PuDataType.STRING);
						}
					} catch (Exception ex) {
						result = new PuValue(responseText, PuDataType.STRING);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException("Error while consuming response entity", e);
			}
		}
		return result;
	}

	@Override
	public void close() throws IOException {
		if (this.httpAsyncClient != null && this.httpAsyncClient instanceof Closeable) {
			((Closeable) this.httpAsyncClient).close();
		}
		if (this.httpClient != null) {
			((Closeable) this.httpClient).close();
		}
	}

	public boolean isUsingMultipath() {
		return usingMultipath;
	}

	public void setUsingMultipath(boolean usingMultipath) {
		this.usingMultipath = usingMultipath;
	}

	public boolean isFollowRedirect() {
		return followRedirect;
	}

	public void setFollowRedirect(boolean followRedirect) {
		this.followRedirect = followRedirect;
	}
}
