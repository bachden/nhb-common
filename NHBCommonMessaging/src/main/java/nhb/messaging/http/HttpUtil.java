package nhb.messaging.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

public class HttpUtil {
	public static String get(String uri, Map<String, Object> params) throws IOException, ClientProtocolException {
		HttpClient client = HttpClientBuilder.create().build();
		List<NameValuePair> list = new ArrayList<>();
		for (Entry<String, Object> entry : params.entrySet()) {
			list.add(new BasicNameValuePair(entry.getKey(), (String) entry.getValue()));
		}
		String paramString = URLEncodedUtils.format(list, "utf-8");
		uri += paramString;
		HttpGet request = new HttpGet(uri);
		System.out.println("\nSending 'GET' request to URL : " + uri);
		HttpResponse response = client.execute(request);
		BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = in.readLine()) != null) {
			result.append(line);
		}

		in.close();
		System.out.println("Response: " + result.toString());
		return result.toString();
	}

	public static String post(String url, Map<String, Object> params) throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);

		// add header
		List<NameValuePair> urlParamters = new ArrayList<>();
		for (Entry<String, Object> entry : params.entrySet()) {
			urlParamters.add(new BasicNameValuePair(entry.getKey(), (String) entry.getValue()));
		}

		post.setEntity(new UrlEncodedFormEntity(urlParamters, Charset.forName("UTF-8")));
		HttpResponse response = client.execute(post);
		System.out.println("\nSending 'POST' request to URL : " + url);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		System.out.println(result.toString());
		return result.toString();
	}
}
