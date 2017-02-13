package com.nhb.messaging.kafka.consumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import com.nhb.common.data.PuElement;
import com.nhb.eventdriven.impl.BaseEventDispatcher;
import com.nhb.messaging.kafka.event.KafkaEvent;
import com.nhb.messaging.kafka.serialization.KafkaPuElementDeserializer;

public class KafkaMessageConsumer extends BaseEventDispatcher {

	private static final long START_OFFSET = -1l;
	private static final long END_OFFSET = -2l;

	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicBoolean closer = new AtomicBoolean(false);
	private List<String> topics;
	private KafkaConsumer<byte[], PuElement> consumer;

	private int pollTimeout = 100;
	private Thread poolingThead;

	private Properties properties;

	private final Map<TopicPartition, Long> seekConfigs = new ConcurrentHashMap<>();

	public KafkaMessageConsumer(Properties properties, List<String> topics, int pollTimeout) {
		if (properties == null) {
			throw new IllegalArgumentException("Properties for kafka message consumer cannot be null");
		}
		if (topics == null || topics.size() == 0) {
			throw new IllegalArgumentException("Topics cannot be empty");
		}

		properties.put("key.deserializer", ByteArrayDeserializer.class.getName());
		properties.put("value.deserializer", KafkaPuElementDeserializer.class.getName());

		this.properties = properties;

		this.consumer = new KafkaConsumer<>(properties);
		this.topics = topics;
		this.pollTimeout = pollTimeout;
	}

	public Set<TopicPartition> getTopicPartitions() {
		if (!this.isRunning()) {
			throw new RuntimeException("Consumer must be running before get partitions");
		}
		return this.consumer.assignment();
	}

	private Collection<PartitionInfo> _getPartitionInfos(String topic) {
		if (topic != null) {
			return this.consumer.partitionsFor(topic);
		}
		return null;
	}

	public Map<String, Collection<PartitionInfo>> getPartitionInfos(String... topics) {
		if (!this.isRunning()) {
			throw new RuntimeException("Consumer must be running before get partitions");
		} else if (topics != null) {
			Map<String, Collection<PartitionInfo>> results = new HashMap<>();
			for (String topic : topics) {
				results.put(topic, _getPartitionInfos(topic));
			}
			return results;
		}
		return null;
	}

	public void seek(TopicPartition partition, long offset) {
		if (this.isRunning()) {
			throw new IllegalStateException("Cannot seek while consumer is running");
		}
		this.seekConfigs.put(partition, offset);
	}

	public void seekToBeginning(Collection<TopicPartition> partitions) {
		if (this.isRunning()) {
			throw new IllegalStateException("Cannot seek while consumer is running");
		}
		for (TopicPartition topicPartition : partitions) {
			this.seekConfigs.put(topicPartition, START_OFFSET);
		}
	}

	public void seekToEnd(Collection<TopicPartition> partitions) {
		if (this.isRunning()) {
			throw new IllegalStateException("Cannot seek while consumer is running");
		}
		for (TopicPartition topicPartition : partitions) {
			this.seekConfigs.put(topicPartition, END_OFFSET);
		}
	}

	public boolean isRunning() {
		return this.running.get();
	}

	public void start() {
		if (this.running.compareAndSet(false, true)) {
			this.consumer.subscribe(this.topics);
			this.poolingThead = new Thread() {

				@Override
				public void run() {
					while (!closer.get()) {
						try {
							if (seekConfigs.size() > 0) {
								Collection<TopicPartition> seekToEndPartitions = new ArrayList<>();
								Collection<TopicPartition> seekToBeginningPartitions = new ArrayList<>();
								Map<TopicPartition, Long> seekPartitions = new HashMap<>();

								for (Entry<TopicPartition, Long> entry : seekConfigs.entrySet()) {
									if (entry.getValue() == START_OFFSET) {
										seekToBeginningPartitions.add(entry.getKey());
									} else if (entry.getValue() == END_OFFSET) {
										seekToEndPartitions.add(entry.getKey());
									} else {
										seekPartitions.put(entry.getKey(), entry.getValue());
									}
								}

								if (seekToBeginningPartitions.size() > 0) {
									consumer.seekToBeginning(seekToBeginningPartitions);
								}
								if (seekToEndPartitions.size() > 0) {
									consumer.seekToEnd(seekToEndPartitions);
								}
								if (seekPartitions.size() > 0) {
									for (Entry<TopicPartition, Long> entry : seekPartitions.entrySet()) {
										consumer.seek(entry.getKey(), entry.getValue());
									}
								}
							}
							ConsumerRecords<byte[], PuElement> records = consumer.poll(pollTimeout);
							Iterator<ConsumerRecord<byte[], PuElement>> it = records.iterator();
							while (it.hasNext()) {
								ConsumerRecord<byte[], PuElement> record = it.next();
								KafkaEvent event = KafkaEvent.newInstance(record);
								dispatchEvent(event);
							}
						} catch (WakeupException we) {
							// do nothing
						}
					}
				}
			};
			this.poolingThead.start();

			getLogger().info("Kafka Message Consumer started successfully with properties: {");
			for (Object key : this.properties.keySet()) {
				getLogger("pureLogger").info("\t" + key + " = " + properties.getProperty((String) key));
			}
			getLogger("pureLogger").info("}");
		}
	}

	public void stop() {
		if (this.consumer != null) {
			this.closer.set(true);
			this.consumer.wakeup();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public Properties getProperties() {
		return this.properties;
	}
}
