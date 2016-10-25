package nhb.common.scripting;

import java.util.Map;

/**
 * An executable object contains compiled script and runtime arguments, can be
 * invoke from anywhere
 * 
 * @author bachden
 * 
 */
public interface ExecutableScript {

	Map<String, Object> getExecuteArguments();

	void setCompiledScript(CompiledScript compiledScript);

	CompiledScript getCompiledScript();

	default Object run() {
		return this.getCompiledScript().run(this.getExecuteArguments());
	}
}
