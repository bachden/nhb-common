package com.nhb.messaging;

import com.nhb.common.data.PuElement;

public interface MessageProducer<T> {

	T publish(PuElement data);

	T publish(PuElement data, String routingKey);
}
