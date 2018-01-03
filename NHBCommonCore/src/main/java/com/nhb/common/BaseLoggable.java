package com.nhb.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhb.common.annotations.Transparent;

public class BaseLoggable implements Loggable {

	private Logger logger = null;

	@Override
	@Transparent
	public Logger getLogger() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(getClass());
		}
		return logger;
	}

	@Override
	@Transparent
	public Logger getLogger(String name) {
		return LoggerFactory.getLogger(name);
	}

}
