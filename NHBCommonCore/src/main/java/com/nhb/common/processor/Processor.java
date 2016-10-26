package com.nhb.common.processor;

import com.nhb.common.serializable.PuSerializable;

public interface Processor<T extends PuSerializable, R extends PuSerializable> {

	R process(T message);
}
