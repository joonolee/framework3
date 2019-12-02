package framework.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;

/**
 * JWT 토큰 생성, 유효성 검증, 디코드 할 때 이용할 수 있는 유틸리티 클래스
 */
public class JwtUtil {
	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private JwtUtil() {
	}

	/**
	 * JWT 토근을 생성한다.
	 * @param secret secret 키
	 * @param issuer 발급자
	 * @param claims 정보조각 맵
	 * @param expireMilliseconds 만료시간(밀리초)
	 * @return 생성된 JWT 토큰 문자열
	 */
	public static String createToken(String secret, String issuer, Map<String, String> claims, long expireMilliseconds) {
		Builder tokenBuilder = JWT.create();
		tokenBuilder.withIssuedAt(new Date()); // 발행시간
		tokenBuilder.withExpiresAt(new Date(new Date().getTime() + expireMilliseconds)); // 만료시간
		tokenBuilder.withIssuer(issuer); // 발급자
		if (claims != null) {
			for (Entry<String, String> entry : claims.entrySet()) {
				tokenBuilder.withClaim(entry.getKey(), entry.getValue());
			}
		}
		return tokenBuilder.sign(Algorithm.HMAC256(secret));
	}

	/**
	 * 토큰 유효성 검사
	 * @param secret secret 키
	 * @param issuer 발급자
	 * @param token JWT 토큰 문자열
	 * @return 유효성 여부
	 */
	public static boolean verifyToken(String secret, String issuer, String token) {
		try {
			Verification verification = JWT.require(Algorithm.HMAC256(secret));
			verification.withIssuer(issuer); // 발급자
			JWTVerifier verifier = verification.build();
			verifier.verify(token); // 유효성 검사
			return true;
		} catch (JWTVerificationException e) {
			return false;
		}
	}

	/**
	 * 토큰에서 정보 조각을 얻어온다.
	 * @param token JWT 토큰 문자열
	 * @return 정보조각 맵
	 */
	public static Map<String, String> decodeToken(String token) {
		DecodedJWT decodedJwt = JWT.decode(token);
		Map<String, String> returnMap = new HashMap<String, String>();
		Map<String, Claim> claims = decodedJwt.getClaims();
		if (claims != null) {
			for (Entry<String, Claim> entry : claims.entrySet()) {
				if ("exp".equals(entry.getKey()) || "iat".equals(entry.getKey())) { // exp, iat의 경우 반환하지 않는다.
					continue;
				} else { // 그 외 값은 문자열로 변환해 맵에 담아 반환한다.
					returnMap.put(entry.getKey(), entry.getValue().asString());
				}
			}
		}
		return returnMap;
	}
}