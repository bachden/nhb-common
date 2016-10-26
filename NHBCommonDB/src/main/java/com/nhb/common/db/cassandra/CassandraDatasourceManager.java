package com.nhb.common.db.cassandra;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CassandraDatasourceManager {

	private final Map<String, CassandraDataSource> sources = new ConcurrentHashMap<>();

	public void init(CassandraDatasourceConfig... configs) {
		if (configs == null || configs.length == 0) {
			return;
		}
		this.init(Arrays.asList(configs));
	}

	public void init(Collection<CassandraDatasourceConfig> configs) {
		if (configs != null) {
			for (CassandraDatasourceConfig config : configs) {
				if (config == null) {
					continue;
				}
				this.sources.put(config.getName(),
						new CassandraDataSource(config.getKeyspace(), config.getEndpoints()));
			}
		}
	}

	public CassandraDataSource remove(String name) {
		return this.sources.remove(name);
	}

	public CassandraDataSource getDatasource(String name) {
		return this.sources.get(name);
	}
}
