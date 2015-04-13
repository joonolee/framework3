package framework.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * OAuth 1.0a 인증을 사용하기 위한 유틸리티 클래스이다.
 */
public class OAuth10aUtil {

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private OAuth10aUtil() {
	}

	/**
	 * Result 객체
	 */
	public static class Result {
		private int statusCode;
		private String content;

		public Result() {
			super();
		}

		public Result(int statusCode, String content) {
			super();
			this.statusCode = statusCode;
			this.content = content;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public String getContent() {
			return content;
		}

		@Override
		public String toString() {
			return String.format("Result={ statusCode : %d, content : %s }", getStatusCode(), getContent());
		}
	}

	/**
	 * Consumer 객체
	 */
	public static class Consumer extends CommonsHttpOAuthConsumer {
		private static final long serialVersionUID = 3312085951191371927L;

		public Consumer(String consumerKey, String consumerSecret) {
			super(consumerKey, consumerSecret);
		}

		public Consumer(String consumerKey, String consumerSecret, String token, String tokenSecret) {
			this(consumerKey, consumerSecret);
			this.setTokenWithSecret(token, tokenSecret);
		}
	}

	/**
	 * Provider 객체
	 */
	public static class Provider extends CommonsHttpOAuthProvider {
		private static final long serialVersionUID = -4670920617701598709L;

		public Provider(String requestTokenEndpointUrl, String accessTokenEndpointUrl, String authorizationWebsiteUrl) {
			super(requestTokenEndpointUrl, accessTokenEndpointUrl, authorizationWebsiteUrl);
			this.setOAuth10a(true);
		}
	}

	/**
	 * RequestToken 요청 단계에 필요한 Consumer를 생성한다.
	 * @param consumerKey 컨슈머키
	 * @param consumerSecret 컨슈머시크릿
	 * @return Consumer 객체
	 */
	public static Consumer makeConsumer(String consumerKey, String consumerSecret) {
		return new Consumer(consumerKey, consumerSecret);
	}

	/**
	 * Protected Resource 요청 단계에 필요한 Consumer를 생성한다.
	 * @param consumerKey 컨슈머키
	 * @param consumerSecret 컨슈머시크릿
	 * @param token 액세스토큰
	 * @param tokenSecret 액세스토큰시크릿
	 * @return Consumer 객체
	 */
	public static Consumer makeConsumer(String consumerKey, String consumerSecret, String token, String tokenSecret) {
		return new Consumer(consumerKey, consumerSecret, token, tokenSecret);
	}

	/**
	 * 입력한 값으로 Provider을 생성한다.
	 * @param requestTokenEndpointUrl
	 * @param accessTokenEndpointUrl
	 * @param authorizationWebsiteUrl
	 * @return Provider 객체
	 */
	public static Provider makeProvider(String requestTokenEndpointUrl, String accessTokenEndpointUrl, String authorizationWebsiteUrl) {
		return new Provider(requestTokenEndpointUrl, accessTokenEndpointUrl, authorizationWebsiteUrl);
	}

