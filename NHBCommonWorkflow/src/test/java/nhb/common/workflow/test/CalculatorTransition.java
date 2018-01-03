package nhb.common.workflow.test;

import com.nhb.common.workflow.statemachine.State;
import com.nhb.common.workflow.statemachine.Transition;

import lombok.Getter;

@Getter
public enum CalculatorTransition implements Transition {

	CALCULATE_SUCCESS(1, CalculatorState.CALCULATE_SUM, CalculatorState.PRINT_RESULT);

	private final int id;
	private final State from;
	private final State to;

	private CalculatorTransition(int id, State from, State to) {
		this.id = id;
		this.from = from;
		this.to = to;
	}
}
