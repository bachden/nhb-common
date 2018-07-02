package com.nhb.messaging.zmq;

import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.async.Callback;
import com.nhb.common.data.PuElement;

import lombok.Getter;
import lombok.Setter;

public class DefaultZMQFuture extends BaseRPCFuture<PuElement> implements ZMQFuture {

	@Setter
	@Getter
	private volatile byte[] refId;

	@Setter
	private Callback<byte[]> cancelCallback;

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (super.cancel(mayInterruptIfRunning)) {
			if (this.cancelCallback != null) {
				this.cancelCallback.apply(this.refId);
			}
			return true;
		}
		return false;
	}
}
