package nhb.common.scripting;

import javax.script.ScriptEngineManager;

public interface ScriptCompiler {

	static final ScriptEngineManager SCRIPT_ENGINE_MANAGER = new ScriptEngineManager();

	CompiledScript compile(Script script);
}
