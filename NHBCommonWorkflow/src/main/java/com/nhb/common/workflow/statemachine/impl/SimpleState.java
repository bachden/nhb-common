package com.nhb.common.workflow.statemachine.impl;

import com.nhb.common.workflow.statemachine.State;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
