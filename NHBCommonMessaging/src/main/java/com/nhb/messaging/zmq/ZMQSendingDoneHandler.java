package com.nhb.messaging.zmq;

import org.zeromq.ZMQException;

import com.nhb.common.Loggable;
import com.nhb.common.data.PuNull;

public interface ZMQSendingDoneHandler extends Loggable {

	static ZMQSendingDoneHandler DEFAULT = new ZMQSendingDoneHandler() {

		@Override
		public void onSendingDone(final ZMQEvent event) {
			if (event != null) {
				final DefaultZMQFuture future = event.getFuture();
				if (future != null) {
					if (!future.isDone()) {
						if (event.isSuccess()) {
							future.setAndDone(PuNull.IGNORE_ME);
						} else {
							future.setFailedCause(event.getFailedCause() != null ? event.getFailedCause()
									: new ZMQException("Unknown error", -1));
							future.setAndDone(null);
						}
					} else {
						getLogger().warn("Future already done!!!, messageId: {}, responseEndpoint: {}",
								event.getMessageId(), event.getResponseEndpoint());
					}
				} else {
					getLogger().warn("Future not found for message id: {}, responseEndpoint: {}", event.getMessageId(),
							event.getResponseEndpoint());
				}
			} else {
				throw new NullPointerException("Cannot make done on null event");
			}
		}
	};

	void onSendingDone(ZMQEvent event);
}
