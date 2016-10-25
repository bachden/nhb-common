package nhb.eventdriven.impl;

import java.lang.reflect.Method;

import nhb.common.BaseLoggable;
import nhb.eventdriven.Event;
import nhb.eventdriven.EventHandler;
import nhb.eventdriven.exception.InvalidHandlerInstanceException;
import nhb.eventdriven.exception.InvalidHandlerMethodException;

public class BaseEventHandler extends BaseLoggable implements EventHandler {

	private Object handler;
	private String methodName;
	private Method targetMethod;

	public BaseEventHandler() {
		// do nothing
	}

	public BaseEventHandler(Object handler, String methodName) {
		this.handler = handler;
		this.methodName = methodName;
	}

	@Override
	public void onEvent(Event event) throws Exception {
		if (this.handler == null) {
			throw new InvalidHandlerInstanceException("Instance cannot be null");
		}
		if (targetMethod == null) {
			synchronized (this) {
				if (targetMethod == null) {
					Method[] methods = this.handler.getClass().getMethods();
					for (Method method : methods) {
						if (method.getName().equals(this.methodName) && method.getParameterCount() == 1
								&& Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
							targetMethod = method;
							break;
						}
					}
					if (targetMethod == null) {
						throw new InvalidHandlerMethodException("Method name " + methodName + " has not been found");
					}
				}
			}
		}
		targetMethod.invoke(handler, event);
	}
}
