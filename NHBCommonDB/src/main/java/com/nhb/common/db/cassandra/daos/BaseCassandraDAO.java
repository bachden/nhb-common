package com.nhb.common.db.cassandra.daos;

import java.io.Closeable;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import com.nhb.common.db.cassandra.CassandraDataSource;

public class BaseCassandraDAO implements CassandraDAO, Closeable {

	private CassandraDataSource dataSource;

	void setDataSource(CassandraDataSource dataSource) {
		this.dataSource = dataSource;
	}

	protected CassandraDataSource getDatasource() {
		return this.dataSource;
	}

	protected ResultSet execute(String cql) {
		return this.getDatasource().execute(cql);
	}

	protected ResultSet execute(Statement statement) {
		return this.getDatasource().execute(statement);
	}

	protected BoundStatement getStatement(String cql) {
		return new BoundStatement(this.getDatasource().getPreparedStatement(cql));
	}

	@Override
	public void close() {

	}
}
