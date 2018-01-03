package com.nhb.common.processor.impl;

import com.nhb.common.data.PuArray;
import com.nhb.common.serializable.PuSerializable;
import com.nhb.common.serializable.PuSerializableFactory;

public class AutoDeserializeController extends BaseController {

	private final PuSerializableFactory serializableFactory;

	public AutoDeserializeController(PuSerializableFactory factory) {
		this.serializableFactory = factory;
	}

	@SuppressWarnings("unchecked")
	protected <T extends PuSerializableFactory> T getSerializableFactory() {
		return (T) this.serializableFactory;
	}

	@SuppressWarnings("unchecked")
	public <R extends PuSerializable> R process(PuArray puArray) {
		PuSerializable data = this.serializableFactory.deserialize(puArray);
		return (R) this.process(data);
	}
}
