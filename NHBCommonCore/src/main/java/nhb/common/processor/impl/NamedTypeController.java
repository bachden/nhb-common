package nhb.common.processor.impl;

import nhb.common.processor.Processor;
import nhb.common.serializable.PuNamedTypeSerializableFactory;
import nhb.common.serializable.PuSerializable;

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
