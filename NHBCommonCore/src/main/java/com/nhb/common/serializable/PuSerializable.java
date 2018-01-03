package com.nhb.common.serializable;

import com.nhb.common.Loggable;
import com.nhb.common.data.PuArray;

public interface PuSerializable extends Loggable {

	void write(PuArray puArray);

	void read(PuArray puArray);
}
