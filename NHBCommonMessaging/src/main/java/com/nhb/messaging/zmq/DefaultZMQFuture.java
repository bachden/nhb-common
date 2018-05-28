package com.nhb.messaging.zmq;

import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.data.PuElement;

public class DefaultZMQFuture extends BaseRPCFuture<PuElement> implements ZMQFuture {

	@Override
	public void setAndDone(PuElement value) {
		if (value == null) {
			getLogger().error("result value is null", new Exception());
		}
		super.setAndDone(value);
	}
}
