package nhb.common.processor;

import nhb.common.serializable.PuSerializable;

public interface Processor<T extends PuSerializable, R extends PuSerializable> {

	R process(T message);
}
