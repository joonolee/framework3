package framework.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * 암/복호화 관련 기능을 하는 유틸리티 클래스이다.
 */
public class CryptUtil {

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private CryptUtil() {
	}

	/**
	 * 메시지를 MD5 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashMD5HexString(String message) {
		return _hashHexString(message, "MD5");
	}

	/**
	 * 메시지를 MD5 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashMD5Base64String(String message) {
		return _hashBase64String(message, "MD5");
	}

	/**
	 * salt를 적용하여 메시지를 MD5 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashMD5HexString(String message, String salt) {
		return _hashHexString(message, salt, "MD5");
	}

	/**
	 * salt를 적용하여 메시지를 MD5 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashMD5Base64String(String message, String salt) {
		return _hashBase64String(message, salt, "MD5");
	}

	/**
	 * 메시지를 SHA-1 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashSHA1HexString(String message) {
		return _hashHexString(message, "SHA-1");
	}

	/**
	 * 메시지를 SHA-1 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashSHA1Base64String(String message) {
		return _hashBase64String(message, "SHA-1");
	}

	/**
	 * salt를 적용하여 메시지를 SHA-1 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashSHA1HexString(String message, String salt) {
		return _hashHexString(message, salt, "SHA-1");
	}

	/**
	 * salt를 적용하여 메시지를 SHA-1 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashSHA1Base64String(String message, String salt) {
		return _hashBase64String(message, salt, "SHA-1");
	}

	/**
	 * 메시지를 SHA-256 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashSHA256HexString(String message) {
		return _hashHexString(message, "SHA-256");
	}

	/**
	 * 메시지를 SHA-256 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과  Base64 문자열
	 */
	public static String hashSHA256Base64String(String message) {
		return _hashBase64String(message, "SHA-256");
	}

	/**
	 * salt를 적용하여 메시지를 SHA-256 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashSHA256HexString(String message, String salt) {
		return _hashHexString(message, salt, "SHA-256");
	}

	/**
	 * salt를 적용하여 메시지를 SHA-256 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과  Base64 문자열
	 */
	public static String hashSHA256Base64String(String message, String salt) {
		return _hashBase64String(message, salt, "SHA-256");
	}

	/**
	 * 메시지를 SHA-512 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashSHA512HexString(String message) {
		return _hashHexString(message, "SHA-512");
	}

	/**
	 * 메시지를 SHA-512 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashSHA512Base64String(String message) {
		return _hashBase64String(message, "SHA-512");
	}

	/**
	 * salt를 적용하여 메시지를 SHA-512 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashSHA512HexString(String message, String salt) {
		return _hashHexString(message, salt, "SHA-512");
	}

	/**
	 * salt를 적용하여 메시지를 SHA-512 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashSHA512Base64String(String message, String salt) {
		return _hashBase64String(message, salt, "SHA-512");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacMD5 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashHmacMD5HexString(String message, String secretKey) {
		return _hashHmacHexString(message, secretKey, "HmacMD5");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacMD5 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashHmacMD5Base64String(String message, String secretKey) {
		return _hashHmacBase64String(message, secretKey, "HmacMD5");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacSHA1 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashHmacSHA1HexString(String message, String secretKey) {
		return _hashHmacHexString(message, secretKey, "HmacSHA1");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacSHA1 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashHmacSHA1Base64String(String message, String secretKey) {
		return _hashHmacBase64String(message, secretKey, "HmacSHA1");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacSHA256 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashHmacSHA256HexString(String message, String secretKey) {
		return _hashHmacHexString(message, secretKey, "HmacSHA256");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacSHA256 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashHmacSHA256Base64String(String message, String secretKey) {
		return _hashHmacBase64String(message, secretKey, "HmacSHA256");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacSHA512 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashHmacSHA512HexString(String message, String secretKey) {
		return _hashHmacHexString(message, secretKey, "HmacSHA512");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacSHA512 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashHmacSHA512Base64String(String message, String secretKey) {
		return _hashHmacBase64String(message, secretKey, "HmacSHA512");
	}

	/**
	 * 메시지를 BASE64 알고리즘으로 인코딩한다.
	 * @param message 원본메시지
	 * @return 인코딩된 문자열
	 */
	public static String encodeBase64String(String message) {
		return Base64.encodeBase64String(message.getBytes());
	}

