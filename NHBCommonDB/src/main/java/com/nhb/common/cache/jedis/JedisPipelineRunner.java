package com.nhb.common.cache.jedis;

import redis.clients.jedis.Pipeline;

public interface JedisPipelineRunner {

	Object execute(Pipeline pipeline);
}
