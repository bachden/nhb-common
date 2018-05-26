package com.nhb.messaging.zmq;

import org.zeromq.ZMQException;

import com.nhb.common.data.PuNull;

public interface ZMQSendingDoneHandler {

	static ZMQSendingDoneHandler DEFAULT = new ZMQSendingDoneHandler() {

		@Override
		public void onSendingDone(ZMQEvent event) {
			if (event != null) {
				DefaultZMQFuture future = event.getFuture();
				if (future != null && !future.isDone()) {
					if (event.isSuccess()) {
						future.setAndDone(PuNull.IGNORE_ME);
					} else {
						future.setFailedCause(event.getFailedCause() != null ? event.getFailedCause()
								: new ZMQException("Unknown error", -1));
						future.setAndDone(null);
					}
				}
			}
		}
	};

	void onSendingDone(ZMQEvent event);
}
