package nhb.common.cache.jedis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nhb.common.BaseLoggable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Pipeline;

public class JedisService extends BaseLoggable implements AutoCloseable {

	private Jedis jedis;
	private JedisPool pool;
	private JedisCluster cluster;
	private JedisSentinelPool sentinel;

	public Jedis getJedis() {
		return jedis;
	}

	public void setJedis(Jedis jedis) {
		this.jedis = jedis;
	}

	public JedisPool getPool() {
		return pool;
	}

	public void setPool(JedisPool pool) {
		this.pool = pool;
	}

	public JedisCluster getCluster() {
		return cluster;
	}

	public void setCluster(JedisCluster cluster) {
		this.cluster = cluster;
	}

	public JedisSentinelPool getSentinel() {
		return sentinel;
	}

	public void setSentinel(JedisSentinelPool sentinel) {
		this.sentinel = sentinel;
	}

	public JedisService() {
		// do nothing
	}

	public JedisService(Jedis jedis) {
		this();
		this.jedis = jedis;
	}

	public JedisService(JedisPool pool) {
		this();
		this.pool = pool;
	}

	public JedisService(JedisCluster cluster) {
		this();
		this.cluster = cluster;
	}

	public JedisService(JedisSentinelPool sentinel) {
		this();
		this.sentinel = sentinel;
	}

