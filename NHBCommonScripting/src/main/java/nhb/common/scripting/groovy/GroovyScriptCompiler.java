package nhb.common.scripting.groovy;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import nhb.common.scripting.CompiledScript;
import nhb.common.scripting.Script;
import nhb.common.scripting.ScriptCompiler;
import nhb.common.scripting.exception.IllegalLanguageException;
import nhb.common.scripting.exception.ScriptCompileException;
import nhb.common.scripting.statics.ScriptLanguage;

public class GroovyScriptCompiler implements ScriptCompiler {

	private static final ScriptEngine groovyScriptEngine = SCRIPT_ENGINE_MANAGER.getEngineByName("groovy");

	private static Compilable compilableEngine = (Compilable) groovyScriptEngine;

	@Override
	public CompiledScript compile(Script script) {
		if (script.getLanguage() != ScriptLanguage.GROOVY) {
			throw new IllegalLanguageException("Only Groovy Language Script is allowed");
		}
		try {
			javax.script.CompiledScript compiled = compilableEngine.compile(script.getContent());
			return new GroovyCompiledScript(compiled);
		} catch (ScriptException e) {
			throw new ScriptCompileException(e);
		}
	}

}
