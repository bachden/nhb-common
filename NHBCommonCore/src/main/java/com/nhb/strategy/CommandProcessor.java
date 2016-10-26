package com.nhb.strategy;

public interface CommandProcessor {
	public CommandResponseData execute(CommandController context, CommandRequestParameters request);
}
