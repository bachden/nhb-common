package com.nhb.messaging.kafka.consumer;

import java.util.Map;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;

import com.nhb.common.data.PuElement;

public class KafkaOffsetCommitter {

	private final KafkaConsumer<byte[], PuElement> consumer;

	KafkaOffsetCommitter(KafkaConsumer<byte[], PuElement> consumer) {
		this.consumer = consumer;
	}

	public void commitSync() {
		consumer.commitSync();
	}

	public void commitSync(Map<TopicPartition, OffsetAndMetadata> offsets) {
		consumer.commitSync(offsets);
	}

	public void commitAsync() {
		consumer.commitAsync();
	}

	public void commitAsync(OffsetCommitCallback callback) {
		consumer.commitAsync(callback);
	}

	public void commitAsync(Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback) {
		consumer.commitAsync(offsets, callback);
	}

	public OffsetAndMetadata committed(TopicPartition partition) {
		return consumer.committed(partition);
	}

}
