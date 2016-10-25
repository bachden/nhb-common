package nhb.common.geo.utils;

import nhb.common.geo.GeoLocation;

/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
/*::                                                                         :*/
/*::  This routine calculates the distance between two points (given the     :*/
/*::  latitude/longitude of those points). It is being used to calculate     :*/
/*::  the distance between two locations using GeoDataSource (TM) prodducts  :*/
/*::                                                                         :*/
/*::  Definitions:                                                           :*/
/*::    South latitudes are negative, east longitudes are positive           :*/
/*::                                                                         :*/
/*::  Passed to function:                                                    :*/
/*::    lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees)  :*/
/*::    lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees)  :*/
/*::    unit = the unit you desire for results                               :*/
/*::           where: 'M' is statute miles (default)                         :*/
/*::                  'K' is kilometers                                      :*/
/*::                  'N' is nautical miles                                  :*/
/*::  Worldwide cities and other features databases with latitude longitude  :*/
/*::  are available at http://www.geodatasource.com                          :*/
/*::                                                                         :*/
/*::  For enquiries, please contact sales@geodatasource.com                  :*/
/*::                                                                         :*/
/*::  Official Web site: http://www.geodatasource.com                        :*/
/*::                                                                         :*/
/*::           GeoDataSource.com (C) All Rights Reserved 2015                :*/
/*::                                                                         :*/
/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

public class GeoUtils {

	public static void main(String[] args) throws java.lang.Exception {
		System.out.println(
				distance(new GeoLocation(32.9697, -96.80322), new GeoLocation(29.46786, -98.53506), "M") + " Miles\n");
		System.out.println(distance(new GeoLocation(32.9697, -96.80322), new GeoLocation(29.46786, -98.53506), "K")
				+ " Kilometers\n");
		System.out.println(distance(new GeoLocation(32.9697, -96.80322), new GeoLocation(29.46786, -98.53506), "N")
				+ " Nautical Miles\n");
	}

	/**
	 * Calculate distance between 2 geoLocation, return result in unit specific
	 * by M (Mile), K (Kilometre) and N (Nautical Miles)
	 * 
	 * @param loc1
	 *            first location
	 * @param loc2
	 *            second location
	 * @param unit
	 *            M, K or N
	 * @return value in unit
	 */
	public static double distance(GeoLocation loc1, GeoLocation loc2, String unit) {
		double theta = loc1.getLng() - loc2.getLng();
		double dist = Math.sin(deg2rad(loc1.getLat())) * Math.sin(deg2rad(loc2.getLat()))
				+ Math.cos(deg2rad(loc1.getLat())) * Math.cos(deg2rad(loc2.getLat())) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515; // in miles
		if (unit.equalsIgnoreCase("K")) {
			dist = dist * 1.609344;
		} else if (unit.equalsIgnoreCase("N")) {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts decimal degrees to radians :: */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	public static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees :: */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	public static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}
}
