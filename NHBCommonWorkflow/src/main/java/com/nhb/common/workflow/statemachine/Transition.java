package com.nhb.common.workflow.statemachine;

public interface Transition {

	int getId();

	State getFrom();

	State getTo();

}
