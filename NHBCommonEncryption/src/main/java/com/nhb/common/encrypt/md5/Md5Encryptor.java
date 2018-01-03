package com.nhb.common.encrypt.md5;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Md5Encryptor {

	public static final byte[] encrypt(byte[] data) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		md.update(data);
		return md.digest();
	}

	public static final byte[] encrypt(byte[]... data) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		for (byte[] bytes : data) {
			md.update(bytes);
		}
		return md.digest();
	}

	public static final String encryptToHex(byte[] data) {
		byte[] hash = encrypt(data);
		StringBuilder hexString = new StringBuilder();
		for (int i = 0; i < hash.length; i++) {
			if ((0xff & hash[i]) < 0x10) {
				hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
			} else {
				hexString.append(Integer.toHexString(0xFF & hash[i]));
			}
		}
		return hexString.toString();
	}

	public final static boolean validate(byte[] validSignature, byte[] data, byte[] salt) {
		return Arrays.equals(validSignature, encrypt(data, salt));
	}

}