	/**
	 * 메시지를 BASE64 알고리즘으로 디코딩한다.
	 * @param message 원본 메시지
	 * @return 디코딩된 문자열
	 */
	public static String decodeBase64String(String message) {
		return new String(Base64.decodeBase64(message.getBytes()));
	}

	/**
	 * 메시지를 개인키를 이용하여 AES 알고리즘으로 암호화한다.
	 * @param message 원본메시지
	 * @param privateKey 개인키 
	 * @return 암호화된 문자열
	 */
	public static String encryptAES(String message, String privateKey) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(privateKey.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			return new String(Hex.encodeHex(cipher.doFinal(message.getBytes())));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 메시지를 개인키를 이용하여 AES 알고리즘으로 복호화한다.
	 * @param message 원본메시지
	 * @param privateKey 개인키 
	 * @return 복호화된 문자열
	 */
	public static String decryptAES(String message, String privateKey) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(privateKey.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			return new String(cipher.doFinal(Hex.decodeHex(message.toCharArray())));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 메시지를 개인키를 이용하여 DES 알고리즘으로 암호화한다.
	 * @param message 원본메시지
	 * @param privateKey 개인키 
	 * @return 암호화된 문자열
	 */
	public static String encryptDES(String message, String privateKey) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(privateKey.getBytes(), "DES");
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			return Hex.encodeHexString(cipher.doFinal(message.getBytes()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 메시지를 개인키를 이용하여 DES 알고리즘으로 복호화한다.
	 * @param message 원본메시지
	 * @param privateKey 개인키 
	 * @return 복호화된 문자열
	 */
	public static String decryptDES(String message, String privateKey) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(privateKey.getBytes(), "DES");
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			return new String(cipher.doFinal(Hex.decodeHex(message.toCharArray())));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 해시알고리즘에서 사용하기 위한 솔트를 생성한다.
	 * @return 랜덤으로 생성된 20자리 솔트 문자열
	 */
	public static String randomSalt() {
		SecureRandom r = new SecureRandom();
		byte[] salt = new byte[10];
		r.nextBytes(salt);
		return Hex.encodeHexString(salt);
	}

	//////////////////////////////////////////////////////////////////////////////////////////Private 메소드

	/*
	 * 해시결과 바이트 배열
	 */
	private static byte[] _hash(String message, String algorithm) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.reset();
			md.update("".getBytes());
			return md.digest(message.getBytes());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * 해시결과 Hex 문자열
	 */
	private static String _hashHexString(String message, String algorithm) {
		return Hex.encodeHexString(_hash(message, algorithm));
	}

	/*
	 * 해시결과 Base64 문자열
	 */
	private static String _hashBase64String(String message, String algorithm) {
		return Base64.encodeBase64String((_hash(message, algorithm)));
	}

	/*
	 * Hmac 해시결과 바이트 배열
	 */
	private static byte[] _hashHmac(String message, String secretKey, String algorithm) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes(), algorithm);
			Mac mac = Mac.getInstance(algorithm);
			mac.init(skeySpec);
			return mac.doFinal((message.getBytes()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Hmac 해시결과 Hex 문자열
	 */
	private static String _hashHmacHexString(String message, String secretKey, String algorithm) {
		return Hex.encodeHexString(_hashHmac(message, secretKey, algorithm));
	}

	/*
	 * Hmac 해시결과 Base64 문자열
	 */
	private static String _hashHmacBase64String(String message, String secretKey, String algorithm) {
		return Base64.encodeBase64String(_hashHmac(message, secretKey, algorithm));
	}

	/*
	 * salt 적용 해시결과 바이트 배열
	 */
	private static byte[] _hash(String message, String salt, String algorithm) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.reset();
			md.update(salt.getBytes());
			return md.digest(message.getBytes());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * salt 적용 해시결과 Hex 문자열
	 */
	private static String _hashHexString(String message, String salt, String algorithm) {
		return Hex.encodeHexString(_hash(message, salt, algorithm));
	}

	/*
	 * salt 적용 해시결과 Base64 문자열
	 */
	private static String _hashBase64String(String message, String salt, String algorithm) {
		return Base64.encodeBase64String(_hash(message, salt, algorithm));
	}
}