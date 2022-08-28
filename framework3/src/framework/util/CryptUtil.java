package framework.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource.PSpecified;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

/**
 * 암/복호화 관련 기능을 하는 유틸리티 클래스
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
		return hashHexString(message, "MD5");
	}

	/**
	 * 메시지를 MD5 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashMD5Base64String(String message) {
		return hashBase64String(message, "MD5");
	}

	/**
	 * salt를 적용하여 메시지를 MD5 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashMD5HexString(String message, String salt) {
		return hashHexString(message, salt, "MD5");
	}

	/**
	 * salt를 적용하여 메시지를 MD5 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashMD5Base64String(String message, String salt) {
		return hashBase64String(message, salt, "MD5");
	}

	/**
	 * 메시지를 SHA-1 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashSHA1HexString(String message) {
		return hashHexString(message, "SHA-1");
	}

	/**
	 * 메시지를 SHA-1 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashSHA1Base64String(String message) {
		return hashBase64String(message, "SHA-1");
	}

	/**
	 * salt를 적용하여 메시지를 SHA-1 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashSHA1HexString(String message, String salt) {
		return hashHexString(message, salt, "SHA-1");
	}

	/**
	 * salt를 적용하여 메시지를 SHA-1 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashSHA1Base64String(String message, String salt) {
		return hashBase64String(message, salt, "SHA-1");
	}

	/**
	 * 메시지를 SHA-256 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashSHA256HexString(String message) {
		return hashHexString(message, "SHA-256");
	}

	/**
	 * 메시지를 SHA-256 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과  Base64 문자열
	 */
	public static String hashSHA256Base64String(String message) {
		return hashBase64String(message, "SHA-256");
	}

	/**
	 * salt를 적용하여 메시지를 SHA-256 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashSHA256HexString(String message, String salt) {
		return hashHexString(message, salt, "SHA-256");
	}

	/**
	 * salt를 적용하여 메시지를 SHA-256 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과  Base64 문자열
	 */
	public static String hashSHA256Base64String(String message, String salt) {
		return hashBase64String(message, salt, "SHA-256");
	}

	/**
	 * 메시지를 SHA-512 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashSHA512HexString(String message) {
		return hashHexString(message, "SHA-512");
	}

	/**
	 * 메시지를 SHA-512 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashSHA512Base64String(String message) {
		return hashBase64String(message, "SHA-512");
	}

	/**
	 * salt를 적용하여 메시지를 SHA-512 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashSHA512HexString(String message, String salt) {
		return hashHexString(message, salt, "SHA-512");
	}

	/**
	 * salt를 적용하여 메시지를 SHA-512 알고리즘으로 해쉬한다.
	 * @param message 원본메시지
	 * @param salt 솔트값
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashSHA512Base64String(String message, String salt) {
		return hashBase64String(message, salt, "SHA-512");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacMD5 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashHmacMD5HexString(String message, String secretKey) {
		return hashHmacHexString(message, secretKey, "HmacMD5");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacMD5 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashHmacMD5Base64String(String message, String secretKey) {
		return hashHmacBase64String(message, secretKey, "HmacMD5");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacSHA1 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashHmacSHA1HexString(String message, String secretKey) {
		return hashHmacHexString(message, secretKey, "HmacSHA1");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacSHA1 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashHmacSHA1Base64String(String message, String secretKey) {
		return hashHmacBase64String(message, secretKey, "HmacSHA1");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacSHA256 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashHmacSHA256HexString(String message, String secretKey) {
		return hashHmacHexString(message, secretKey, "HmacSHA256");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacSHA256 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashHmacSHA256Base64String(String message, String secretKey) {
		return hashHmacBase64String(message, secretKey, "HmacSHA256");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacSHA512 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Hex 문자열
	 */
	public static String hashHmacSHA512HexString(String message, String secretKey) {
		return hashHmacHexString(message, secretKey, "HmacSHA512");
	}

	/**
	 * 메시지를 secretKey를 이용하여 HmacSHA512 알고리즘으로 해시한다.
	 * @param message 원본메시지
	 * @param secretKey 키
	 * @return 해쉬결과 Base64 문자열
	 */
	public static String hashHmacSHA512Base64String(String message, String secretKey) {
		return hashHmacBase64String(message, secretKey, "HmacSHA512");
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
	 * 메시지를 공개키를 이용하여 RSA-OAEP 알고리즘으로 암호화한다.
	 * @param message 원본메시지
	 * @param publicKeyBase64 Base64 인코딩된 공개키 문자열
	 * @return 암호화된 문자열
	 */
	public static String encryptRSA(String message, String publicKeyBase64) {
		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyBase64.getBytes()));
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = keyFactory.generatePublic(keySpec);
			return encryptRSA(message, publicKey);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 메시지를 공개키를 이용하여 RSA-OAEP 알고리즘으로 암호화한다.
	 * @param message 원본메시지
	 * @param publicKey 공개키
	 * @return 암호화된 문자열
	 */
	public static String encryptRSA(String message, PublicKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return new String(Hex.encodeHex(cipher.doFinal(message.getBytes())));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 메시지를 공개키를 이용하여 RSA-OAEP 알고리즘으로 암호화한다.
	 * @param message 원본메시지
	 * @param publicKeyPemFile 공개키 pem 파일
	 * @return 암호화된 문자열
	 */
	public static String encryptRSA(String message, File publicKeyPemFile) {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, readKeyFromPemFile(publicKeyPemFile));
			return new String(Hex.encodeHex(cipher.doFinal(message.getBytes())));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 메시지를 공개키를 이용하여 RSA-OAEP 알고리즘으로 암호화한다.
	 * @param message 원본메시지
	 * @param publicKeyPemReader 공개키 pem Reader
	 * @return 암호화된 문자열
	 */
	public static String encryptRSA(String message, Reader publicKeyPemReader) {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, readKeyFromPemReader(publicKeyPemReader));
			return new String(Hex.encodeHex(cipher.doFinal(message.getBytes())));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 메시지를 개인키를 이용하여 RSA-OAEP 알고리즘으로 복호화한다.
	 * @param message 원본메시지
	 * @param privateKeyBase64 Base64 인코딩된 개인키 문자열
	 * @return 복호화된 문자열
	 */
	public static String decryptRSA(String message, String privateKeyBase64) {
		try {
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyBase64.getBytes()));
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
			return decryptRSA(message, privateKey);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 메시지를 개인키를 이용하여 RSA-OAEP 알고리즘으로 복호화한다.
	 * @param message 원본메시지
	 * @param privateKey 개인키
	 * @return 복호화된 문자열
	 */
	public static String decryptRSA(String message, PrivateKey privateKey) {
		try {
			OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-1"), PSpecified.DEFAULT);
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
			cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
			return new String(cipher.doFinal(Hex.decodeHex(message.toCharArray())));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 메시지를 개인키를 이용하여 RSA-OAEP 알고리즘으로 복호화한다.
	 * @param message 원본메시지
	 * @param privateKeyPemFile 개인키 pem 파일
	 * @return 복호화된 문자열
	 */
	public static String decryptRSA(String message, File privateKeyPemFile) {
		try {
			OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-1"), PSpecified.DEFAULT);
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
			cipher.init(Cipher.DECRYPT_MODE, readKeyFromPemFile(privateKeyPemFile), oaepParams);
			return new String(cipher.doFinal(Hex.decodeHex(message.toCharArray())));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 메시지를 개인키를 이용하여 RSA-OAEP 알고리즘으로 복호화한다.
	 * @param message 원본메시지
	 * @param privateKeyPemReader 개인키 pem Reader
	 * @return 복호화된 문자열
	 */
	public static String decryptRSA(String message, Reader privateKeyPemReader) {
		try {
			OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-1"), PSpecified.DEFAULT);
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
			cipher.init(Cipher.DECRYPT_MODE, readKeyFromPemReader(privateKeyPemReader), oaepParams);
			return new String(cipher.doFinal(Hex.decodeHex(message.toCharArray())));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * RSA 공개키, 개인키를 생성하여 Base64로 인코딩하여 반환한다. 공개키와 개인키는 | 문자로 구분된다.
	 * @return 공개키(base64인코딩)|개인키(base64인코딩) 형식의 문자열
	 */
	public static String genRSAKeyPairBase64String() {
		KeyPair keyPair = genRSAKeyPair();
		return genRSAKeyPairBase64String(keyPair);
	}

	/**
	 * RSA 공개키, 개인키를 생성하여 pem 파일로 저장한 후 Base64로 인코딩하여 반환한다. 공개키와 개인키는 | 문자로 구분된다.
	 * @param publicKeyFile 공개키 pem 파일
	 * @param privateKeyFile 개인키 pem 파일
	 * @return 공개키(base64인코딩)|개인키(base64인코딩) 형식의 문자열
	 */
	public static String genRSAKeyPairBase64String(File publicKeyFile, File privateKeyFile) {
		KeyPair keyPair = genRSAKeyPair();
		writeKeyToPemFile(keyPair.getPublic(), "PUBLIC KEY", publicKeyFile);
		writeKeyToPemFile(keyPair.getPrivate(), "PRIVATE KEY", privateKeyFile);
		return genRSAKeyPairBase64String(keyPair);
	}

	/**
	 * RSA 공개키, 개인키를 생성하여 pem Writer로 저장한 후 Base64로 인코딩하여 반환한다. 공개키와 개인키는 | 문자로 구분된다.
	 * @param publicKeyWriter 공개키 pem Writer
	 * @param privateKeyWriter 개인키 pem Writer
	 * @return 공개키(base64인코딩)|개인키(base64인코딩) 형식의 문자열
	 */
	public static String genRSAKeyPairBase64String(Writer publicKeyWriter, Writer privateKeyWriter) {
		KeyPair keyPair = genRSAKeyPair();
		writeKeyToPemWriter(keyPair.getPublic(), "PUBLIC KEY", publicKeyWriter);
		writeKeyToPemWriter(keyPair.getPrivate(), "PRIVATE KEY", privateKeyWriter);
		return genRSAKeyPairBase64String(keyPair);
	}

	/**
	 * RSA 공개키, 개인키를 Base64로 인코딩하여 반환한다. 공개키와 개인키는 | 문자로 구분된다.
	 * @param keyPair RSA 키쌍
	 * @return 공개키(base64인코딩)|개인키(base64인코딩) 형식의 문자열
	 */
	public static String genRSAKeyPairBase64String(KeyPair keyPair) {
		return new String(Base64.encodeBase64String(keyPair.getPublic().getEncoded())) +
			"|" + new String(Base64.encodeBase64String(keyPair.getPrivate().getEncoded()));
	}

	/**
	 * 2048비트 RSA 키쌍을 생성한다.
	 * @return 2048비트 RSA 키쌍
	 */
	public static KeyPair genRSAKeyPair() {
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(2048, new SecureRandom());
			return gen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
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
	private static byte[] hash(String message, String algorithm) {
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
	private static String hashHexString(String message, String algorithm) {
		return Hex.encodeHexString(hash(message, algorithm));
	}

	/*
	 * 해시결과 Base64 문자열
	 */
	private static String hashBase64String(String message, String algorithm) {
		return Base64.encodeBase64String((hash(message, algorithm)));
	}

	/*
	 * Hmac 해시결과 바이트 배열
	 */
	private static byte[] hashHmac(String message, String secretKey, String algorithm) {
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
	private static String hashHmacHexString(String message, String secretKey, String algorithm) {
		return Hex.encodeHexString(hashHmac(message, secretKey, algorithm));
	}

	/*
	 * Hmac 해시결과 Base64 문자열
	 */
	private static String hashHmacBase64String(String message, String secretKey, String algorithm) {
		return Base64.encodeBase64String(hashHmac(message, secretKey, algorithm));
	}

	/*
	 * salt 적용 해시결과 바이트 배열
	 */
	private static byte[] hash(String message, String salt, String algorithm) {
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
	private static String hashHexString(String message, String salt, String algorithm) {
		return Hex.encodeHexString(hash(message, salt, algorithm));
	}

	/*
	 * salt 적용 해시결과 Base64 문자열
	 */
	private static String hashBase64String(String message, String salt, String algorithm) {
		return Base64.encodeBase64String(hash(message, salt, algorithm));
	}

	/*
	 * 키를 pem 파일로 쓰기
	 */
	private static void writeKeyToPemFile(Key key, String description, File pemFile) {
		try {
			writeKeyToPemWriter(key, description, new FileWriter(pemFile));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * 키를 pem Writer로 쓰기
	 */
	private static void writeKeyToPemWriter(Key key, String description, Writer writer) {
		PemWriter pemWriter = null;
		try {
			PemObject pemObject = new PemObject(description, key.getEncoded());
			pemWriter = new PemWriter(writer);
			pemWriter.writeObject(pemObject);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (pemWriter != null) {
				try {
					pemWriter.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/*
	 * pem 파일에서 키 읽기
	 */
	private static Key readKeyFromPemFile(File pemFile) {
		try {
			return readKeyFromPemReader(new FileReader(pemFile));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * pem Reader에서 키 읽기
	 */
	private static Key readKeyFromPemReader(Reader reader) {
		PemReader pemReader = null;
		try {
			pemReader = new PemReader(reader);
			PemObject pemObject = pemReader.readPemObject();
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			if ("PUBLIC KEY".equals(pemObject.getType())) {
				X509EncodedKeySpec spec = new X509EncodedKeySpec(pemObject.getContent());
				return keyFactory.generatePublic(spec);
			} else if ("PRIVATE KEY".equals(pemObject.getType())) {
				final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pemObject.getContent());
				return keyFactory.generatePrivate(keySpec);
			}
			return null;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (pemReader != null) {
				try {
					pemReader.close();
				} catch (IOException e) {
				}
			}
		}
	}
}