package nhb.common.data.msgpkg;

import java.io.IOException;
import java.util.Map.Entry;

import org.msgpack.packer.Packer;
import org.msgpack.template.StringTemplate;
import org.msgpack.unpacker.Unpacker;

import nhb.common.data.PuArrayList;
import nhb.common.data.PuObject;
import nhb.common.data.PuValue;

public class PuObjectTemplate extends PuTemplate<PuObject> {

	private static final PuObjectTemplate instance = new PuObjectTemplate();

	public static PuObjectTemplate getInstance() {
		return instance;
	}

	@Override
	public void write(Packer pk, PuObject obj, boolean required) throws IOException {
		pk.writeMapBegin(obj.size());
		for (Entry<String, PuValue> entry : obj) {
			StringTemplate.getInstance().write(pk, entry.getKey(), required);
			if (entry.getValue().getData() instanceof PuObject) {
				this.write(pk, (PuObject) entry.getValue().getData(), required);
			} else if (entry.getValue().getData() instanceof PuArrayList) {
				PuArrayTemplate.getInstance().write(pk, (PuArrayList) entry.getValue().getPuArray(), required);
			} else {
				this.getGenericTypeTemplate().write(pk, entry.getValue().getData(), required);
			}
		}
		pk.writeMapEnd();
	}

	@Override
	public PuObject read(Unpacker unpacker, PuObject obj, boolean required) throws IOException {
		PuObject puObject = new PuObject();
		int size = unpacker.readMapBegin();
		for (int i = 0; i < size; i++) {
			puObject.set(unpacker.readString(), this.getGenericTypeTemplate().read(unpacker, null, required));
		}
		unpacker.readMapEnd();
		return puObject;
	}
}
