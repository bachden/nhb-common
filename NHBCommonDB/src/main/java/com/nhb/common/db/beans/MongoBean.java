package com.nhb.common.db.beans;

import org.bson.Document;
import org.bson.types.ObjectId;

public interface MongoBean {
	
	ObjectId getObjectId();

	Document toDocument();
}
