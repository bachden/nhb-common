package com.nhb.messaging.zmq.producer;

import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuElement;

interface ZeroMQMetadataGenerator {

	PuArray generateMetadata(PuElement data);
}
