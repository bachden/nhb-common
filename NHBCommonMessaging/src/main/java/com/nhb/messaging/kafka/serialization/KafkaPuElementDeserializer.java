package com.nhb.messaging.kafka.serialization;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.msgpack.unpacker.Unpacker;

import com.nhb.common.Loggable;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.msgpkg.PuElementTemplate;

public class KafkaPuElementDeserializer extends MsgpackCodec implements Deserializer<PuElement>, Loggable {

	@Override
	public void close() {
	}

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {

	}

	@Override
	public PuElement deserialize(String topic, byte[] data) {
		Unpacker unpacker = this.getMsgpack().createUnpacker(new ByteArrayInputStream(data));
		try {
			return PuElementTemplate.getInstance().read(unpacker, null);
		} catch (Exception e) {
			getLogger().error("Error while deserializing message: " + Arrays.toString(data), e);
			throw new RuntimeException(e);
		}
	}

}
