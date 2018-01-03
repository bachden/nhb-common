package com.nhb.common.workflow.statemachine;

import com.nhb.common.workflow.statemachine.impl.SimpleState;

public interface State {

	int getId();

	String getName();

	public static SimpleState.SimpleStateBuilder builder() {
		return SimpleState.builder();
	}

	public static final State ASTERISK = new State() {

		@Override
		public String getName() {
			return "*";
		}

		@Override
		public int getId() {
			return -1;
		}
	};

	public static final State END = new State() {

		@Override
		public int getId() {
			return Integer.MAX_VALUE;
		}

		@Override
		public String getName() {
			return "end";
		}
	};

	public static final State START = new State() {

		@Override
		public int getId() {
			return 0;
		}

		@Override
		public String getName() {
			return "start";
		}

	};
}
