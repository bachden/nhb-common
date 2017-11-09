package nhb.common.messaging.test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpResponse;

import com.nhb.common.async.Callback;
import com.nhb.messaging.http.HttpAsyncFuture;
import com.nhb.messaging.http.HttpClientHelper;
import com.nhb.messaging.http.producer.HttpAsyncMessageProducer;

public class TestAsynHttp {

	public static void main(String[] args) throws InterruptedException, IOException {

		final CountDownLatch doneSignal = new CountDownLatch(2);

		HttpAsyncMessageProducer asyncMessageProducer = new HttpAsyncMessageProducer();
		asyncMessageProducer.setMethod("get");

		asyncMessageProducer.setEndpoint("http://latte.lozi.vn/v1.2/newsfeed/merchants?t=popular&cityId=50&districtId=-1&cats=24");
		HttpAsyncFuture future = asyncMessageProducer.publish(null);

		future.setCallback(new Callback<HttpResponse>() {

			@Override
			public void apply(HttpResponse result) {
				System.out.println(HttpClientHelper.handleResponse(result).toString().trim().substring(0, 10000));
				System.out.println("****************************************************************");
				doneSignal.countDown();
			}
		});

		asyncMessageProducer.setEndpoint("https://www.adayroi.com");
		asyncMessageProducer.publish(null).setCallback(new Callback<HttpResponse>() {

			@Override
			public void apply(HttpResponse result) {
				System.out.println(HttpClientHelper.handleResponse(result).toString().trim().substring(0, 1000));
				System.out.println("****************************************************************");
				System.out.println("****************************************************************");
				doneSignal.countDown();
			}
		});

		doneSignal.await();
		asyncMessageProducer.close();
	}

}
