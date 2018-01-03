package com.nhb.common.encrypt.aes;

public class ThreadLocalAESEncryptor extends ThreadLocal<AESEncryptor> {

	private final byte[] password;

	public ThreadLocalAESEncryptor(byte[] password) {
		this.password = password;
	}

	public ThreadLocalAESEncryptor(String plainPassword) {
		this(plainPassword.getBytes());
	}

	@Override
	protected AESEncryptor initialValue() {
		AESEncryptor encryptor = AESEncryptor.newInstance();
		encryptor.setPassword(this.password);
		return encryptor;
	}
}
