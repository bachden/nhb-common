package nhb.common.encrypt.trippledes;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import nhb.common.encrypt.utils.EncryptionUtils;

public class TrippleDesEncryptor {
	private static final String ALGORITHM = "DESede";
	private static final String DES = "DESede/CBC/PKCS5Padding";
	private static final int KEY_BYTE_SIZE = 24;
	private static final int IV_BYTE_SIZE = 8;
	private byte[] initVector;
	private byte[] password;

	public byte[] encrypt(byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		Cipher c3des = Cipher.getInstance(DES);
		SecretKeySpec myKey = new SecretKeySpec(password, ALGORITHM);
		IvParameterSpec ivspec = new IvParameterSpec(initVector);
		c3des.init(Cipher.ENCRYPT_MODE, myKey, ivspec);
		return c3des.doFinal(data);
	}

	public byte[] decrypt(byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		Cipher decipher = Cipher.getInstance(DES);
		SecretKeySpec myKey = new SecretKeySpec(password, ALGORITHM);
		IvParameterSpec ivspec = new IvParameterSpec(initVector);
		decipher.init(Cipher.DECRYPT_MODE, myKey, ivspec);
		return decipher.doFinal(data);
	}

	public String encryptToBase64(String data) throws Exception {
		byte[] plaintext = data.getBytes();
		byte[] cipherText = encrypt(plaintext);
		return encodeBase64String(cipherText);
	}

	public String decryptFromBase64(String base64String) throws Exception {
		byte[] encData = decodeBase64(base64String);
		byte[] plainText = decrypt(encData);
		return new String(plainText);
	}

	private static String encodeBase64String(byte[] cipherText) {
		return Base64.getEncoder().encodeToString(cipherText);
	}

	private static byte[] decodeBase64(String base64String) {
		return Base64.getDecoder().decode(base64String);
	}

	public byte[] getInitVector() {
		return initVector;
	}

	public void setInitVector(byte[] initVector) {
		if (initVector.length != IV_BYTE_SIZE) {
			throw new RuntimeException(
					"init vector's length " + initVector.length + "is invalid, must be " + IV_BYTE_SIZE);
		}
		this.initVector = initVector;
	}

	public void setInitVectorBase64(String base64Vector) {
		setInitVector(decodeBase64(base64Vector));
	}

	public byte[] getPassword() {
		return password;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}

	public void setPasswordBase64(String base64Passsword) {
		this.password = decodeBase64(base64Passsword);
	}

	public static byte[] generateIv() {
		return EncryptionUtils.randomBytes(IV_BYTE_SIZE);
	}

	public static byte[] generatePassword() {
		return EncryptionUtils.randomBytes(KEY_BYTE_SIZE);
	}

	public static String generatePasswordBase64() {
		return encodeBase64String(generatePassword());
	}
}
