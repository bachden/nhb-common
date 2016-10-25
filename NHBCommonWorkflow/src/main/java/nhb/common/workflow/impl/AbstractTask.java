package nhb.common.workflow.impl;

import lombok.Getter;
import nhb.common.Loggable;
import nhb.common.workflow.Task;

public abstract class AbstractTask implements Loggable, Task {

	@Getter
	private final String name;

	public AbstractTask(String name) {
		this.name = name;
	}

}
