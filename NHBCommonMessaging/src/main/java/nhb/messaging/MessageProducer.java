package nhb.messaging;

import nhb.common.data.PuElement;

public interface MessageProducer<T> {

	T publish(PuElement data);

	T publish(PuElement data, String routingKey);
}
