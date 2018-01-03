package com.nhb.common.workflow.holder;

import java.util.Map;
import java.util.Map.Entry;

import com.nhb.common.Loggable;

public interface EnvironmentVariableHolder extends Loggable {

	default void addEnvironmentVariables(Map<String, Object> map) {
		if (map != null) {
			for (Entry<String, Object> entry : map.entrySet()) {
				this.addEnvironmentVariable(entry.getKey(), entry.getValue());
			}
		}
	}

	boolean containsEnvironmentVariable(String varName);

	void addEnvironmentVariable(String varName, Object value);

	Object removeEnvironmentVariable(String varName);

	<T> T getEnvironmentVariable(String varName);
}
