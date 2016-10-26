package com.nhb.eventdriven;

public interface EventHandler {
	public void onEvent(Event event) throws Exception;
}
