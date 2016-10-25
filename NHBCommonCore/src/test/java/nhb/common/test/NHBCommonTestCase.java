package nhb.common.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;
import nhb.common.Loggable;

public abstract class NHBCommonTestCase extends TestCase implements Loggable {

	@Override
	public Logger getLogger() {
		return LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public Logger getLogger(String name) {
		return LoggerFactory.getLogger(name);
	}

}
