package com.nhb.common.db.sql;

import java.util.Map.Entry;

import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;

import java.util.Properties;

public class SQLDataSourceConfig extends BaseLoggable {

	private String name;
	private final Properties properties = new Properties();

	public void readPuObject(PuObjectRO data) {
		if (data.variableExists("name")) {
			this.setName(data.getString("name"));
		}
		if (data.variableExists("properties")) {
			PuValue propertiesValue = data.valueOf("properties");
			if (propertiesValue.getType() == PuDataType.PUOBJECT) {
				this.setProperties(data.getPuObject("properties"));
			} else {
				getLogger().warn("Reference config for SQLConfig must not contains file path reference");
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProperties(PuObject initParams) {
		for (Entry<String, PuValue> entry : initParams) {
			this.properties.setProperty(entry.getKey(), entry.getValue().getString());
		}
	}

	public void setProperties(Properties props) {
		if (props != null) {
			for (Object objKey : props.keySet()) {
				String key = objKey.toString();
				this.properties.put(key, props.get(key));
			}
		}
	}

	public Properties getProperties() {
		return this.properties;
	}
}
