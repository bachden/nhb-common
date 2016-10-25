package nhb.common.serializable;

import nhb.common.Loggable;
import nhb.common.data.PuArray;

public interface PuSerializable extends Loggable {

	void write(PuArray puArray);

	void read(PuArray puArray);
}
