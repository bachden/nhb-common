package nhb.test.encrypt;

import java.security.GeneralSecurityException;

import com.nhb.common.encrypt.rsa.KeyPairHelper;
import com.nhb.common.encrypt.rsa.SignatureHelper;

public class TestRSA {

	public static void main(String[] args) throws GeneralSecurityException {
		String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC5Agasc6hnv0eR"
				+ "BfOsI3IqmmSng7C6+Lee9ntjRoBu+MQaxBFtE6I4/f5WOuGmpp3+l18+Yvubr8SN"
				+ "MRAZteOsvcA+uhJqxISJ5tgbBAARAd4UIV6w+9D4BmL0CJuTvwqP+b6NHPG9tu3f"
				+ "j5nREw4tAMfHcVxKKgXo3rs4GvYmickEFZ4JnpRurSP5RJYgGdt16miD3cBiC6XC"
				+ "VGSoEff5ZmGGsnqQ/uTjO8Xf7HBFTvIkWqNNFcM/OXPH969EXcNVbOq7WPLE6Wec"
				+ "N3gLBfQiA5uqmCNI8jtGjYwm1xtMCOWUraulthuSbQv27YGKLv5F0rtDGc/tqvxv"
				+ "PXQop7XXAgMBAAECggEAN0A3pgDUZkdlpBXW862SRs7dCHn3qZ7ll3+c0cUYTgvU"
				+ "5PlxCtY2ODcCYdYr1Y0k2gdy1jrRUWlolZH1cPg1Cj2Q0CrZnTEbM+PQ3iCPtISO"
				+ "QgzFLq3FdozRuHdpnvIKwXygP5xJRZL/7yf/k8TREveMsi9UlIGASk9aMiM7J760"
				+ "TtQ3+EX/Af0b7ZvbSMyegr5+rtY0uwof7G0HPipYuFfadrAZ1E9ZuzmkmOC1YcBj"
				+ "iuw10CmGIf3OM7iL3aoqjjNOgIHZysW78WYFQjE8KWjtAso9T34SaQmoiGpn5uiL"
				+ "Qai4ofOGMQjH5llW9vq5VBFmB/qodxrZ3KR7UK5DYQKBgQDg6pHKo8u2CK6uYXzl"
				+ "PmCKOdQLcnPZXQ1CftN/gf1dws8UocWYp+MjR+QP0aegR4dLv1sf6Ycz64Bo0CBh"
				+ "TaND1drezqIXXInfaquPTqfh3NCXctLMwpWORBxYDDEM8QTCmMz3FVoKbVVmDBmz"
				+ "Pi3flEAK0myn3jOKU2SjOduS3QKBgQDSk4R4JVcXAiqk9XzB7epm9Khr22h+1bYz"
				+ "QNKfas7FL0+jWGneCJbj16zjzW180r/+j+dHlSog4vmM6P+l/SMiarkRXkutJiSI"
				+ "Qdg1bwGk4z4g/1PoXo4KaWgG7NSdzH3bLCtIfSgBztSqsJRTL0JA+pNXh/MaHs4g"
				+ "cccHLE3+QwKBgCJLgil86k36avBKCypFZaEOCkgojoOhTalK/2Lc5k5KqxrBRAjv"
				+ "6JFzn9HNrRqibX3eUr299RC0oyLYQWe01+U7EaGRrmOTh65abIhwfObSrbe6GWyB"
				+ "ax6DQjMmwL/xbEFj3TT7u3wFidUCmVSccZA64b0Esu6x0fgQmTLOWq29AoGBAKkp"
				+ "qLl8BcKOEwQB0f9YyX17/fDnicKanIv//zPh8cmmgCuQ0ztzWwQeNt2qIdKs21/8"
				+ "6qcuxXE15ZO1eeo2uQrlAVdakSDAC4kKnE4Qpxl3FGods6Jwk1r54n/tscAyjsiL"
				+ "axoIsSMCrQWSZRWOWYMpyUQACuNo695UiLbSeLVRAoGBAKhctL1z52+hC9WNWtsh"
				+ "cl8H9BdrFu81cE+bsdPvKskSdf40t1Q5e0sUn/MEYcDwWC8zArRT8LzNdE5VxkGk"
				+ "R1908iX5HZ7P4vqi8C+lDqbJ6JPqP0J2xJEzzyox87eRu+C78WYYF2cxSp0pXAM5" + "+h7l6uYvyTc7jAZRdLb9ZFcO";

		String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuQIGrHOoZ79HkQXzrCNy"
				+ "Kppkp4Owuvi3nvZ7Y0aAbvjEGsQRbROiOP3+Vjrhpqad/pdfPmL7m6/EjTEQGbXj"
				+ "rL3AProSasSEiebYGwQAEQHeFCFesPvQ+AZi9Aibk78Kj/m+jRzxvbbt34+Z0RMO"
				+ "LQDHx3FcSioF6N67OBr2JonJBBWeCZ6Ubq0j+USWIBnbdepog93AYgulwlRkqBH3"
				+ "+WZhhrJ6kP7k4zvF3+xwRU7yJFqjTRXDPzlzx/evRF3DVWzqu1jyxOlnnDd4CwX0"
				+ "IgObqpgjSPI7Ro2MJtcbTAjllK2rpbYbkm0L9u2Bii7+RdK7QxnP7ar8bz10KKe1" + "1wIDAQAB";

		KeyPairHelper keyPairHelper = new KeyPairHelper();
		keyPairHelper.loadPrivateKey(privateKey);

		SignatureHelper signatureHelper = new SignatureHelper();
		signatureHelper.setKeyPairHelper(keyPairHelper);

		String msg = "Hello World!!!";

		byte[] signature = signatureHelper.sign(msg);

		keyPairHelper.loadPublicKey(publicKey);
		System.out.println("Valid signature: " + signatureHelper.verify(msg.getBytes(), signature));
	}
}
