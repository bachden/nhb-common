package com.nhb.common.geo;

import java.io.Serializable;

import com.nhb.common.data.PuObject;

public class GeoLocation implements Serializable {

	private static final long serialVersionUID = -7236318175988473839L;

	private double lat;
	private double lng;

	public GeoLocation() {
		// do nothing
	}

	public GeoLocation(double lat, double lng) {
		this();
		this.lat = lat;
		this.lng = lng;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public PuObject toPuObject() {
		PuObject puo = new PuObject();
		puo.setDouble("lat", this.getLat());
		puo.setDouble("lng", this.getLng());
		return puo;
	}

	public double[] toDblArray() {
		return new double[] { this.getLat(), this.getLng() };
	}

	public static GeoLocation fromPuObject(PuObject puo) {
		if (puo == null) {
			return null;
		}
		GeoLocation result = new GeoLocation();
		if (puo.variableExists("lat")) {
			result.setLat(puo.getDouble("lat"));
		}
		if (puo.variableExists("lng")) {
			result.setLng(puo.getDouble("lon"));
		}
		return result;
	}
}
