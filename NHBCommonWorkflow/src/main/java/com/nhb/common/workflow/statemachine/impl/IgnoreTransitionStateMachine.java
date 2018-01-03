package com.nhb.common.workflow.statemachine.impl;

import com.nhb.common.workflow.statemachine.State;
import com.nhb.common.workflow.statemachine.StateMachine;
import com.nhb.common.workflow.statemachine.Transition;

import lombok.Getter;
import lombok.Setter;

public class IgnoreTransitionStateMachine implements StateMachine {

	@Getter
	private State currentState;

	@Setter
	@Getter
	private State finalState;

	@Override
	public void gotoState(State nextState) {
		if (nextState == null) {
			throw new NullPointerException("Cannot goto null state");
		}
		this.currentState = nextState;
	}

	@Override
	public void addTransition(Transition... transitions) {
		// not supported
	}

	@Override
	public void removeTransition(State from, State to) {
		// not supported
	}

	@Override
	public void addTransition(State from, State to) {
		// not supported
	}

	@Override
	public State getStateForName(String name) {
		// not supported
		return null;
	}

	@Override
	public State getStateForId(int id) {
		// not supported
		return null;
	}

}
