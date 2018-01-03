<?php

if (!function_exists("mcrypt_get_iv_size") || !function_exists("mcrypt_create_iv")) {
	die("[ERROR] Required method(s) in mcrypt extension were undefined");
}

final class AESSimpleEncryptor {

	const KEY_BYTE_SIZE = 16;
	const CIPHER_METHOD = "rijndael-128";
	const ENCRYPT_MODE = "cbc";

	private static function ourSubstr($str, $start, $length = null) {
		static $exists = null;
		if ($exists === null) {
			$exists = \function_exists('mb_substr');
		}
		if ($exists) {
			// mb_substr($str, 0, NULL, '8bit') returns an empty string on PHP
			// 5.3, so we have to find the length ourselves.
			if (!isset($length)) {
				if ($start >= 0) {
					$length = self::ourStrlen($str) - $start;
				} else {
					$length = -$start;
				}
			}

			return \mb_substr($str, $start, $length, '8bit');
		}

		// Unlike mb_substr(), substr() doesn't accept NULL for length
		if (isset($length)) {
			return \substr($str, $start, $length);
		} else {
			return \substr($str, $start);
		}
	}

	private static function randomIV() {
		// for good entropy (for MCRYPT_RAND)
		srand((double) microtime() * 1000000);
		// generate random iv
		$ivSize = mcrypt_get_iv_size(self::CIPHER_METHOD, self::ENCRYPT_MODE);
		print_r("ok\n");
		$result = mcrypt_create_iv($ivSize, MCRYPT_RAND);
		return $result;
	}

	private static function normalizeSecretKey($secretKey) {
		return hash("SHA256", $secretKey, true);
	}

	static function encrypt($value, $secretKey) {
		$iv = self::randomIV();
		return $iv . rtrim(mcrypt_encrypt(self::CIPHER_METHOD, self::normalizeSecretKey($secretKey), $value, self::ENCRYPT_MODE, $iv), "\0");
	}

	static function encryptToBase64($value, $secretKey) {
		$raw = self::encrypt($value, $secretKey);
		return base64_encode($raw);
	}

	static function decrypt($data, $secretKey) {
		$ivSize = mcrypt_get_iv_size(self::CIPHER_METHOD, self::ENCRYPT_MODE);
		$iv = self::ourSubstr($data, 0, $ivSize);
		$data = self::ourSubstr($data, $ivSize);
		return rtrim(mcrypt_decrypt(self::CIPHER_METHOD, self::normalizeSecretKey($secretKey), $data, self::ENCRYPT_MODE, $iv), "\0");
	}

	static function decryptFromBase64($base64Value, $secretKey) {
		$data = base64_decode($base64Value);
		return self::decrypt($data, $secretKey);
	}

}

$message = "Hello World!!!";
$secretKey = "this is a secret key";

echo("checkpoint 1 \n");

$encrypted = AESSimpleEncryptor::encryptToBase64($message, $secretKey);
echo("checkpoint 2 \n");
echo("Encrypted: " . $encrypted . "\n");
$decrypted = AESSimpleEncryptor::decryptFromBase64($encrypted, $secretKey);
echo("checkpoint 3 \n");
echo("Decrypted: " . $decrypted . "\n");
?>