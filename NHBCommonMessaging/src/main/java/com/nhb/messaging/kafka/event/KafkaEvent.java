package com.nhb.messaging.kafka.event;

import org.apache.kafka.clients.consumer.ConsumerRecords;

import com.nhb.common.data.PuElement;
import com.nhb.eventdriven.impl.AbstractEvent;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KafkaEvent extends AbstractEvent {

	public static final String NEW_BATCH = "newBatch";

	private ConsumerRecords<byte[], PuElement> batch;

	public static KafkaEvent newInstance(ConsumerRecords<byte[], PuElement> batch) {
		KafkaEvent event = new KafkaEvent();
		event.setType(NEW_BATCH);
		event.setBatch(batch);
		return event;
	}
}
