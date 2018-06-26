package com.nhb.common.db.models;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.hazelcast.core.HazelcastInstance;
import com.mongodb.MongoClient;
import com.nhb.common.cache.jedis.JedisService;
import com.nhb.common.db.cassandra.daos.CassandraDAOFactory;
import com.nhb.common.db.sql.DBIAdapter;

public class ModelFactory {

	private ClassLoader classLoader = this.getClass().getClassLoader();

	private DBIAdapter dbAdapter;
	private JedisService jedisService;
	private HazelcastInstance hazelcast;
	private MongoClient mongoClient;

	private final Map<String, Object> environmentVariables = new ConcurrentHashMap<>();

	private final Map<String, String> implMap = new ConcurrentHashMap<>();

	private CassandraDAOFactory cassandraDAOFactory;

	public ModelFactory() {
		// do nothing;
	}

	public void addClassImplMapping(Properties props) {
		if (props != null) {
			for (Object obj : props.keySet()) {
				String key = String.valueOf(obj);
				this.implMap.put(key.trim(), props.getProperty(key).trim());
			}
		}
	}

	public void addClassImplMapping(Map<String, String> map) {
		if (map != null) {
			for (String key : map.keySet()) {
				this.implMap.put(key.trim(), map.get(key).trim());
			}
		}
	}

	public void removeClassImplMapping(String key) {
		this.implMap.remove(key);
	}

	public ModelFactory(DBIAdapter dbAdapter) {
		this.setDbAdapter(dbAdapter);
	}

	public ModelFactory(DBIAdapter dbAdapter, JedisService jedisService) {
		this.setDbAdapter(dbAdapter);
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractModel> T getModel(String modelClass) {
		try {
			final Class<?> clazz;
			if (this.implMap.containsKey(modelClass)) {
				clazz = (Class<?>) this.classLoader.loadClass(this.implMap.get(modelClass));
			} else {
				clazz = (Class<?>) this.classLoader.loadClass(modelClass);
			}
			T model = (T) clazz.newInstance();
			model.setDbAdapter(this.getDbAdapter());
			model.setJedisService(this.getJedisService());
			model.setHazelcast(this.getHazelcast());
			model.setMongoClient(this.getMongoClient());
			model.setCassandraDAOFactory(this.getCassandraDAOFactory());
			model.silentInit(this.environmentVariables);
			return model;
		} catch (Exception ex) {
			throw new RuntimeException("Create model instance error: ", ex);
		}
	}

	@Deprecated
	public <T extends AbstractModel> T newModel(Class<T> modelClass) {
		return this.getModel(modelClass.getName());
	}

	public <T extends AbstractModel> T getModel(Class<T> modelClass) {
		return this.getModel(modelClass.getName());
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
		if (this.classLoader != null) {
			if (this.cassandraDAOFactory != null) {
				this.cassandraDAOFactory.setClassLoader(this.classLoader);
			}
		}
	}

	public HazelcastInstance getHazelcast() {
		return hazelcast;
	}

	public void setHazelcast(HazelcastInstance hazelcast) {
		this.hazelcast = hazelcast;
	}

	public DBIAdapter getDbAdapter() {
		return dbAdapter;
	}

	public void setDbAdapter(DBIAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}

	public MongoClient getMongoClient() {
		return mongoClient;
	}

	public void setMongoClient(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	public JedisService getJedisService() {
		return jedisService;
	}

	public void setJedisService(JedisService jedisService) {
		this.jedisService = jedisService;
	}

	public CassandraDAOFactory getCassandraDAOFactory() {
		return cassandraDAOFactory;
	}

	public void setCassandraDAOFactory(CassandraDAOFactory cassandraDAOFactory) {
		this.cassandraDAOFactory = cassandraDAOFactory;
		if (this.cassandraDAOFactory != null && this.classLoader != null) {
			this.cassandraDAOFactory.setClassLoader(this.classLoader);
		}
	}

	public void setEnvironmentVariable(String key, Object value) {
		this.environmentVariables.put(key, value);
	}

	public void removeEnvironmentVariable(String key) {
		this.environmentVariables.remove(key);
	}

	public Object getEnvironmentVariable(String key) {
		return this.environmentVariables.get(key);
	}
}
