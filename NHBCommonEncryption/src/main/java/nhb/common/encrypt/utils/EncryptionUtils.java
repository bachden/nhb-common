package nhb.common.encrypt.utils;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {

	public static String HMAC_ALGORITHM = "hmacSHA256";

	/***
	 * Used for conversion in cases in which you *know* the encoding exists.
	 */
	public static final byte[] bytes(String in) {
		try {
			return in.getBytes("UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
			return null;
		}
	}

	public static final byte[] randomBytes(int len) {
		byte[] bytes = new byte[len];
		ThreadLocalRandom.current().nextBytes(bytes);
		return bytes;
	}

	public static final int BLOCKSIZE = 256 / 8;
	public static final byte[] HMAC_INPUT = bytes("Sync-AES_256_CBC-HMAC256");

	/**
	 * Step 1 of RFC 5869 Get sha256HMAC Bytes Input: salt (message), IKM (input
	 * keyring material) Output: PRK (pseudorandom key)
	 */
	public static byte[] hkdfExtract(byte[] salt, byte[] IKM) throws NoSuchAlgorithmException, InvalidKeyException {
		return digestBytes(IKM, makeHMACHasher(salt));
	}

	/**
	 * Step 2 of RFC 5869. Input: PRK from step 1, info, length. Output: OKM
	 * (output keyring material).
	 */
	public static byte[] hkdfExpand(byte[] prk, byte[] info, int len)
			throws NoSuchAlgorithmException, InvalidKeyException {
		Mac hmacHasher = makeHMACHasher(prk);

		byte[] T = {};
		byte[] Tn = {};

		int iterations = (int) Math.ceil(((double) len) / ((double) BLOCKSIZE));
		for (int i = 0; i < iterations; i++) {
			Tn = digestBytes(concatAll(Tn, info, hex2Byte(Integer.toHexString(i + 1))), hmacHasher);
			T = concatAll(T, Tn);
		}

		byte[] result = new byte[len];
		System.arraycopy(T, 0, result, 0, len);
		return result;
	}

	/**
	 * @param ikm
	 *            Initial Keying Material
	 * @param len
	 *            How many bytes?
	 * @param info
	 *            What sort of key are we deriving?
	 * @param salt
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] hkdf(byte[] ikm, int len, byte[] info, byte[] salt)
			throws InvalidKeyException, NoSuchAlgorithmException {
		return hkdfExpand(hkdfExtract(salt, ikm), info, len);
	}

	/**
	 * Make HMAC key Input: key (salt) Output: Key HMAC-Key
	 */
	public static Key makeHMACKey(byte[] key) {
		if (key == null || key.length == 0) {
			key = new byte[BLOCKSIZE];
		}
		return new SecretKeySpec(key, HMAC_ALGORITHM);
	}

	/**
	 * Make an HMAC hasher Input: Key hmacKey Ouput: An HMAC Hasher
	 */
	public static Mac makeHMACHasher(byte[] key) throws NoSuchAlgorithmException, InvalidKeyException {
		Mac hmacHasher = null;
		hmacHasher = Mac.getInstance(HMAC_ALGORITHM);

		// If Mac.getInstance doesn't throw NoSuchAlgorithmException, hmacHasher
		// is non-null.
		assert (hmacHasher != null);

		hmacHasher.init(makeHMACKey(key));
		return hmacHasher;
	}

	/**
	 * Hash bytes with given hasher Input: message to hash, HMAC hasher Output:
	 * hashed byte[].
	 */
	public static byte[] digestBytes(byte[] message, Mac hasher) {
		hasher.update(message);
		byte[] ret = hasher.doFinal();
		hasher.reset();
		return ret;
	}

	private static byte[] concatAll(byte[] first, byte[]... rest) {
		int totalLength = first.length;
		for (byte[] array : rest) {
			totalLength += array.length;
		}

		byte[] result = new byte[totalLength];
		int offset = first.length;

		System.arraycopy(first, 0, result, 0, offset);

		for (byte[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	private static byte[] hex2Byte(String str) {
		if (str.length() % 2 == 1) {
			str = "0" + str;
		}

		byte[] bytes = new byte[str.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}

	public static byte[] hashHmacSHA256(byte[] data, byte[] key) throws NoSuchAlgorithmException, InvalidKeyException {
		SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_ALGORITHM);
		Mac mac = Mac.getInstance(HMAC_ALGORITHM);
		mac.init(keySpec);
		return mac.doFinal(data);
	}

	public static boolean verifyHMAC(byte[] hmac, byte[] cipherText, byte[] akey) {
		try {
			byte[] blind = randomBytes(16);
			return Arrays.equals(hashHmacSHA256(hashHmacSHA256(cipherText, akey), blind), hashHmacSHA256(hmac, blind));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static byte[] sha256(byte[] data) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			return digest.digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}