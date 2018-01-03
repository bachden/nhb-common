package nhb.test.common.jedis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.nhb.common.cache.jedis.JedisService;
import com.nhb.common.vo.HostAndPort;

import redis.clients.jedis.JedisSentinelPool;

public class TestJesisService {

	private static final Collection<HostAndPort> endpoints = new HashSet<>();

	static {
		endpoints.add(new HostAndPort("gateway.puppetteam.com", 16380));
		endpoints.add(new HostAndPort("gateway.puppetteam.com", 16381));
	}

	public static void main(String[] args) throws InterruptedException {
		Set<String> sentinels = new HashSet<>();
		for (HostAndPort hnp : endpoints) {
			sentinels.add(hnp.toString());
		}

		System.out.println("trying to connect to sentinels: " + sentinels);

		try (JedisService jedisService = new JedisService(new JedisSentinelPool("lagen2-redis-cluster", sentinels))) {
			for (int i = 0; i < 1e9; i++) {
				try {
					Collection<String> obj = jedisService.smembers("lagen2:room:gameType");
					System.out.println(obj);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				Thread.sleep(100);
			}
		}
		System.exit(0);
	}

}
