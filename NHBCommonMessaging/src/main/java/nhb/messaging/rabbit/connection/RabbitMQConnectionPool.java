package nhb.messaging.rabbit.connection;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Address;

import net.jodah.lyra.ConnectionOptions;
import net.jodah.lyra.Connections;
import net.jodah.lyra.config.Config;
import net.jodah.lyra.config.ConfigurableConnection;
import net.jodah.lyra.config.RecoveryPolicies;
import net.jodah.lyra.config.RetryPolicies;
import nhb.common.BaseLoggable;
import nhb.common.vo.HostAndPort;
import nhb.common.vo.UserNameAndPassword;

public class RabbitMQConnectionPool extends BaseLoggable implements Closeable {

	private UserNameAndPassword credential;
	private List<HostAndPort> endpoints;

	private Config config;
	private ConnectionOptions options;
	private final Set<ConfigurableConnection> connections = new HashSet<>();

	public RabbitMQConnectionPool() {
		this.config = new Config();
		this.config.withRecoveryPolicy(RecoveryPolicies.recoverAlways());
		this.config.withRetryPolicy(RetryPolicies.retryAlways());

		this.options = new ConnectionOptions();
	}

	public void init() {
		if (this.getCredential() != null) {
			getLogger().debug("connect with username and password: {}, {}", this.getCredential().getUserName(),
					this.getCredential().getPassword());
			this.options.withUsername(this.getCredential().getUserName());
			this.options.withPassword(this.getCredential().getPassword());
		}
		if (this.getEndpoints() != null) {
			Address[] addresses = new Address[this.getEndpoints().size()];
			for (int i = 0; i < addresses.length; i++) {
				HostAndPort endpoint = this.getEndpoints().get(i);
				getLogger().debug("add address: {}", endpoint);
				if (endpoint != null && endpoint.getHost() != null && !endpoint.getHost().trim().isEmpty()
						&& endpoint.getPort() > 0) {
					addresses[i] = new Address(endpoint.getHost(), endpoint.getPort());
				}
			}
			this.options.withAddresses(addresses);
		}
	}

	public RabbitMQConnection getConnection() {

		long sleepTime = -1;
		ConfigurableConnection connection = null;

		try {
			connection = Connections.create(this.options, this.config);
		} catch (IOException ioException) {
			getLogger().debug("connection error", ioException);
			sleepTime = 5000;
		} catch (TimeoutException timeoutException) {
			sleepTime = 1000;
		}

		if (sleepTime > 0) {
			try {
				getLogger().debug("sleep {}ms to get connection..", sleepTime);
				Thread.sleep(sleepTime);
				return getConnection();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		if (connection != null) {
			this.connections.add(connection);
		}
		return new RabbitMQConnection(connection);
	}

	public UserNameAndPassword getCredential() {
		return credential;
	}

	public void setCredential(UserNameAndPassword credential) {
		this.credential = credential;
	}

	public List<HostAndPort> getEndpoints() {
		return endpoints;
	}

	public void addEndpoints(Collection<HostAndPort> endpoints) {
		if (endpoints == null) {
			return;
		}
		if (this.endpoints == null) {
			this.endpoints = new ArrayList<>();
		}
		this.endpoints.addAll(endpoints);
	}

	public void addEndpoints(HostAndPort... endpoints) {
		if (endpoints == null) {
			return;
		}
		if (this.endpoints == null) {
			this.endpoints = new ArrayList<>();
		}
		this.endpoints.addAll(Arrays.asList(endpoints));
	}

	@Override
	public void close() throws IOException {
		for (ConfigurableConnection connection : this.connections) {
			connection.close();
		}
	}
}
