package nhb.common.cache.jedis;

import redis.clients.jedis.Jedis;

public interface JedisRunner<T> {

	T execute(Jedis jedis);
}
