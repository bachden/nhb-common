package com.nhb.messaging.zmq;

import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;

public interface ZMQFuture extends RPCFuture<PuElement> {

	static DefaultZMQFuture newDefault() {
		return new DefaultZMQFuture();
	}
	
}
