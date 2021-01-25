package framework.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;

/**
 * Firebase 기능을 이용할 수 있는 유틸리티 클래스
 */
public class FirebaseUtil {
	private static final Log logger = LogFactory.getLog(FirebaseUtil.class);

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private FirebaseUtil() {
	}

	/**
	 * 인증 토큰에서 사용자 정보 획득
	 */
	public static class FirebaseUser {
		private Map<String, Object> claims;
		private String uid;
		private String issuer;
		private String name;
		private String picture;
		private String email;
		private String signInProvider;

		public FirebaseUser(FirebaseToken token) {
			this.claims = token.getClaims();
			this.uid = token.getUid();
			this.issuer = token.getIssuer();
			this.name = token.getName();
			this.picture = token.getPicture();
			this.email = token.getEmail();
			@SuppressWarnings("unchecked")
			Map<String, Object> firebase = (Map<String, Object>) claims.get("firebase");
			this.signInProvider = (String) firebase.get("sign_in_provider");
		}

		public Map<String, Object> getClaims() {
			return claims;
		}

		public String getUid() {
			return uid;
		}

		public String getIssuer() {
			return issuer;
		}

		public String getName() {
			return name;
		}

		public String getPicture() {
			return picture;
		}

		public String getEmail() {
			return email;
		}

		public String getSignInProvider() {
			return signInProvider;
		}
	}

	/**
	 * Noti 객체
	 */
	public static class Noti {
		private String title;
		private String body;
		private String image;

		public Noti(String title, String body) {
			this.title = title;
			this.body = body;
		}

		public Noti(String title, String body, String image) {
			this.title = title;
			this.body = body;
			this.image = image;
		}

		public String getTitle() {
			return title;
		}

		public String getBody() {
			return body;
		}

		public String getImage() {
			return image;
		}
	}

	/**
	 * 토큰 유효성 검사
	 * @param token 인증토큰
	 * @param name 앱이름
	 * @return Uid 문자열
	 */
	public static FirebaseUser verifyIdToken(String token, String name) {
		try {
			FirebaseToken firebaseToken = FirebaseAuth.getInstance(FirebaseApp.getInstance(name)).verifyIdToken(token);
			return new FirebaseUser(firebaseToken);
		} catch (FirebaseAuthException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 파이어베이스 사용자 정보 삭제
	 * @param uid 파이어베이스 UID
	 */
	public static void deleteUser(String uid, String name) {
		try {
			FirebaseAuth.getInstance(FirebaseApp.getInstance(name)).deleteUser(uid);
		} catch (FirebaseAuthException e) {
			logger.error("", e);
		}
	}

	/**
	 * 특정 기기에 메시지 전송(노티만)
	 * @param token fcm 토큰
	 * @param noti 노티할 데이터
	 * @param name 앱이름
	 * @return 성공여부
	 */
	public static boolean sendMessage(String token, Noti noti, String name) {
		Notification notification = Notification.builder()
			.setTitle(noti.getTitle())
			.setBody(noti.getBody())
			.setImage(noti.getImage())
			.build();
		Message message = Message.builder()
			.setNotification(notification)
			.setToken(token)
			.build();
		try {
			String response = FirebaseMessaging.getInstance(FirebaseApp.getInstance(name)).send(message);
			logger.debug(response);
			return true;
		} catch (FirebaseMessagingException e) {
			logger.error("", e);
		}
		return false;
	}

	/**
	 * 특정 기기에 메시지 전송(데이터만)
	 * @param token fcm 토큰
	 * @param data 전송할 데이터 맵
	 * @param name 앱이름
	 * @return 성공여부
	 */
	public static boolean sendMessage(String token, Map<String, String> data, String name) {
		Message message = Message.builder()
			.putAllData(data)
			.setToken(token)
			.build();
		try {
			String response = FirebaseMessaging.getInstance(FirebaseApp.getInstance(name)).send(message);
			logger.debug(response);
			return true;
		} catch (FirebaseMessagingException e) {
			logger.error("", e);
		}
		return false;
	}

	/**
	 * 특정 기기에 메시지 전송(노티 + 데이터)
	 * @param token fcm 토큰
	 * @param noti 노티할 데이터
	 * @param data 전송할 데이터 맵
	 * @param name 앱이름
	 * @return 성공여부
	 */
	public static boolean sendMessage(String token, Noti noti, Map<String, String> data, String name) {
		Notification notification = Notification.builder()
			.setTitle(noti.getTitle())
			.setBody(noti.getBody())
			.setImage(noti.getImage())
			.build();
		Message message = Message.builder()
			.setNotification(notification)
			.putAllData(data)
			.setToken(token)
			.build();
		try {
			String response = FirebaseMessaging.getInstance(FirebaseApp.getInstance(name)).send(message);
			logger.debug(response);
			return true;
		} catch (FirebaseMessagingException e) {
			logger.error("", e);
		}
		return false;
	}

	/**
	 * 여러 기기에 메시지 전송(노티만)
	 * @param tokenList fcm 토큰리스트
	 * @param noti 노티할 데이터
	 * @param name 앱이름
	 * @return 성공여부
	 */
	public static boolean sendMessage(List<String> tokenList, Noti noti, String name) {
		Notification notification = Notification.builder()
			.setTitle(noti.getTitle())
			.setBody(noti.getBody())
			.setImage(noti.getImage())
			.build();
		MulticastMessage message = MulticastMessage.builder()
			.setNotification(notification)
			.addAllTokens(tokenList)
			.build();
		try {
			BatchResponse response = FirebaseMessaging.getInstance(FirebaseApp.getInstance(name)).sendMulticast(message);
			logger.debug(response.toString());
			return true;
		} catch (FirebaseMessagingException e) {
			logger.error("", e);
		}
		return false;
	}

	/**
	 * 여러 기기에 메시지 전송(데이터만)
	 * @param tokenList fcm 토큰리스트
	 * @param data 전송할 데이터 맵
	 * @param name 앱이름
	 * @return 성공여부
	 */
	public static boolean sendMessage(List<String> tokenList, Map<String, String> data, String name) {
		MulticastMessage message = MulticastMessage.builder()
			.putAllData(data)
			.addAllTokens(tokenList)
			.build();
		try {
			BatchResponse response = FirebaseMessaging.getInstance(FirebaseApp.getInstance(name)).sendMulticast(message);
			logger.debug(response.toString());
			return true;
		} catch (FirebaseMessagingException e) {
			logger.error("", e);
		}
		return false;
	}

	/**
	 * 여러 기기에 메시지 전송(노티 + 데이터)
	 * @param tokenList fcm 토큰리스트
	 * @param noti 노티할 데이터
	 * @param data 전송할 데이터 맵
	 * @param name 앱이름
	 * @return 성공여부
	 */
	public static boolean sendMessage(List<String> tokenList, Noti noti, Map<String, String> data, String name) {
		Notification notification = Notification.builder()
			.setTitle(noti.getTitle())
			.setBody(noti.getBody())
			.setImage(noti.getImage())
			.build();
		MulticastMessage message = MulticastMessage.builder()
			.setNotification(notification)
			.putAllData(data)
			.addAllTokens(tokenList)
			.build();
		try {
			BatchResponse response = FirebaseMessaging.getInstance(FirebaseApp.getInstance(name)).sendMulticast(message);
			logger.debug(response.toString());
			return true;
		} catch (FirebaseMessagingException e) {
			logger.error("", e);
		}
		return false;
	}
}