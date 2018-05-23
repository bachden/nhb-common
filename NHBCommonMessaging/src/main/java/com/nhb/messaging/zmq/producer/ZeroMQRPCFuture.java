package com.nhb.messaging.zmq.producer;

import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.async.RPCFuture;
import com.nhb.common.data.PuElement;

public interface ZeroMQRPCFuture extends RPCFuture<PuElement> {

}

class DefaultZeroMQRPCFuture extends BaseRPCFuture<PuElement> implements ZeroMQRPCFuture {

}
