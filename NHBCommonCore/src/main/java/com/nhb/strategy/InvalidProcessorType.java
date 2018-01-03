package com.nhb.strategy;

public class InvalidProcessorType extends Exception {

	private static final long serialVersionUID = -8800964978479327185L;

	public InvalidProcessorType() {
	}
	
	public InvalidProcessorType(String message) {
		super(message);
	}
}
