package com.nhb.common.vo;

import com.nhb.common.data.PuObjectRO;
import com.nhb.common.exception.InvalidFormatException;

public class HostAndPort {

	private boolean isMaster = false;
	private String host = "localhost";
	private int port = 0;
	private boolean useSSL;

	public void readPuObject(PuObjectRO data) {
		if (data.variableExists("host")) {
			this.setHost(data.getString("host"));
		}
		if (data.variableExists("port")) {
			this.setPort(data.getInteger("port"));
		}
		if (data.variableExists("isMaster")) {
			this.setMaster(data.getBoolean("isMaster"));
		} else if (data.variableExists("master")) {
			this.setMaster(data.getBoolean("master"));
		}
		if (data.variableExists("ssl")) {
			this.setUseSSL(data.getBoolean("ssl"));
		} else if (data.variableExists("SSL")) {
			this.setUseSSL(data.getBoolean("SSL"));
		}
	}

	public HostAndPort() {
		// do nothing
	}

	public HostAndPort(String host) {
		this();
		this.setHost(host);
	}

	public HostAndPort(int port) {
		this();
		this.setPort(port);
	}

	public HostAndPort(String host, int port) {
		this();
		this.setHost(host);
		this.setPort(port);
	}

	public HostAndPort(String host, boolean useSSL) {
		this();
		this.setHost(host);
		this.setUseSSL(useSSL);
	}

	public HostAndPort(String host, int port, boolean useSSL) {
		this(host, port);
		this.setUseSSL(useSSL);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return this.getHost() + (this.getPort() > 0 ? (":" + this.getPort()) : "");
	}

	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	public boolean isUseSSL() {
		return useSSL;
	}

	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof HostAndPort) {
			HostAndPort other = (HostAndPort) obj;
			return (other.getHost() == null ? this.getHost() == null : other.getHost().equals(this.getHost()))
					&& (this.getPort() == other.getPort());
		}
		return false;
	}

	public static HostAndPort fromString(String str) {
		if (str != null && str.trim().length() > 0) {
			if (str.indexOf(":") >= 0) {
				String[] arr = str.split(":");
				if (arr.length != 2) {
					throw new InvalidFormatException("String " + str + " format is invalid for HostAndPort");
				} else {
					return new HostAndPort(arr[0].trim(), Integer.valueOf(arr[1]));
				}
			} else {
				return new HostAndPort(str);
			}
		}
		return null;
	}
}
