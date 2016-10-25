package nhb.common.db.mongodb.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nhb.common.data.PuArray;
import nhb.common.data.PuDataType;
import nhb.common.data.PuObject;
import nhb.common.data.PuObjectRO;
import nhb.common.data.PuValue;
import nhb.common.data.exception.InvalidDataException;
import nhb.common.vo.HostAndPort;

public class MongoDBConfig {

	private String name;
	private List<MongoDBCredentialConfig> credentialConfigs = new ArrayList<MongoDBCredentialConfig>();
	private List<HostAndPort> endpoints = new ArrayList<HostAndPort>();

	private MongoDBReadPreferenceConfig readPreference;

	public void readPuObject(PuObjectRO data) {
		if (data.variableExists("name")) {
			this.setName(data.getString("name"));
		}
		if (data.variableExists("credentials")) {
			PuArray arr = data.getPuArray("credentials");
			for (PuValue value : arr) {
				if (value == null || value.getType() != PuDataType.PUOBJECT) {
					throw new InvalidDataException("entry for credential must be puobject");
				}
				PuObject entry = value.getPuObject();
				if (entry != null) {
					MongoDBCredentialConfig vo = new MongoDBCredentialConfig();
					vo.readPuObject(entry);
					this.credentialConfigs.add(vo);
				}
			}
		}

		PuArray endpoints = null;
		if (data.variableExists("endpoints")) {
			endpoints = data.getPuArray("endpoints");
		} else if (data.variableExists("endpoint")) {
			endpoints = data.getPuArray("endpoint");
		}
		if (endpoints != null) {
			for (PuValue value : endpoints) {
				if (value == null || value.getType() != PuDataType.PUOBJECT) {
					throw new InvalidDataException("entry for credential must be puobject");
				}
				PuObject entry = value.getPuObject();
				if (entry != null) {
					HostAndPort vo = new HostAndPort();
					vo.readPuObject(entry);
					this.endpoints.add(vo);
				}
			}
		}
	}

	public MongoDBConfig() {
		// do nothing
	}

	public MongoDBConfig(String name) {
		this();
		this.setName(name);
	}

	public MongoDBConfig(String name, HostAndPort... networkConfigs) {
		this(name);
		this.getEndpoints().addAll(Arrays.asList(networkConfigs));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<HostAndPort> getEndpoints() {
		return endpoints;
	}

	public void addEndpoint(String host, int port) {
		this.addEndpoint(new HostAndPort(host, port));
	}

	public void addEndpoint(HostAndPort config) {
		if (config != null) {
			this.getEndpoints().add(config);
		}
	}

	public void addEndpoints(HostAndPort... configs) {
		if (configs != null) {
			this.getEndpoints().addAll(Arrays.asList(configs));
		}
	}

	public List<MongoDBCredentialConfig> getCredentialConfigs() {
		return credentialConfigs;
	}

	public void addCredentialConfig(MongoDBCredentialConfig config) {
		if (config != null) {
			this.getCredentialConfigs().add(config);
		}
	}

	public void addCredentialConfigs(MongoDBCredentialConfig... configs) {
		if (configs != null) {
			this.getCredentialConfigs().addAll(Arrays.asList(configs));
		}
	}

	public MongoDBReadPreferenceConfig getReadPreference() {
		return readPreference;
	}

	public void setReadPreference(MongoDBReadPreferenceConfig readPreference) {
		this.readPreference = readPreference;
	}
}
