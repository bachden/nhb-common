package nhb.messaging.http.producer;

import nhb.common.data.PuElement;
import nhb.common.data.PuObjectRO;
import nhb.common.exception.UnsupportedTypeException;
import nhb.messaging.http.HttpAsyncFuture;

public class HttpAsyncMessageProducer extends HttpMessageProducer<HttpAsyncFuture> {

	@Override
	public HttpAsyncFuture publish(PuElement data) {
		if (data == null || data instanceof PuObjectRO) {
			switch (this.getMethod()) {
			case GET:
				return this.executeAsyncGet((PuObjectRO) data);
			case POST:
				return this.executeAsyncPost((PuObjectRO) data);
			default:
				throw new UnsupportedTypeException(
						"Method type " + this.getMethod() + " is not supported in " + this.getClass().getSimpleName());
			}
		}
		throw new IllegalArgumentException("Http message producer only allow message instanceof PuObjectRO");
	}
}
