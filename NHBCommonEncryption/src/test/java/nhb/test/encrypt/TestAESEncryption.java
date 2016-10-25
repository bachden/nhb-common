package nhb.test.encrypt;

import nhb.common.encrypt.aes.AESEncryptor;

public class TestAESEncryption {

	public static void main(String[] args) {
		AESEncryptor encryptor = AESEncryptor.newInstance();
		encryptor.setPassword("puppet@team###");

		String message = "RThnlhRdyKBb/u8nWbducnC9bBjYzXi+Hyvf3zAOXnjmyK1nGzHvKZ2EnzuRKlfFe2QWHQbhywHR8Y+uc4ckiQP/FWBjH7vvmtk+j7LG3YEX/BSJVshdSJU4S5uxad458JAZFWoYm9FIHtOuK2yRG/gkLpmhjMc6LluGQBkoxvJ9O0TobaKJH6vg0RK1sCiHOEhBGecUGhv8MIQ4OvHpRV0K2GuNMuwPgEh6oLmYFCBVBMap6bNXtuzoW+GBjqWPK/SgJejKMkPEHQ7M0BuBTxJja8A/DNXSHD9FRJ7XeR32M8J/Qu5X12EHD+jh9WZ0//8Fta+Pux7DnS7E9130AhT72+N+QOoME8O7pV+6YAK/j/dstd7eBbrKJt6kmzVAa7hm2ES/iklt/jzuavTFCGI9UMjj7WgVefVviK3O5fiyq8ZBoLJ/R6h/ah6ruveMt0o/V9S2/NEXNhImBLyruA==";

		byte[] data = encryptor.decryptSimpleFromBase64(message);
		System.out.println(new String(data));
	}

}
