package com.nhb.common.db.beans;

import org.bson.types.ObjectId;

import com.nhb.common.annotations.Transparent;
import com.nhb.common.data.PuObject;

public abstract class AbstractMongoBean extends AbstractBean implements MongoBean {

	private static final long serialVersionUID = 3202015216706335287L;

	@Transparent
	private ObjectId objectId;

	public void setObjectId(ObjectId objectId) {
		this.objectId = objectId;
	}

	@Override
	@Transparent
	public ObjectId getObjectId() {
		return this.objectId;
	}

	public PuObject toPuObject() {
		return null;
	}

	public String getObjectIdHex() {
		return this.objectId.toHexString();
	}
}
