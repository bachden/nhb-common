package com.nhb.messaging.kafka.producer;

import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuElement;
import com.nhb.common.utils.UUIDUtils;
import com.nhb.messaging.MessageProducer;
import com.nhb.messaging.kafka.serialization.KafkaPuElementSerializer;

public class KafkaMessageProducer extends BaseLoggable implements MessageProducer<byte[]> {

	private KafkaProducer<byte[], PuElement> producer;
	private String defaultTopic;

	public KafkaMessageProducer(Properties properties, String defaultTopic) {
		if (properties == null) {
			throw new RuntimeException("Properties for kafka producer cannot be null");
		}

		properties.put("key.serializer", ByteArraySerializer.class.getName());
		properties.put("value.serializer", KafkaPuElementSerializer.class.getName());

		getLogger().debug("init kafka producer with config: " + properties);

		this.producer = new KafkaProducer<>(properties);
		this.defaultTopic = defaultTopic;
	}

	public void stop() {
		if (this.producer != null) {
			this.producer.close();
		}
	}

	public void send(byte[] key, PuElement value) {
		this.producer.send(new ProducerRecord<byte[], PuElement>(this.defaultTopic, key, value));
	}

	public Future<RecordMetadata> send(byte[] key, PuElement value, Callback callback) {
		return this.producer.send(new ProducerRecord<byte[], PuElement>(this.defaultTopic, key, value), callback);
	}

	public void send(String topic, byte[] key, PuElement value) {
		this.producer.send(new ProducerRecord<byte[], PuElement>(topic, key, value));
	}

	public void send(String topic, byte[] key, PuElement value, Callback callback) {
		this.producer.send(new ProducerRecord<byte[], PuElement>(topic, key, value), callback);
	}

	@Override
	public byte[] publish(PuElement data) {
		byte[] key = UUIDUtils.timebasedUUIDAsBytes();
		this.send(key, data);
		return key;
	}

	@Override
	public byte[] publish(PuElement data, String topic) {
		if (topic == null) {
			throw new IllegalArgumentException("topic cannot be null");
		}
		byte[] key = UUIDUtils.timebasedUUIDAsBytes();
		this.send(topic, key, data);
		return key;
	}

}
