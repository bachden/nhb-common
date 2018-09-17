package com.nhb.common.db.sql;

import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import com.nhb.common.BaseLoggable;

import snaq.db.DBPoolDataSource;

class DBPoolDataSourceCreator extends BaseLoggable implements DataSourceCreator {

	@Override
	public DataSource createDataSource(Properties props) throws Exception {
		if (props != null) {
			DBPoolDataSource ds = new DBPoolDataSource();
			ds.registerShutdownHook();

			for (Object keyObj : props.keySet()) {
				String refName = (String) keyObj;
				String refValue = (String) props.get(refName);
				if (refName.equalsIgnoreCase("description")) {
					ds.setDescription(refValue);
					getLogger().trace("Set DataSource description: " + refValue);
				} else if ((refName.equalsIgnoreCase("user")) || (refName.equalsIgnoreCase("username"))) {
					ds.setUser(refValue);
					getLogger().trace("Set DataSource username: " + refValue);
				} else if (refName.equalsIgnoreCase("password")) {
					ds.setPassword(refValue);
					getLogger().trace("Set DataSource password");
				} else if (refName.equalsIgnoreCase("driverClassName")) {
					ds.setDriverClassName(refValue);
					getLogger().trace("Set DataSource driver class name: " + refValue);
				} else if (refName.equalsIgnoreCase("url")) {
					ds.setUrl(refValue);
					getLogger().trace("Set DataSource URL: " + refValue);
				} else if (refName.equalsIgnoreCase("passwordDecoderClassName")) {
					ds.setPasswordDecoderClassName(refValue);
					getLogger().trace("Set DataSource PasswordDecoder class name: " + refValue);
				} else if (refName.equalsIgnoreCase("validatorClassName")) {
					ds.setValidatorClassName(refValue);
					getLogger().trace("Set DataSource ConnectionValidator class name: " + refValue);
				} else if (refName.equalsIgnoreCase("validationQuery")) {
					ds.setValidationQuery(refValue);
					getLogger().trace("Set DataSource validation query: " + refValue);
				} else if (refName.equalsIgnoreCase("minPool")) {
					try {
						ds.setMinPool(Integer.parseInt(refValue));
					} catch (NumberFormatException nfx) {
						throw new NamingException("Invalid '" + refName + "' value: " + refValue);
					}
					getLogger().trace("Set DataSource minPool: " + refValue);
				} else if (refName.equalsIgnoreCase("maxPool")) {
					try {
						ds.setMaxPool(Integer.parseInt(refValue));
					} catch (NumberFormatException nfx) {
						throw new NamingException("Invalid '" + refName + "' value: " + refValue);
					}
					getLogger().trace("Set DataSource maxPool: " + refValue);
				} else if (refName.equalsIgnoreCase("maxSize")) {
					try {
						ds.setMaxSize(Integer.parseInt(refValue));
					} catch (NumberFormatException nfx) {
						throw new NamingException("Invalid '" + refName + "' value: " + refValue);
					}
					getLogger().trace("Set DataSource minSize: " + refValue);
				} else if (refName.equalsIgnoreCase("idleTimeout")) {
					try {
						ds.setIdleTimeout(Integer.parseInt(refValue));
					} catch (NumberFormatException nfx) {
						throw new NamingException("Invalid '" + refName + "' value: " + refValue);
					}
					getLogger().trace("Set DataSource idleTimeout: " + refValue);
				} else if (refName.equalsIgnoreCase("loginTimeout")) {
					try {
						ds.setLoginTimeout(Integer.parseInt(refValue));
					} catch (NumberFormatException nfx) {
						throw new NamingException("Invalid '" + refName + "' value: " + refValue);
					}
					getLogger().trace("Set DataSource idleTimeout: " + refValue);
				}
			}
			return ds;
		}
		return null;
	}

}
