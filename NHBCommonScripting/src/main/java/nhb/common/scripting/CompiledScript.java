package nhb.common.scripting;

import java.util.Map;

public interface CompiledScript {

	Object run(Map<String, Object> arguments);
}
