package com.nhb.common.encrypt.rsa;

import static com.nhb.common.encrypt.rsa.KeyPairHelper.ALGORITHM;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAEncryptor {

	private KeyPairHelper keyPairHelper;

	public String encrypt(String input) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		return new String(this.encrypt(input.getBytes()));
	}

	public byte[] encrypt(byte[] input) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		// get an RSA cipher object and print the provider
		final Cipher cipher = Cipher.getInstance(ALGORITHM);
		// encrypt the plain text using the public key
		cipher.init(Cipher.ENCRYPT_MODE, this.keyPairHelper.getPublicKey());
		return cipher.doFinal(input);
	}

	public String decrypt(String input) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		return new String(this.decrypt(input.getBytes()));
	}

	public byte[] decrypt(byte[] input) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		// get an RSA cipher object and print the provider
		final Cipher cipher = Cipher.getInstance(ALGORITHM);
		// decrypt the text using the private key
		cipher.init(Cipher.DECRYPT_MODE, this.keyPairHelper.getPrivateKey());
		return cipher.doFinal(input);
	}

	public KeyPairHelper getKeyPairHelper() {
		return keyPairHelper;
	}

	public void setKeyPairHelper(KeyPairHelper keyPairHelper) {
		this.keyPairHelper = keyPairHelper;
	}
}
