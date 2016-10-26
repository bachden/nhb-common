package com.nhb.common.encrypt.rsa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/*
 * to working with keypair created by OpenSSL
 * 
 * Generate a 2048-bit RSA private key
 * >> $ openssl genrsa -out private_key.pem 2048
 * 
 * Convert private Key to PKCS#8 format (so Java can read it)
 * >> $ openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem \ -out private_key.der -nocrypt
 * 
 * Output public key portion in DER format (so Java can read it)
 * >> $ openssl rsa -in private_key.pem -pubout -outform DER -out public_key.der
 * 
 */

public class KeyPairHelper {

	static final String ALGORITHM = "RSA";

	private int keySize = 1024;

	private PrivateKey privateKey;
	private PublicKey publicKey;

	public PublicKey getPublicKey() {
		return this.publicKey;
	}

	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}

	private String base64Encode(byte[] bytes) {
		return Base64.encodeBase64String(bytes);
	}

	private byte[] base64Decode(String str) {
		return Base64.decodeBase64(str);
	}

	public void generateKey() throws NoSuchAlgorithmException {
		final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
		keyGen.initialize(keySize);
		final KeyPair key = keyGen.generateKeyPair();
		this.privateKey = key.getPrivate();
		this.publicKey = key.getPublic();
	}

	public void loadPrivateKey(File file) throws IOException, GeneralSecurityException {
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			this.loadPrivateKey(is);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public void loadPrivateKey(InputStream is) throws GeneralSecurityException, IOException {
		StringWriter sw = new StringWriter();
		IOUtils.copy(is, sw);
		this.loadPrivateKey(sw.toString());
	}

	public void loadPrivateKey(String base64EncryptedPrivateKey) throws GeneralSecurityException {
		byte[] clear = base64Decode(base64EncryptedPrivateKey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
		KeyFactory fact = KeyFactory.getInstance(ALGORITHM);
		PrivateKey priv = fact.generatePrivate(keySpec);
		Arrays.fill(clear, (byte) 0);
		this.privateKey = priv;
	}

	public void loadPublicKey(File file) throws IOException, GeneralSecurityException {
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			this.loadPublicKey(is);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public void loadPublicKey(InputStream is) throws IOException, GeneralSecurityException {
		StringWriter sw = new StringWriter();
		IOUtils.copy(is, sw);
		this.loadPublicKey(sw.toString());
	}

	public void loadPublicKey(String base64EncryptdPublicKey) throws GeneralSecurityException, IOException {
		byte[] data = base64Decode(base64EncryptdPublicKey);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
		KeyFactory fact = KeyFactory.getInstance(ALGORITHM);
		this.publicKey = fact.generatePublic(spec);
	}

	public String savePrivateKey() throws GeneralSecurityException {
		byte[] packed = this.privateKey.getEncoded();
		return base64Encode(packed);
	}

	public void savePrivateKey(String filePath) throws GeneralSecurityException {
		String privateKey = this.savePrivateKey();
		this.writeTextFile(filePath, privateKey);
	}

	public String savePublicKey() throws GeneralSecurityException {
		return base64Encode(this.publicKey.getEncoded());
	}

	public void savePublicKey(String filePath) throws GeneralSecurityException {
		String publicKey = this.savePublicKey();
		this.writeTextFile(filePath, publicKey);
	}

	private void writeTextFile(String destination, String content) {
		File file = new File(destination);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.err.println("create file `" + file.getAbsolutePath() + "` error");
				e.printStackTrace();
				return;
			}
		}
		try {
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (Exception e) {
			System.err.println("writing file error: ");
			e.printStackTrace();
		}
	}
}
