package com.nhb.common.async.translator;

import com.nhb.common.async.Callback;
import com.nhb.common.async.RPCCallback;
import com.nhb.common.async.RPCFuture;

public abstract class RPCFutureTranslator<FromType, ToType> extends AbstractFutureTranslator<FromType, ToType>
		implements RPCFuture<ToType> {

	private Callback<ToType> callback;

	@SuppressWarnings("unchecked")
	public RPCFutureTranslator(RPCFuture<FromType> future) {
		super(future);
		if (this.getSourceFuture() instanceof RPCCallback) {
			((RPCCallback<FromType>) this.getSourceFuture()).setCallback(new Callback<FromType>() {

				@Override
				public void apply(FromType result) {
					if (getCallback() != null) {
						getCallback().apply(parseAndSaveResult(result));
					}
				}
			});
		}
	}

	@Override
	public void setCallback(Callback<ToType> callable) {
		this.callback = callable;
	}

	@Override
	public Callback<ToType> getCallback() {
		return this.callback;
	}

}
