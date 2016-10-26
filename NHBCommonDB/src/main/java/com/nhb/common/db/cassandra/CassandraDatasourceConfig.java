package com.nhb.common.db.cassandra;

import java.util.Collection;

import com.nhb.common.vo.HostAndPort;

public interface CassandraDatasourceConfig {

	public Collection<HostAndPort> getEndpoints();

	public String getName();

	public String getKeyspace();
}
