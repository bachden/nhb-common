package nhb.common.workflow.statemachine.impl;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import nhb.common.workflow.statemachine.State;

@Setter
@Getter
@Builder
public class SimpleState implements State {

	private int id;

	private String name;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof State) {
			return ((State) obj).getId() == this.getId();
		}
		return false;
	}

}
