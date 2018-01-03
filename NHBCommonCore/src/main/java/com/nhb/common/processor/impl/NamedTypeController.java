package com.nhb.common.processor.impl;

import com.nhb.common.processor.Processor;
import com.nhb.common.serializable.PuNamedTypeSerializableFactory;
import com.nhb.common.serializable.PuSerializable;

public class NamedTypeController extends AutoDeserializeController {

	public NamedTypeController(PuNamedTypeSerializableFactory factory) {
		super(factory);
	}

	public <T extends PuSerializable, R extends PuSerializable> void registerProcessor(String namedMessageType,
			Processor<T, R> processor) {
		PuNamedTypeSerializableFactory serializableFactory = this.getSerializableFactory();
		Class<T> messageClass = serializableFactory.getClassForType(namedMessageType);
		this.registerProcessor(messageClass, processor);
	}

}
