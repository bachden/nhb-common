package com.nhb.messaging.rabbit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuValue;
import com.nhb.messaging.MessagingModel;

public class RabbitMQQueueConfig {

	private int qos = -1;
	private String queueName;
	private boolean autoAck = false;

	private MessagingModel type;

	// exchange's properties
	private String exchangeName = "";
	private String exchangeType = "direct"; // fanout
	private String routingKey = "";

	// queue's properties
	private boolean durable = false;
	private boolean exclusive = false;
	private boolean autoDelete = false;
	private Map<String, Object> arguments;

	public void readPuObject(PuObject data) {
		if (data.variableExists("qos")) {
			this.setQos(data.getInteger("qos"));
		}
		if (data.variableExists("queueName")) {
			this.setQueueName(data.getString("queueName"));
		} else if (data.variableExists("name")) {
			this.setQueueName(data.getString("queueName"));
		}

		if (data.variableExists("autoAck")) {
			this.setAutoAck(data.getBoolean("autoAck"));
		}

		if (data.variableExists("type")) {
			this.setType(MessagingModel.fromName(data.getString("type")));
		} else if (data.variableExists("messagingmodel")) {
			this.setType(MessagingModel.fromName(data.getString("messagingmodel")));
		}

		if (data.variableExists("exchangeName")) {
			this.setExchangeName(data.getString("exchangeName"));
		}

		if (data.variableExists("exchangeType")) {
			this.setExchangeType(data.getString("exchangeType"));
		}

		if (data.variableExists("routingKey")) {
			this.setRoutingKey(data.getString("routingKey"));
		}

		if (data.variableExists("durable")) {
			this.setDurable(data.getBoolean("durable"));
		}

		if (data.variableExists("exclusive")) {
			this.setExclusive(data.getBoolean("exclusive"));
		}

		if (data.variableExists("autoDelete")) {
			this.setAutoDelete(data.getBoolean("autoDelete"));
		}

		if (data.variableExists("arguments")) {
			if (this.arguments == null) {
				this.arguments = new HashMap<>();
			}
			PuObject arguments = data.getPuObject("arguments");
			for (Entry<String, PuValue> entry : arguments) {
				this.arguments.put(entry.getKey(), entry.getValue().getData());
			}
		}
	}

	public boolean isDurable() {
		return durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}

	public boolean isAutoDelete() {
		return autoDelete;
	}

	public void setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
	}

	public Map<String, Object> getArguments() {
		return arguments;
	}

	public void setArguments(Map<String, Object> arguments) {
		this.arguments = arguments;
	}

	public MessagingModel getType() {
		return type;
	}

	public void setType(MessagingModel type) {
		this.type = type;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String name) {
		this.queueName = name;
	}

	public boolean isAutoAck() {
		return autoAck;
	}

	public void setAutoAck(boolean autoAck) {
		this.autoAck = autoAck;
	}

	public String getExchangeName() {
		return exchangeName;
	}

	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName == null ? "" : exchangeName.trim();
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public void setRoutingKey(String rountingKey) {
		this.routingKey = rountingKey;
	}

	public String getExchangeType() {
		return exchangeType;
	}

	private List<String> validTypes = Arrays.asList("fanout", "direct", "topic", "headers");

	public void setExchangeType(String exchangeType) {
		if (!validTypes.contains(exchangeType)) {
			throw new IllegalArgumentException(
					"Exchange type is not invalid: " + exchangeType + ", use the one in list: " + validTypes);
		}
		this.exchangeType = exchangeType;
	}

	public int getQos() {
		return qos;
	}

	public void setQos(int qos) {
		this.qos = qos;
	}
}