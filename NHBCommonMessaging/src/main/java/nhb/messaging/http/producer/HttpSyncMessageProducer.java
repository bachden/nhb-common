package nhb.messaging.http.producer;

import nhb.common.data.PuElement;
import nhb.common.data.PuObjectRO;
import nhb.common.exception.UnsupportedTypeException;

public class HttpSyncMessageProducer extends HttpMessageProducer<PuElement> {

	@Override
	public PuElement publish(PuElement data) {
		if (data == null || data instanceof PuObjectRO) {
			switch (this.getMethod()) {
			case GET:
				return this.executeGet((PuObjectRO) data);
			case POST:
				return this.executePost((PuObjectRO) data);
			default:
				throw new UnsupportedTypeException(
						"Method type " + this.getMethod() + " is not supported in " + this.getClass().getSimpleName());
			}
		}
		throw new IllegalArgumentException("Http message producer only allow message instanceof PuObjectRO");
	}
}
