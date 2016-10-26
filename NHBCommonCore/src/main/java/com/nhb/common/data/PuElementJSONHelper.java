package com.nhb.common.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minidev.json.parser.ParseException;

public class PuElementJSONHelper {

	private static Logger logger = LoggerFactory.getLogger(PuElementJSONHelper.class);

	public static PuElement fromJSON(String json) {
		if (json == null) {
			return null;
		}
		json = json.trim();
		try {
			if (json.startsWith("{")) {
				return PuObject.fromJSON(json);
			} else if (json.startsWith("[")) {
				return PuArrayList.fromJSON(json);
			}
		} catch (Exception e) {
			logger.warn("Cannot parse json as object or array, try with primitive type");
		}
		try {
			return PuValue.fromJSON(json);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}