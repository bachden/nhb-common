package com.nhb.messaging;

public enum MessagingModel {

	TASK_QUEUE, RPC, PUB_SUB, ROUTING, ROUTING_RPC, TOPIC, TOPIC_RPC;

	public static MessagingModel fromName(String name) {
		if (name != null) {
			for (MessagingModel type : values()) {
				if (type.name().equalsIgnoreCase(name)) {
					return type;
				}
			}
		}
		return null;
	}
}
