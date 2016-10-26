package com.nhb.common.async.translator;

import java.util.concurrent.Future;

public interface FutureTranslator<FromType, ToType> extends Future<ToType> {

	Throwable getFailedCause();
}
