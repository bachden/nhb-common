package com.nhb.messaging.zmq;

import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.data.PuElement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class DefaultZMQFuture extends BaseRPCFuture<PuElement> implements ZMQFuture {

	@Getter
	@Setter(AccessLevel.PACKAGE)
	private PuElement metadata;
}
