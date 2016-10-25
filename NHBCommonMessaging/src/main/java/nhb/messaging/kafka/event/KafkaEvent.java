package nhb.messaging.kafka.event;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import nhb.common.data.PuElement;
import nhb.eventdriven.impl.AbstractEvent;

public class KafkaEvent extends AbstractEvent {

	public static final String NEW_RECORD = "newRecord";

	private ConsumerRecord<byte[], PuElement> record;

	public ConsumerRecord<byte[], PuElement> getRecord() {
		return record;
	}

	public void setRecord(ConsumerRecord<byte[], PuElement> record) {
		this.record = record;
	}

	public static KafkaEvent newInstance(ConsumerRecord<byte[], PuElement> record) {
		KafkaEvent event = new KafkaEvent();
		event.setType(NEW_RECORD);
		event.setRecord(record);
		return event;
	}
}
