package nhb.common.db.models;

import java.util.HashMap;
import java.util.Map;

import org.skife.jdbi.v2.Handle;

import com.hazelcast.core.HazelcastInstance;
import com.mongodb.MongoClient;

import nhb.common.BaseLoggable;
import nhb.common.cache.jedis.JedisService;
import nhb.common.db.cassandra.daos.CassandraDAOFactory;
import nhb.common.db.sql.DBIAdapter;
import nhb.common.db.sql.daos.AbstractDAO;

public class AbstractModel extends BaseLoggable {

	private DBIAdapter dbAdapter;
	private JedisService jedisService;
	private HazelcastInstance hazelcast;
	private MongoClient mongoClient;
	private CassandraDAOFactory cassandraDAOFactory;
	private Map<String, Object> environmentVariables = new HashMap<>();

	/**
	 * This method called after this model created in ModelFactory and
	 * everything was set
	 * 
	 * Internal modifier let this invisible with outside method
	 */
	void silentInit(Map<String, Object> environmentVariables) {
		// setting environment variables injected by model factory
		this.environmentVariables = environmentVariables;
		// call protected method init
		this.init();
	}

	protected Map<String, Object> getEnvironmentVariables() {
		return this.environmentVariables;
	}

	protected void init() {
		// do nothing
	}

	protected DBIAdapter getDbAdapter() {
		return dbAdapter;
	}

	void setDbAdapter(DBIAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}

	@SuppressWarnings("unchecked")
	protected <T extends AbstractDAO> T openDAO(Class<T> daoClass) {
		assert daoClass != null;
		try {
			Class<T> clazz = (Class<T>) this.getClass().getClassLoader().loadClass(daoClass.getName());
			return this.dbAdapter.openDAO(clazz);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("class not found: " + daoClass.getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T extends AbstractDAO> T openDAO(Class<T> daoClass, Handle handle) {
		assert daoClass != null;
		try {
			Class<T> clazz = (Class<T>) this.getClass().getClassLoader().loadClass(daoClass.getName());
			return handle.attach(clazz);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("class not found: " + daoClass.getName(), e);
		}
	}

	protected Handle newHandle() {
		return this.dbAdapter.newHandle();
	}

	protected HazelcastInstance getHazelcast() {
		return hazelcast;
	}

	void setHazelcast(HazelcastInstance hazelcast) {
		this.hazelcast = hazelcast;
	}

	protected MongoClient getMongoClient() {
		return mongoClient;
	}

	void setMongoClient(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	protected JedisService getJedisService() {
		return jedisService;
	}

	void setJedisService(JedisService jedisService) {
		this.jedisService = jedisService;
	}

	protected CassandraDAOFactory getCassandraDAOFactory() {
		return cassandraDAOFactory;
	}

	void setCassandraDAOFactory(CassandraDAOFactory cassandraDAOFactory) {
		this.cassandraDAOFactory = cassandraDAOFactory;
	}
}
