package nhb.common.encrypt.sha;

import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;

public class SHAEncryptor {

	public static byte[] sha512(byte[] data) {
		if (data != null) {
			try {
				return MessageDigest.getInstance("SHA-512").digest(data);
			} catch (Exception e) {
				throw new RuntimeException("Error while getting MessageDigest for SHA-512 algorithm");
			}
		}
		return null;
	}

	public static byte[] sha512(String data) {
		if (data != null) {
			return sha512(data.getBytes());
		}
		return null;
	}

	public static String sha512Hex(byte[] data) {
		if (data != null) {
			return Hex.encodeHexString(sha512(data));
		}
		return null;
	}

	public static String sha512Hex(String data) {
		if (data != null) {
			return sha512Hex(data.getBytes());
		}
		return null;
	}
}
