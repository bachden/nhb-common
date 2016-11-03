package com.nhb.common.scripting.groovy;

import java.io.IOException;

import org.codehaus.groovy.control.CompilerConfiguration;

import com.nhb.common.BaseLoggable;
import com.nhb.common.scripting.CompiledScript;
import com.nhb.common.scripting.Script;
import com.nhb.common.scripting.ScriptCompiler;
import com.nhb.common.scripting.exception.ScriptCompileException;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;

public class GroovyScriptCompiler extends BaseLoggable implements ScriptCompiler {
	public static final String GROOVY_INDY_SETTING_NAME = "indy";
	public static final String UNTRUSTED_CODEBASE = "/untrusted";

	@SuppressWarnings("rawtypes")
	@Override
	public CompiledScript compile(Script script) {
		CompilerConfiguration configuration = new CompilerConfiguration();
		configuration.getOptimizationOptions().put(GROOVY_INDY_SETTING_NAME, true);
		GroovyCompiledScript result = null;
		try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader(getClass().getClassLoader(), configuration)) {
			GroovyCodeSource codeSource = new GroovyCodeSource(script.getContent(), script.getName(),
					UNTRUSTED_CODEBASE);
			codeSource.setCachable(false);
			Class parseClass = groovyClassLoader.parseClass(codeSource);
			try {
				groovy.lang.Script compiledScript = (groovy.lang.Script) parseClass.newInstance();
				result = new GroovyCompiledScript(compiledScript);
				return result;
			} catch (InstantiationException | IllegalAccessException ex) {
				throw new ScriptCompileException(ex);
			}
		} catch (IOException e) {
			getLogger().warn("error when close goovyClassLoader", e);
			return result;
		}
	}
}