	/**
	 * Provider에 RequestToken을 요청하여, RequestToken과 RequestTokenSecret을 받아온다. 
	 * @param consumer 컨슈머 객체
	 * @param provider 프로바이더 객체
	 * @param callbackUrl 콜백주소
	 * @return authorize URL
	 */
	public static String getRequestToken(Consumer consumer, Provider provider, String callbackUrl) {
		try {
			String authorizeUrl = provider.retrieveRequestToken(consumer, callbackUrl);
			return authorizeUrl;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Provider에 AccessToken을 요청하여, AccessToken과 AccessTokenSecret을 받아온다.
	 * @param consumer 컨슈머 객체
	 * @param provider 프로바이더 객체
	 * @param verifier 검증값 또는 핀코드
	 */
	public static void getAccessToken(Consumer consumer, Provider provider, String verifier) {
		try {
			provider.retrieveAccessToken(consumer, verifier);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Protected Resource 에 GET 방식으로 요청한다.
	 * @param consumer 컨슈머 객체
	 * @param url API URL
	 * @return Result 객체
	 */
	public static Result get(Consumer consumer, String url) {
		return get(consumer, url, null);
	}

	/**
	 * Protected Resource 에 GET 방식으로 요청한다.
	 * @param consumer 컨슈머 객체
	 * @param url API URL
	 * @param headerMap 헤더
	 * @return Result 객체
	 */
	public static Result get(Consumer consumer, String url, Map<String, String> headerMap) {
		int statusCode = 0;
		String content = "";
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			if (headerMap != null) {
				for (Entry<String, String> entry : headerMap.entrySet()) {
					httpGet.addHeader(entry.getKey(), entry.getValue());
				}
			}
			consumer.sign(httpGet);
			HttpResponse responseGet = httpClient.execute(httpGet);
			statusCode = responseGet.getStatusLine().getStatusCode();
			HttpEntity resEntityGet = responseGet.getEntity();
			if (resEntityGet != null) {
				content = EntityUtils.toString(resEntityGet);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return new Result(statusCode, content);
	}

	/**
	 * Protected Resource 에 POST 방식으로 요청한다.
	 * @param consumer 컨슈머 객체
	 * @param url API URL
	 * @return Result 객체
	 */
	public static Result post(Consumer consumer, String url) {
		return post(consumer, url, null, (Map<String, String>) null);
	}

	/**
	 * Protected Resource 에 POST 방식으로 요청한다.
	 * @param consumer 컨슈머 객체
	 * @param url API URL
	 * @param paramMap 파라미터
	 * @return Result 객체
	 */
	public static Result post(Consumer consumer, String url, Map<String, String> paramMap) {
		return post(consumer, url, paramMap, (Map<String, String>) null);
	}

	/**
	 * Protected Resource 에 POST 방식으로 요청한다.
	 * @param consumer 컨슈머 객체
	 * @param url API URL
	 * @param paramMap 파라미터
	 * @param headerMap 헤더
	 * @return Result 객체
	 */
	public static Result post(Consumer consumer, String url, Map<String, String> paramMap, Map<String, String> headerMap) {
		int statusCode = 0;
		String content = "";
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			if (headerMap != null) {
				for (Entry<String, String> entry : headerMap.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			if (paramMap != null) {
				for (Entry<String, String> entry : paramMap.entrySet()) {
					params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
			}
			UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, "UTF-8");
			httpPost.setEntity(ent);
			consumer.sign(httpPost);
			HttpResponse responsePOST = httpClient.execute(httpPost);
			statusCode = responsePOST.getStatusLine().getStatusCode();
			HttpEntity resEntity = responsePOST.getEntity();
			if (resEntity != null) {
				content = EntityUtils.toString(resEntity);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return new Result(statusCode, content);
	}

	/**
	 * Protected Resource 에 POST 방식으로 요청한다.
	 * @param consumer 컨슈머 객체
	 * @param url API URL
	 * @param paramMap 파라미터
	 * @param fileList 파일
	 * @return Result 객체
	 */
	public static Result post(Consumer consumer, String url, Map<String, String> paramMap, List<File> fileList) {
		return post(consumer, url, paramMap, fileList, null);
	}

	/**
	 * Protected Resource 에 POST 방식으로 요청한다.
	 * @param consumer 컨슈머 객체
	 * @param url API URL
	 * @param paramMap 파라미터
	 * @param fileList 파일
	 * @param headerMap 헤더
	 * @return Result 객체
	 */
	public static Result post(Consumer consumer, String url, Map<String, String> paramMap, List<File> fileList, Map<String, String> headerMap) {
		int statusCode = 0;
		String content = "";
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			if (headerMap != null) {
				for (Entry<String, String> entry : headerMap.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			consumer.sign(httpPost);
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			if (paramMap != null) {
				for (Entry<String, String> entry : paramMap.entrySet()) {
					reqEntity.addPart(entry.getKey(), new StringBody(entry.getValue()));
				}
			}
			if (fileList != null) {
				for (File file : fileList) {
					ContentBody contentBody = new FileBody(file);
					reqEntity.addPart("userfile", contentBody);
				}
			}
			httpPost.setEntity(reqEntity);
			HttpResponse response = httpClient.execute(httpPost);
			statusCode = response.getStatusLine().getStatusCode();
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				content = EntityUtils.toString(resEntity);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return new Result(statusCode, content);
	}
}