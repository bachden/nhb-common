package com.nhb.messaging.kafka.serialization;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import com.nhb.common.data.PuElement;

public class KafkaPuElementSerializer extends MsgpackCodec implements Serializer<PuElement> {

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {

	}

	@Override
	public byte[] serialize(String topic, PuElement data) {
		if (data == null) {
			return null;
		}
		byte[] result = data.toBytes();
		// System.out.println("Sending data: " + new String(result));
		return result;
	}

	@Override
	public void close() {

	}

}
