package com.nhb.common.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhb.common.Loggable;
import com.nhb.common.utils.Initializer;

import junit.framework.TestCase;

public abstract class NHBTestCase extends TestCase implements Loggable {

	private static boolean isInitialized = false;

	@Override
	protected final void setUp() throws Exception {
		if (!isInitialized) {
			System.out.println("Initializing base path and configuration...");
			Initializer.bootstrap(this.getClass());
			isInitialized = true;
		}
		this.customSetUp();
	}

	protected void customSetUp() throws Exception {
		// do nothing
	}

	@Override
	public Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}

	@Override
	public Logger getLogger(String name) {
		return LoggerFactory.getLogger(name);
	}
}
