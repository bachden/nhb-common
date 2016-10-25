package nhb.common.workflow.test;

import lombok.Getter;
import nhb.common.workflow.statemachine.State;

@Getter
public enum CalculatorState implements State {

	CALCULATE_SUM(1, "calculateSum"), PRINT_RESULT(2, "printResult");

	private final String name;
	private final int id;

	private CalculatorState(int id, String name) {
		this.id = id;
		this.name = name;
	}
}
