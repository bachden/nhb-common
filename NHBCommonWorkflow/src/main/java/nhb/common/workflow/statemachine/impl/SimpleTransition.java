package nhb.common.workflow.statemachine.impl;

import lombok.Builder;
import lombok.Getter;
import nhb.common.workflow.statemachine.State;
import nhb.common.workflow.statemachine.Transition;

@Getter
@Builder
public class SimpleTransition implements Transition {

	private int id;
	private State from = State.ASTERISK;
	private State to = State.ASTERISK;
}
