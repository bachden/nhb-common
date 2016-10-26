package com.nhb.common.db.beans;

import java.util.UUID;

import com.nhb.common.annotations.Transparent;
import com.nhb.common.utils.Converter;

public class UUIDBean extends IdBinary16Bean {

	private static final long serialVersionUID = 3893364548396802191L;

	public UUIDBean() {
	}

	public UUIDBean(byte[] id) {
		super(id);
	}

	public UUIDBean(UUID id) {
		super(Converter.uuidToBytes(id));
	}

	@Transparent
	public UUID getUuid() {
		return Converter.bytesToUUID(getId());
	}

	public String getUuidString() {
		return this.getUuid().toString();
	}

	public void setUuidString(String uuidString) {
		this.setId(UUID.fromString(uuidString));
	}

	public void setId(UUID uuid) {
		this.setId(Converter.uuidToBytes(uuid));
	}

	public void autoId() {
		this.setId(UUID.randomUUID());
	}
}
