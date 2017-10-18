package com.nhb.common.db.beans;

import java.util.UUID;

import com.nhb.common.annotations.Transparent;
import com.nhb.common.utils.UuidUtils;

public class UUIDBean extends IdBinary16Bean {

	private static final long serialVersionUID = 3893364548396802191L;

	public UUIDBean() {
	}

	public UUIDBean(byte[] id) {
		super(id);
	}

	public UUIDBean(UUID id) {
		super(UuidUtils.uuidToBytes(id));
	}

	@Transparent
	public UUID getUuid() {
		return UuidUtils.bytesToUUID(getId());
	}

	public String getUuidString() {
		return this.getUuid().toString();
	}

	public void setUuidString(String uuidString) {
		this.setId(UUID.fromString(uuidString));
	}

	public void setId(UUID uuid) {
		this.setId(UuidUtils.uuidToBytes(uuid));
	}

	public void autoId() {
		this.setId(UuidUtils.timebasedUuid());
	}
}
