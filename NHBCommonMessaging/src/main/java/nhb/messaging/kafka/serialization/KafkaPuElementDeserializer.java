package nhb.messaging.kafka.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.msgpack.unpacker.Unpacker;

import nhb.common.data.PuElement;
import nhb.common.data.msgpkg.PuElementTemplate;

public class KafkaPuElementDeserializer extends MsgpackCodec implements Deserializer<PuElement> {

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
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
