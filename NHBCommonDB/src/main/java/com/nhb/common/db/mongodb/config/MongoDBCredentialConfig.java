package com.nhb.common.db.mongodb.config;

import org.w3c.dom.Node;

import com.nhb.common.data.PuObjectRO;
import com.nhb.common.vo.UserNameAndPassword;

public class MongoDBCredentialConfig extends UserNameAndPassword {

	private String authDB = "admin";

	public MongoDBCredentialConfig() {
		// do nothing
	}

	public void readPuObject(PuObjectRO data) {
		if (data.variableExists("userName")) {
			this.setUserName(data.getString("userName"));
		} else if (data.variableExists("username")) {
			this.setUserName(data.getString("username"));
		}

		if (data.variableExists("password")) {
			this.setPassword(data.getString("password"));
		}

		if (data.variableExists("authDB")) {
			this.setAuthDB(data.getString("authDB"));
		} else if (data.variableExists("authDb")) {
			this.setAuthDB(data.getString("authDb"));
		}
	}

	public MongoDBCredentialConfig(Node node) {
		if (node != null) {
			Node currNode = node.getFirstChild();
			while (currNode != null) {
				if (currNode.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = currNode.getNodeName().toLowerCase();
					String nodeValue = currNode.getTextContent().trim();
					switch (nodeName) {
					case "username":
						this.setUserName(nodeValue);
						break;
					case "password":
						this.setPassword(nodeValue);
						break;
					case "authdb":
						this.setAuthDB(nodeValue);
						break;
					default:
						getLogger().warn("Unrecognize MongoDB credential config section: " + nodeName);
						break;
					}
				}
				currNode = currNode.getNextSibling();
			}
		}
	}

	public MongoDBCredentialConfig(String userName, String password) {
		this();
		this.setUserName(userName);
		this.setPassword(password);
	}

	public MongoDBCredentialConfig(String userName, String password, String authDB) {
		this(userName, password);
		this.setAuthDB(authDB);
	}

	public String getAuthDB() {
		return authDB;
	}

	public void setAuthDB(String authDB) {
		this.authDB = authDB;
	}
}
