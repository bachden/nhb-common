package nhb.common.messaging.test;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nhb.common.data.PuElement;
import nhb.common.data.PuObject;
import nhb.common.utils.Initializer;
import nhb.common.utils.StringUtils;
import nhb.messaging.http.HttpAsyncFuture;
import nhb.messaging.http.HttpClientHelper;
import nhb.messaging.http.HttpMethod;
import nhb.messaging.http.producer.HttpAsyncMessageProducer;

public class TestPayDirectAPI {

	static {
		Initializer.bootstrap(TestPayDirectAPI.class);
	}

	private static Logger logger = LoggerFactory.getLogger(TestPayDirectAPI.class);

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {

		String accountId = "nhantien_test_user";
		String issuer = null;
		String cardCode = null;
		String cardSerial = null;
		String transRef = generateTransferId();
		String partnerCode = "test";
		String password = "123456";
		String secretKey = "test_sk";

		Map<String, String> arguments = parseArguments(args);

		if (!arguments.containsKey("cardCode") || !arguments.containsKey("cardSerial")
				|| !arguments.containsKey("issuer")) {
			printHelp();
			System.exit(0);
		} else {
			issuer = arguments.get("issuer");
			cardCode = arguments.get("cardCode");
			cardSerial = arguments.get("cardSerial");

			if (arguments.containsKey("partnerCode")) {
				partnerCode = arguments.get("partnerCode");
				password = arguments.get("password");
				secretKey = arguments.get("secretKey");
			}
		}

		PuObject params = new PuObject();
		params.setString("issuer", issuer);
		params.setString("cardSerial", cardSerial);
		params.setString("cardCode", cardCode);
		params.setString("amount", "0");
		params.setString("transRef", transRef);
		params.setString("partnerCode", partnerCode);
		params.setString("password", password);
		params.setString("accountId", accountId);
		params.setString("signature", generateSignature(issuer, cardCode, transRef, partnerCode, password, secretKey));
		// params.setString("serviceCode", "VT");

		HttpAsyncMessageProducer httpMessageProducer = new HttpAsyncMessageProducer();
		httpMessageProducer.setUsingMultipath(false);
		httpMessageProducer.setMethod(HttpMethod.POST);
		httpMessageProducer.setEndpoint("http://125.212.219.11/voucher/rest/useCard");

		HttpAsyncFuture future = httpMessageProducer.publish(params);
		PuElement response = HttpClientHelper.handleResponse(future.get());
		httpMessageProducer.close();

		logger.info("\n------------ RESPONSE ------------\n{}\n----------------------------------",
				response.toString());
	}

	private static void printHelp() {
		System.out.println(
				"USSAGE: pass the arguments in following order: cardSerial cardCode issuer [partnerCode password secretKey]");
		System.out.println("The start 3 arguments is required");
		System.out.println("If the last 3 arguments is not passed, default using the test account");
	}

	private static Map<String, String> parseArguments(String[] args) {
		Map<String, String> result = new HashMap<>();
		if (args.length > 0) {
			result.put("cardSerial", args[0]);
			if (args.length > 1) {
				result.put("cardCode", args[1]);
				if (args.length > 2) {
					result.put("issuer", args[2]);
					if (args.length > 5) {
						result.put("partnerCode", args[3]);
						result.put("password", args[4]);
						result.put("secretKey", args[5]);
					}
				}
			}
		}
		return result;
	}

	private static String generateSignature(String issuer, String cardCode, String transRef, String partnerCode,
			String password, String secretKey) {
		try {
			MessageDigest md5Encryptor = MessageDigest.getInstance("MD5");
			md5Encryptor.update((issuer + cardCode + transRef + partnerCode + password + secretKey).getBytes());
			byte[] hash = md5Encryptor.digest();
			StringBuilder hexString = new StringBuilder();
			for (int i = 0; i < hash.length; i++) {
				if ((0xff & hash[i]) < 0x10) {
					hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
				} else {
					hexString.append(Integer.toHexString(0xFF & hash[i]));
				}
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static final String generateTransferId() {
		return StringUtils.randomString(24);
	}
}
