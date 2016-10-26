package com.nhb.common.db.mongodb.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import com.nhb.common.BaseLoggable;

public class MongoDBReadPreferenceConfig extends BaseLoggable {

	private String name;
	private final List<Map<String, String>> tagSetListConfig = new ArrayList<>();

	public MongoDBReadPreferenceConfig() {
		// do nothing
	}

	private Map<String, String> readTagSetNode(Node node) {
		Map<String, String> tagSetConfig = new HashMap<>();
		Node currNode = node.getFirstChild();
		while (currNode != null) {
			if (currNode.getNodeName().equalsIgnoreCase("tag")) {
				Node element = currNode.getFirstChild();
				String name = null;
				String value = null;
				while (element != null) {
					if (element.getNodeType() == Node.ATTRIBUTE_NODE) {
						if (element.getNodeName().equalsIgnoreCase("name")) {
							name = element.getNodeValue();
						} else if (element.getNodeName().equalsIgnoreCase("value")) {
							value = element.getNodeValue();
						}
					} else if (element.getNodeType() == Node.TEXT_NODE) {
						value = element.getTextContent().trim();
					} else if (element.getNodeType() == Node.ELEMENT_NODE) {
						if (element.getNodeName().equalsIgnoreCase("name")) {
							name = element.getTextContent().trim();
						} else if (element.getNodeName().equalsIgnoreCase("value")) {
							value = element.getTextContent().trim();
						}
					}
					element = element.getNextSibling();
				}
				tagSetConfig.put(name, value);
			} else {
				getLogger().warn("Unrecognize tag set entry: {}", currNode.getNodeName());
			}
			currNode = currNode.getNextSibling();
		}
		return tagSetConfig;
	}

	public MongoDBReadPreferenceConfig(Node node) {
		Node currNode = node.getFirstChild();
		while (currNode != null) {
			if (currNode.getNodeType() == 1) {
				String nodeName = currNode.getNodeName().toLowerCase();
				switch (nodeName) {
				case "name": {
					this.setName(currNode.getTextContent().trim());
					break;
				}
				case "tagsetlist": {
					Node tagSetNode = currNode.getFirstChild();
					while (tagSetNode != null) {
						if (tagSetNode.getNodeName().equalsIgnoreCase("tagSet")) {
							this.tagSetListConfig.add(this.readTagSetNode(tagSetNode));
						} else {
							getLogger().warn("Unrecognized MongoDB Read Preference config section: {}", tagSetNode);
						}
						tagSetNode = tagSetNode.getNextSibling();
					}
					break;
				}
				default:
					break;
				}
			}
			currNode = currNode.getNextSibling();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public List<Map<String, String>> getTagSetListConfig() {
		return tagSetListConfig;
	}

}
