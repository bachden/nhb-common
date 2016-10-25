package nhb.common.workflow.statemachine;

import nhb.common.Loggable;

public interface StateMachine extends Loggable {

	State getCurrentState();

	State getFinalState();

	void gotoState(State nextState);

	void setFinalState(State finalState);

	void addTransition(Transition... stateTransitions);

	void addTransition(State from, State to);

	State getStateForName(String name);

	State getStateForId(int id);

	void removeTransition(State from, State to);

	default boolean isFinal() {
		return this.getCurrentState() != null && this.getFinalState() != null
				&& this.getCurrentState().equals(this.getFinalState());
	}

	/**
	 * The shorthand for calling gotoState(startState) for the first times
	 * 
	 * @param startState
	 */
	default void start(State startState) {
		if (this.getCurrentState() == null) {
			this.gotoState(startState);
		} else {
			throw new IllegalStateException("Cannot start when current state not null");
		}
	}

	default void end() {
		this.gotoState(getFinalState());
	}
}
