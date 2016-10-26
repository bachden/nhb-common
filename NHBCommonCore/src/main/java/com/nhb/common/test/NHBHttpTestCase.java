package com.nhb.common.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.vo.HostAndPort;

public abstract class NHBHttpTestCase extends NHBTestCase {

	protected final HttpClient httpClient = HttpClients.createDefault();

	private HostAndPort endpoint = null;
	private String path = "/";

	protected String getPath() {
		return path;
	}

	protected void setPath(String path) {
		this.path = path;
	}

	protected String getDefaultHttpEndpoint() {
		if (this.endpoint == null) {
			return null;
		}
		return "http://" + endpoint.getHost() + (endpoint.getPort() == 80 ? "" : (":" + endpoint.getPort()))
				+ (getPath() == null ? "" : getPath());
	}

	protected void setEndpoint(String host, int port) {
		if (endpoint == null) {
			endpoint = new HostAndPort();
		}
		endpoint.setHost(host);
		endpoint.setPort(port);
	}

	protected HttpResponse executeGet(String uri) {
		return executeGet(uri, null);
	}

	private HttpResponse execute(RequestBuilder builder, PuObjectRO params) {
		try {
			if (params != null) {
				for (Entry<String, PuValue> entry : params) {
					builder.addParameter(entry.getKey(), entry.getValue().getString());
				}
			}
			getLogger().info("\n------- REQUEST -------\nURI: {}\nPARAMS: {}\n-----------------------",
					builder.getUri().toString(), params);
			return httpClient.execute(builder.build());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected HttpResponse executeGet(String uri, PuObjectRO params) {
		try {
			return execute(RequestBuilder.get().setUri(new URI(uri)), params);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error while creating URI instance", e);
		}
	}

	protected HttpResponse executePost(String uri, PuObjectRO params) {
		try {
			return execute(RequestBuilder.post().setUri(new URI(uri)), params);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error while creating URI instance", e);
		}
	}

	protected PuElement handleResponse(HttpResponse response) {
		PuElement result = null;
		if (response != null) {
			try {
				String responseText = EntityUtils.toString(response.getEntity());
				if (responseText != null) {
					try {
						result = PuObject.fromJSON(responseText);
					} catch (Exception ex) {
						result = new PuValue(responseText);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException("Error while consuming response entity", e);
			}
		}
		getLogger().debug("\n------ RESPONSE ------\nStatus: {}\nContent: {}\n----------------------",
				response.getStatusLine(), result);
		return result;
	}

	protected PuElement executeGetOnDefaultEndpoint(Object... params) {
		PuObject request = PuObject.fromObject(new MapTuple<>(params));
		HttpResponse response = executeGet(getDefaultHttpEndpoint(), request);
		return handleResponse(response);
	}

	protected PuElement executeGetWithCommandOnDefaultEndpoint(String command, Object... params) {
		PuObject request = createRequestWithCommand(command);
		request.addAll(PuObject.fromObject(new MapTuple<>(params)));
		HttpResponse response = executeGet(getDefaultHttpEndpoint(), request);
		return handleResponse(response);
	}

	protected PuElement executePostOnDefaultEndpoint(Object... params) {
		PuObject request = PuObject.fromObject(new MapTuple<>(params));
		HttpResponse response = executePost(getDefaultHttpEndpoint(), request);
		return handleResponse(response);
	}

	protected PuElement executePostWithCommandOnDefaultEndpoint(String command, Object... params) {
		PuObject request = createRequestWithCommand(command);
		request.addAll(PuObject.fromObject(new MapTuple<>(params)));
		HttpResponse response = executePost(getDefaultHttpEndpoint(), request);
		return handleResponse(response);
	}

	protected PuObject createRequestWithCommand(String command) {
		return PuObject.fromObject(new MapTuple<>("command", command));
	}
}
