package com.nhb.common.utils;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Converter {

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static byte[] hexToBytes(String hexString) {
		if (hexString != null) {
			if (hexString.startsWith("0x") || hexString.startsWith("0X")) {
				hexString = hexString.substring(2);
			}
			int len = hexString.length();
			byte[] data = new byte[len / 2];
			for (int i = 0; i < len; i += 2) {
				data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
						+ Character.digit(hexString.charAt(i + 1), 16));
			}
			return data;
		}
		return null;
	}

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			int v = bytes[i] & 0xFF;
			hexChars[i * 2] = hexArray[v >>> 4];
			hexChars[i * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	/**
	 * Use UuidUtils instead
	 * 
	 * @param uuid
	 * @return
	 */
	@Deprecated
	public static byte[] uuidToBytes(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}

	/**
	 * Use UuidUtils instead
	 * 
	 * @param uuid
	 * @return
	 */
	@Deprecated
	public static byte[] uuidToBytes(String uuidString) {
		return uuidToBytes(UUID.fromString(uuidString));
	}

	/**
	 * Use UuidUtils instead
	 * 
	 * @param uuid
	 * @return
	 */
	@Deprecated
	public static UUID bytesToUUID(byte[] bytes) {
		if (bytes.length != 16) {
			throw new IllegalArgumentException();
		}
		int i = 0;
		long msl = 0;
		for (; i < 8; i++) {
			msl = (msl << 8) | (bytes[i] & 0xFF);
		}
		long lsl = 0;
		for (; i < 16; i++) {
			lsl = (lsl << 8) | (bytes[i] & 0xFF);
		}
		return new UUID(msl, lsl);
	}

	/**
	 * Use UuidUtils instead
	 * 
	 * @param uuid
	 * @return
	 */
	@Deprecated
	public static String bytesToUUIDString(byte[] bytes) {
		return bytesToUUID(bytes).toString();
	}

	private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	private static Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);;

	/**
	 * Validate ip address with regular expression
	 * 
	 * @param ip
	 *            ip address for validation
	 * @return true valid ip address, false invalid ip address
	 */
	public static boolean isIpV4(final String ip) {
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();
	}

	public static long ip2Long(String ipAddress) {
		if (!isIpV4(ipAddress)) {
			throw new AssertionError("Invalid ip address");
		}

		long result = 0;
		String[] ipAddressInArray = ipAddress.split("\\.");
		for (int i = 3; i >= 0; i--) {
			long ip = Long.parseLong(ipAddressInArray[3 - i]);
			// left shifting 24,16,8,0 and bitwise OR
			// 1. 192 << 24
			// 1. 168 << 16
			// 1. 1 << 8
			// 1. 2 << 0
			result |= ip << (i * 8);
		}
		return result;
	}

}