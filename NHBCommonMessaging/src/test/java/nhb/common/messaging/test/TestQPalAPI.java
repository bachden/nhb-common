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

public class TestQPalAPI {

	static {
		Initializer.bootstrap(TestQPalAPI.class);
	}

	private static Logger logger = LoggerFactory.getLogger(TestQPalAPI.class);

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {

		String type = null;
		String pin = null;
		String serial = null;
		String transId = generateTransferId();
		String accessKey = "24c9c027ca21283f70ca8d7612627038";
		String secretKey = "ecbdc747824ee0c7362ee8d26def990e";

		Map<String, String> arguments = parseArguments(args);

		if (!arguments.containsKey("pin") || !arguments.containsKey("serial") || !arguments.containsKey("type")) {
			printHelp();
			System.exit(0);
		} else {
			type = arguments.get("type");
			pin = arguments.get("pin");
			serial = arguments.get("serial");

			if (arguments.containsKey("accessKey")) {
				accessKey = arguments.get("accessKey");
				secretKey = arguments.get("secretKey");
			}
		}

		PuObject params = new PuObject();
		params.setString("pin", pin);
		params.setString("type", type);
		params.setString("serial", serial);
		params.setString("transid", transId);
		params.setString("accesskey", accessKey);
		params.setString("signature", generateSignature(accessKey, pin, serial, transId, type, secretKey));

		HttpAsyncMessageProducer httpMessageProducer = new HttpAsyncMessageProducer();
		
		httpMessageProducer.setUsingMultipath(false);
		httpMessageProducer.setMethod(HttpMethod.POST);
		httpMessageProducer.setEndpoint("http://card.qpal.net/api/card/v2/topup");

		HttpAsyncFuture future = httpMessageProducer.publish(params);
		PuElement response = HttpClientHelper.handleResponse(future.get());
		httpMessageProducer.close();

		logger.info("\n------------ RESPONSE ------------\n{}\n----------------------------------",
				response.toString());
	}

	private static void printHelp() {
		System.out.println("USSAGE: pass the arguments in following order: serial pin type [accessKey secretKey]");
		System.out.println("The start 3 arguments is required");
		System.out.println("If the last 2 arguments is not passed, default using the test account");
	}

	private static Map<String, String> parseArguments(String[] args) {
		Map<String, String> result = new HashMap<>();
		if (args.length > 0) {
			result.put("serial", args[0]);
			if (args.length > 1) {
				result.put("pin", args[1]);
				if (args.length > 2) {
					result.put("type", args[2]);
					if (args.length > 4) {
						result.put("accessKey", args[4]);
						result.put("secretKey", args[5]);
					}
				}
			}
		}
		return result;
	}

	private static String generateSignature(String accesskey, String pin, String serial, String transid, String type,
			String secret) {
		try {
			MessageDigest md5Encryptor = MessageDigest.getInstance("MD5");
			md5Encryptor.update((accesskey + pin + serial + transid + type + secret).getBytes());
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
