package com.nhb.messaging.zmq.producer;

import com.nhb.common.data.PuArray;

interface ZeroMQMetadataExtractor {

	void extractMetadata(PuArray metadata, ZeroMQResponse response);
}
