package com.nhb.common.db.sql;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.nhb.common.BaseLoggable;

public class SQLDataSourceManager extends BaseLoggable {

	private final Map<String, DataSourceCreator> datasourceCreatorByName = new ConcurrentHashMap<String, DataSourceCreator>();
	private final Map<String, DataSource> nameToDataSourceMapping = new ConcurrentHashMap<String, DataSource>();
	private final Map<String, Properties> datasourceConfigByName = new ConcurrentHashMap<>();
	private String defaultDataSourceName;

	public SQLDataSourceManager() {
		this.datasourceCreatorByName.put("default", new DBPoolDataSourceCreator());
	}

	public void registerDataSourceCreator(DataSourceCreator creator) {
		this.datasourceCreatorByName.put(creator.getClass().getName(), creator);
	}

	public void setDefaultDataSource(String defaultDataSourceName) {
		this.defaultDataSourceName = defaultDataSourceName;
	}

	public void registerDataSource(String name, Properties props) throws Exception {
		this.datasourceConfigByName.put(name, props);
		this.nameToDataSourceMapping.put(name, this.createDataSource(props));
	}

	public Properties getConfig(String name) {
		return this.datasourceConfigByName.get(name);
	}

	public void deregisterDataSource(String name) {
		if (this.nameToDataSourceMapping.containsKey(name)) {
			this.nameToDataSourceMapping.remove(name);
		} else {
			getLogger().warn("{} datasource was not registered, do nothing", name, new Exception());
		}
	}

	public DataSource getDefaultDataSource() {
		return this.getDataSource(defaultDataSourceName);
	}

	public DataSource getDataSource(String name) {
		if (name != null && this.nameToDataSourceMapping.containsKey(name)) {
			return this.nameToDataSourceMapping.get(name);
		} else {
			getLogger().error("{} datasource was not registered", name, new Exception());
		}
		return null;
	}

	public final void registerDataSource(String name, InputStream configInputStream, String dbPrefix) {
		Properties props = null;
		props = new Properties();
		try {
			props.load(configInputStream);
			Properties config = new Properties();
			getLogger().debug("-------------- db config ------------");
			for (Object keyObj : props.keySet()) {
				String key = (String) keyObj;
				if (key.startsWith(dbPrefix)) {
					config.put(key.substring(dbPrefix.length() + 1), props.get(keyObj));
					getLogger().debug(key.substring(dbPrefix.length() + 1) + ": " + props.get(keyObj));
				}
			}
			getLogger().debug("-------------------------------------");
			this.registerDataSource(name, config);
		} catch (Exception e) {
			getLogger().error("cannot create data source for config: " + props != null ? props.toString() : " none", e);
		}
	}

	public final void registerDataSource(String name, String configFilePath, String dbPrefix) {
		InputStream inputStream = null;
		try {
			getLogger().info("loading db config from: " + configFilePath);
			inputStream = new FileInputStream(configFilePath);
			this.registerDataSource(name, inputStream, dbPrefix);
		} catch (FileNotFoundException e) {
			getLogger().error("file not found: " + configFilePath, e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					getLogger().error("cannot close file input stream: " + configFilePath + "...", e);
				}
			}
		}
	}

	protected DataSource createDataSource(Properties props) throws Exception {
		if (props == null) {
			return null;
		}
		if (props.contains("dataSourceCreatorClass")) {
			return this.datasourceCreatorByName.get(props.get("dataSourceCreatorClass")).createDataSource(props);
		}
		return this.datasourceCreatorByName.get("default").createDataSource(props);
	}
}