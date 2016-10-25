import java.util.Collections;

import javax.script.ScriptException;

import nhb.common.scripting.CompiledScript;
import nhb.common.scripting.groovy.GroovyScript;
import nhb.common.scripting.groovy.GroovyScriptCompiler;

public class LoadGroovy {
	public static void main(String[] args) throws ScriptException {
		GroovyScript script = new GroovyScript("println('Hello World'); return 1");
		CompiledScript compile = new GroovyScriptCompiler().compile(script);
		System.out.println(compile.run(Collections.emptyMap()));
	}
}
