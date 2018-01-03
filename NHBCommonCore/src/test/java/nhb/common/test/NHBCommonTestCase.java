package nhb.common.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhb.common.Loggable;

import junit.framework.TestCase;

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
