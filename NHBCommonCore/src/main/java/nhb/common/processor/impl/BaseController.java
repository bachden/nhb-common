package nhb.common.processor.impl;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

import nhb.common.processor.Controller;
import nhb.common.processor.Processor;
import nhb.common.serializable.PuSerializable;

public class BaseController implements Controller {

	private final Map<Class<? extends PuSerializable>, Processor<? extends PuSerializable, ? extends PuSerializable>> processors = new ConcurrentHashMap<>();

	@Override
	public <T extends PuSerializable, R extends PuSerializable> void registerProcessor(Class<T> messageType,
			Processor<T, R> processor) {
		this.processors.put(messageType, processor);
	}

	@Override
	public <T extends PuSerializable, R extends PuSerializable> void deregisterProcessor(Class<T> messageType) {
		this.processors.remove(messageType);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends PuSerializable, R extends PuSerializable> Processor<T, R> getProcessorByType(Class<T> clazz) {
		return (Processor<T, R>) this.processors.get(clazz);
	}

}
