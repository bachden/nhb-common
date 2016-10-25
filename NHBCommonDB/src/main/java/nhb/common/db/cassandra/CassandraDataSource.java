package nhb.common.db.cassandra;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;

import nhb.common.sync.SynchronizedExecutor;
import nhb.common.vo.HostAndPort;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

public class CassandraDataSource implements Closeable {

	private Cluster cluster;
	private Session session;
	private String keyspace;

	private final SynchronizedExecutor<Void> connector = new SynchronizedExecutor<>();
	private final Map<String, SynchronizedExecutor<PreparedStatement>> statementPreparers = new ConcurrentHashMap<>();

	private final Collection<HostAndPort> endpoints = new HashSet<>();
	private final Map<String, PreparedStatement> cachedStatements = new ConcurrentHashMap<>();

	public CassandraDataSource() {
		// do nothing
	}

	public CassandraDataSource(String keyspace) {
		this();
		this.setKeyspace(keyspace);
	}

	public CassandraDataSource(String keyspace, Collection<HostAndPort> endpoints) {
		this(endpoints);
		this.setKeyspace(keyspace);
	}

	public CassandraDataSource(String keyspace, HostAndPort... endpoints) {
		this(endpoints);
		this.setKeyspace(keyspace);
	}

	public CassandraDataSource(Collection<HostAndPort> endpoints) {
		this();
		this.endpoints.addAll(endpoints);
	}

	public CassandraDataSource(HostAndPort... endpoints) {
		this(Arrays.asList(endpoints));
	}

	public boolean isConnected() {
		if (this.session != null && this.session.isClosed()) {
			this.session = null;
		}
		return this.session != null;
	}

	public void reset() {
		this.close();
		this.endpoints.clear();
	}

	public void addEndpoints(Collection<HostAndPort> endpoints) {
		if (endpoints == null) {
			return;
		}
		if (this.isConnected()) {
			throw new IllegalStateException("Cannot add endpoint(s) when cluster is being connected");
		}
		this.endpoints.addAll(endpoints);
	}

	public void addEndpoints(HostAndPort... endpoints) {
		this.addEndpoints(Arrays.asList(endpoints));
	}

	public void removeEndpoint(HostAndPort... endpoints) {
		this.removeEndpoints(Arrays.asList(endpoints));
	}

	public void removeEndpoints(Collection<HostAndPort> endpoints) {
		if (this.isConnected()) {
			throw new IllegalStateException("Cannot remove endpoint(s) when cluster is being connected");
		}
		if (endpoints != null) {
			for (HostAndPort endpoint : endpoints) {
				this.endpoints.remove(endpoint);
			}
		}
	}

	public void connect(Collection<HostAndPort> endpoints) {
		this.endpoints.addAll(endpoints);
		this.connect();
	}

	public void connect() {
		try {
			this.connector.execute(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					if (CassandraDataSource.this.endpoints.size() == 0) {
						throw new RuntimeException("No endpoint defined");
					}
					Builder builder = new Builder();
					for (HostAndPort endpoint : CassandraDataSource.this.endpoints) {
						if (endpoint.getPort() <= 0) {
							builder.addContactPoint(endpoint.getHost());
						} else {
							builder.addContactPointsWithPorts(
									InetSocketAddress.createUnresolved(endpoint.getHost(), endpoint.getPort()));
						}
					}
					CassandraDataSource.this.cluster = builder.build();
					CassandraDataSource.this.session = CassandraDataSource.this.keyspace != null
							? CassandraDataSource.this.cluster.connect(CassandraDataSource.this.keyspace)
							: CassandraDataSource.this.cluster.connect();
					return null;
				}
			}).get();
		} catch (Exception e) {
			throw new RuntimeException("Error while connecting to cassandra cluster", e);
		}
	}

	@Override
	public void close() {
		if (!this.isConnected()) {
			return;
		}
		this.session.close();
		this.session = null;
		this.cachedStatements.clear();
	}

	public ResultSet execute(Statement statement) {
		if (!this.isConnected()) {
			this.connect();
		}
		return this.session.execute(statement);
	}

	public ResultSetFuture executeAsync(Statement statement) {
		if (!this.isConnected()) {
			this.connect();
		}
		return this.session.executeAsync(statement);
	}

	public ResultSet execute(String cql) {
		BoundStatement statement = new BoundStatement(this.getPreparedStatement(cql));
		return this.execute(statement);
	}

	public ResultSetFuture executeAsync(String cql) {
		BoundStatement statement = new BoundStatement(this.getPreparedStatement(cql));
		return this.executeAsync(statement);
	}

	public PreparedStatement getPreparedStatement(final String cql) {
		if (!this.isConnected()) {
			this.connect();
		}
		if (!this.cachedStatements.containsKey(cql)) {
			try {
				return this.getStatementPreparer(cql).execute(new Callable<PreparedStatement>() {

					@Override
					public PreparedStatement call() throws Exception {
						PreparedStatement result = session.prepare(cql);
						cachedStatements.put(cql, result);
						return result;
					}
				}).get();
			} catch (Exception e) {
				throw new RuntimeException("Error while preparing statement", e);
			}
		}
		return this.cachedStatements.get(cql);
	}

	private synchronized SynchronizedExecutor<PreparedStatement> getStatementPreparer(String cql) {
		if (!this.statementPreparers.containsKey(cql)) {
			this.statementPreparers.put(cql, new SynchronizedExecutor<PreparedStatement>());
		}
		return this.statementPreparers.get(cql);
	}

	public String getKeyspace() {
		return keyspace;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}
}
