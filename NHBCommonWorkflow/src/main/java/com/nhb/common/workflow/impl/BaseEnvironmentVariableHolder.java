package com.nhb.common.workflow.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nhb.common.workflow.holder.EnvironmentVariableHolder;

public class BaseEnvironmentVariableHolder implements EnvironmentVariableHolder {

	private final Map<String, Object> variables = new ConcurrentHashMap<>();

	@Override
	public void addEnvironmentVariable(String varName, Object value) {
		this.variables.put(varName, value);
	}

	@Override
	public Object removeEnvironmentVariable(String varName) {
		return this.variables.remove(varName);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getEnvironmentVariable(String varName) {
		return (T) this.variables.get(varName);
	}

	@Override
	public boolean containsEnvironmentVariable(String varName) {
		return this.variables.containsKey(varName);
	}

}