	@Override
	public void close() {
		if (this.jedis != null) {
			try {
				this.jedis.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (this.pool != null) {
			try {
				this.pool.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (this.cluster != null) {
			try {
				this.cluster.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (this.sentinel != null) {
			try {
				this.sentinel.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Execute a logic with Jedis pre-created injected and auto release after
	 * used
	 * 
	 * @param runner:
	 *            the object handler
	 * @return true if runner.execute is called (don't care about exception),
	 *         false vice-versa
	 */
	@SuppressWarnings("deprecation")
	public <T> T execute(JedisRunner<T> runner) {
		T result = null;
		if (runner != null) {
			Collection<JedisPool> pools = null;
			boolean usingCluster = false;
			if (this.cluster != null) {
				pools = this.cluster.getClusterNodes().values();
				usingCluster = true;
			} else if (this.pool != null) {
				pools = new ArrayList<>();
				pools.add(this.pool);
			}

			if (pools != null && pools.size() > 0) {
				for (JedisPool p : pools) {
					Jedis jedis = null;
					try {
						jedis = p.getResource();
						if (jedis != null) {
							result = runner.execute(jedis);
						}
					} catch (Exception ex) {
						String message = ex.getMessage();
						if (message.indexOf("Lua script attempted to access a non local key in a cluster node") < 0) {
							throw ex;
						}
					} finally {
						if (jedis != null) {
							p.returnResourceObject(jedis);
						}
					}
				}
				if (usingCluster && this.pool != null) {
					// retry with pool if using cluster not success (the reason
					// may be no cluster node is available)
					Jedis jedis = null;
					try {
						jedis = this.pool.getResource();
						if (jedis != null) {
							result = runner.execute(jedis);
						}
					} catch (Exception ex) {
						String message = ex.getMessage();
						if (message.indexOf("Lua script attempted to access a non local key in a cluster node") < 0) {
							throw ex;
						}
					} finally {
						if (jedis != null) {
							this.pool.returnResourceObject(jedis);
						}
					}
				}
			} else if (this.sentinel != null) {
				Jedis jedis = null;
				try {
					jedis = this.sentinel.getResource();
					if (jedis != null) {
						result = runner.execute(jedis);
					}
				} catch (Exception ex) {
					String message = ex.getMessage();
					if (message.indexOf("Lua script attempted to access a non local key in a cluster node") < 0) {
						throw ex;
					}
				} finally {
					if (jedis != null) {
						this.sentinel.returnResourceObject(jedis);
					}
				}
			} else if (jedis != null) {
				result = runner.execute(jedis);
			} else {
				throw new RuntimeException("No JedisPool has been found to execute");
			}
		}
		return result;
	}

	/**
	 * Execute runner with pre-created pipeline, no need to callpipeline.save()
	 * 
	 * @param runner
	 */
	public Object executeOnPipeline(final JedisPipelineRunner runner) {
		return this.execute(new JedisRunner<Object>() {

			@Override
			public Object execute(Jedis jedis) {
				Pipeline pipeline = jedis.pipelined();
				Object result = runner.execute(pipeline);
				pipeline.save();
				return result;
			}
		});
	}

	public boolean isClustering() {
		return this.cluster != null;
	}

	// **************** Delegate methods ****************

	public <T> T evalsha(String sha) {
		return this.evalsha(sha, 0, new String[0]);
	}

	public <T> T evalsha(final String sha, final int keyCount, final String... params) {
		return this.execute(new JedisRunner<T>() {

			@Override
			@SuppressWarnings("unchecked")
			public T execute(Jedis jedis) {
				return (T) jedis.evalsha(sha, keyCount, params);
			}
		});
	}

	public <T> T eval(final String script) {
		return this.execute(new JedisRunner<T>() {

			@Override
			@SuppressWarnings("unchecked")
			public T execute(Jedis jedis) {
				return (T) jedis.eval(script);
			}
		});
	}

	public String scriptLoad(final String script) {
		return this.execute(new JedisRunner<String>() {

			@Override
			public String execute(Jedis jedis) {
				return jedis.scriptLoad(script);
			}
		});
	}

	public <T> T eval(final String script, final int keyCount, final String... params) {
		return this.execute(new JedisRunner<T>() {

			@Override
			@SuppressWarnings("unchecked")
			public T execute(Jedis jedis) {
				return (T) jedis.eval(script, keyCount, params);
			}
		});
	}

	public String hmset(final String key, final Map<String, String> map) {
		return this.execute(new JedisRunner<String>() {

			@Override
			public String execute(Jedis jedis) {
				return jedis.hmset(key, map);
			}

		});
	}

	public String hmset(final byte[] key, final Map<byte[], byte[]> map) {
		return this.execute(new JedisRunner<String>() {

			@Override
			public String execute(Jedis jedis) {
				return jedis.hmset(key, map);
			}
		});
	}

	public long sadd(final String key, final String value) {
		return this.execute(new JedisRunner<Long>() {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.sadd(key, value);
			}
		});
	}

	public long sadd(final byte[] key, final byte[] value) {
		return this.execute(new JedisRunner<Long>() {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.sadd(key, value);
			}
		});
	}

	public Set<byte[]> smembers(final byte[] key) {
		return this.execute(new JedisRunner<Set<byte[]>>() {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.smembers(key);
			}
		});
	}

	public Set<String> smembers(final String key) {
		return this.execute(new JedisRunner<Set<String>>() {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.smembers(key);
			}
		});
	}

	public boolean exists(final String key) {
		return this.execute(new JedisRunner<Boolean>() {

			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.exists(key);
			}
		});
	}

	public String get(final String key) {
		return this.execute(new JedisRunner<String>() {

			@Override
			public String execute(Jedis jedis) {
				return jedis.get(key);
			}
		});
	}

	public String set(final String key, final String value) {
		return this.execute(new JedisRunner<String>() {

			@Override
			public String execute(Jedis jedis) {
				return jedis.set(key, value);
			}
		});
	}

	public List<String> hmget(final String configKey, final String... keys) {
		return this.execute(new JedisRunner<List<String>>() {

			@Override
			public List<String> execute(Jedis jedis) {
				return jedis.hmget(configKey, keys);
			}
		});
	}

	public List<String> hmget(String key, List<String> fields) {
		return this.hmget(key, fields.toArray(new String[fields.size()]));
	}

	public String hget(final String key, final String field) {
		return this.execute(new JedisRunner<String>() {

			@Override
			public String execute(Jedis jedis) {
				return jedis.hget(key, field);
			}
		});
	}

	public Long hset(final String key, final String field, final String value) {
		return this.execute(new JedisRunner<Long>() {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.hset(key, field, value);
			}
		});
	}

	public boolean hexists(final String key, final String field) {
		return this.execute(new JedisRunner<Boolean>() {

			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.hexists(key, field);
			}
		});
	}

	public Map<String, String> hgetall(final String key) {
		return this.execute(new JedisRunner<Map<String, String>>() {

			@Override
			public Map<String, String> execute(Jedis jedis) {
				return jedis.hgetAll(key);
			}
		});
	}

	public long del(final String key) {
		return this.execute(new JedisRunner<Long>() {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.del(key);
			}
		});
	}

	public long srem(final String key, final String... members) {
		return this.execute(new JedisRunner<Long>() {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.srem(key, members);
			}
		});
	}
}
