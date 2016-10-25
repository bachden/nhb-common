package nhb.common.encrypt.rsa;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

import org.apache.commons.codec.binary.Base64;

public class SignatureHelper {

	static final String ALGORITHM = "SHA1WithRSA";

	private KeyPairHelper keyPairHelper;

	public byte[] sign(String data) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
		return this.sign(data.getBytes());
	}

	public byte[] sign(byte[] data) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
		Signature sig = Signature.getInstance(ALGORITHM);
		sig.initSign(keyPairHelper.getPrivateKey());
		sig.update(data);
		return sig.sign();
	}

	public boolean verify(String data, String base64Signature)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		return this.verify(data.getBytes(), Base64.decodeBase64(base64Signature));
	}

	public boolean verify(String data, byte[] signature)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		return this.verify(data.getBytes(), signature);
	}

	public boolean verify(byte[] data, String base64Signature)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		return this.verify(data, Base64.decodeBase64(base64Signature));
	}

	public boolean verify(byte[] data, byte[] signature)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		Signature sig = Signature.getInstance(ALGORITHM);
		sig.initVerify(keyPairHelper.getPublicKey());
		sig.update(data);
		return sig.verify(signature);
	}

	public KeyPairHelper getKeyPairHelper() {
		return keyPairHelper;
	}

	public void setKeyPairHelper(KeyPairHelper keyPairHelper) {
		this.keyPairHelper = keyPairHelper;
	}
}
