package com.nhb.common.db.mongodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.Tag;
import com.mongodb.TagSet;
import com.nhb.common.BaseLoggable;
import com.nhb.common.db.mongodb.config.MongoDBConfig;
import com.nhb.common.db.mongodb.config.MongoDBCredentialConfig;
import com.nhb.common.vo.HostAndPort;

public class MongoDBSourceManager extends BaseLoggable {

	private Map<String, MongoDBConfig> configs;
	private Map<String, MongoClient> mongoClients;

	public MongoDBSourceManager() {
		this.configs = new HashMap<String, MongoDBConfig>();
		this.mongoClients = new HashMap<String, MongoClient>();
	}

	public MongoDBSourceManager(MongoDBConfig... configs) {
		this();
		this.addConfigs(configs);
	}

	public MongoDBSourceManager(List<MongoDBConfig> configs) {
		this();
		this.addConfigs(configs);
	}

	public void addConfigs(List<MongoDBConfig> configs) {
		if (configs != null) {
			for (int i = 0; i < configs.size(); i++) {
				this.addConfig(configs.get(i));
			}
		}
	}

	public void addConfigs(MongoDBConfig... configs) {
		if (configs != null) {
			for (int i = 0; i < configs.length; i++) {
				this.addConfig(configs[i]);
			}
		}
	}

	public void addConfig(MongoDBConfig config) {
		if (config != null) {
			if (config.getName() == null || config.getName().isEmpty()) {
				throw new RuntimeException("config's name cannot be empty");
			}
			if (this.configs.containsKey(config.getName())) {
				throw new RuntimeException("Config with name " + config.getName() + " is existing");
			}
			this.configs.put(config.getName(), config);
		}
	}

	public MongoDBConfig getConfig(String name) {
		return this.configs.get(name);
	}

	@SuppressWarnings("deprecation")
	public MongoClient getMongoClient(String name) {
		if (name != null) {
			if (!this.mongoClients.containsKey(name)) {
				MongoDBConfig config = this.getConfig(name);
				if (config != null) {
					List<ServerAddress> serverAddresses = new ArrayList<ServerAddress>();
					for (HostAndPort networkConfig : config.getEndpoints()) {
						serverAddresses.add(new ServerAddress(networkConfig.getHost(), networkConfig.getPort()));
					}
					List<MongoCredential> credentials = new ArrayList<MongoCredential>();
					for (MongoDBCredentialConfig credentialConfig : config.getCredentialConfigs()) {
						credentials.add(MongoCredential.createCredential(credentialConfig.getUserName(),
								credentialConfig.getAuthDB(), credentialConfig.getPassword().toCharArray()));
					}
					MongoClient mongoClient = null;
					if (credentials.size() > 0) {
						mongoClient = new MongoClient(serverAddresses, credentials);
					} else {
						mongoClient = new MongoClient(serverAddresses);
					}
					if (config.getReadPreference() != null) {
						List<TagSet> tagSetList = new ArrayList<>();
						List<Map<String, String>> tagSetListConfig = config.getReadPreference().getTagSetListConfig();
						for (Map<String, String> tagSetConfig : tagSetListConfig) {
							List<Tag> tags = new ArrayList<>();
							for (Entry<String, String> tagEntry : tagSetConfig.entrySet()) {
								tags.add(new Tag(tagEntry.getKey(), tagEntry.getValue()));
							}
							tagSetList.add(new TagSet(tags));
						}
						mongoClient.setReadPreference(
								ReadPreference.valueOf(config.getReadPreference().getName(), tagSetList));
					}
					this.mongoClients.put(config.getName(), mongoClient);

				} else {
					throw new RuntimeException("Unable to get understand why the config for name " + name
							+ " was null, please check the code's logic");
				}
			}
			return this.mongoClients.get(name);
		} else {
			getLogger().warn("name of the MongoClient instance never been null");
		}
		return null;
	}
}
