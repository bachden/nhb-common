package com.nhb.common.scripting.groovy;

import java.util.Map;

import com.nhb.common.scripting.CompiledScript;

import groovy.lang.Binding;

public class GroovyCompiledScript implements CompiledScript {

	private final groovy.lang.Script compiledObject;

	public GroovyCompiledScript(groovy.lang.Script compiled) {
		this.compiledObject = compiled;
	}

	@Override
	public Object run(Map<String, Object> arguments) {
		compiledObject.setBinding(new Binding(arguments));
		return this.compiledObject.run();
	}

}
