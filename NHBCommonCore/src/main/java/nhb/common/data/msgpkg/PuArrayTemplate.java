package nhb.common.data.msgpkg;

import java.io.IOException;

import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

import nhb.common.data.PuArrayList;
import nhb.common.data.PuObject;
import nhb.common.data.PuValue;

public class PuArrayTemplate extends PuTemplate<PuArrayList> {

	private static final PuArrayTemplate instance = new PuArrayTemplate();

	public static PuArrayTemplate getInstance() {
		return instance;
	}

	@Override
	public void write(Packer pk, PuArrayList obj, boolean required) throws IOException {
		pk.writeArrayBegin(obj.size());
		for (PuValue value : obj) {
			if (value.getData() instanceof PuObject) {
				PuObjectTemplate.getInstance().write(pk, value.getPuObject(), required);
			} else if (value.getData() instanceof PuArrayList) {
				this.write(pk, (PuArrayList) value.getPuArray(), required);
			} else {
				this.getGenericTypeTemplate().write(pk, value.getData());
			}
		}
		pk.writeArrayEnd();
	}

	@Override
	public PuArrayList read(Unpacker unpacker, PuArrayList obj, boolean required) throws IOException {
		PuArrayList result = new PuArrayList();
		int length = unpacker.readArrayBegin();
		for (int i = 0; i < length; i++) {
			result.add(new PuValue(this.getGenericTypeTemplate().read(unpacker, null)));
		}
		unpacker.readArrayEnd();
		return result;
	}
}
