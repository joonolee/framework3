package framework.util;

import java.io.File;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * HTTP 클라이언트의 기능을 이용할 수 있는 유틸리티 클래스이다.
 */
public class HttpUtil {

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private HttpUtil() {
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
	 * url 을 Get 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @return Result 객체
	 */
	public static Result get(String url) {
		return get(url, null);
	}

	/**
	 * url 을 Get 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param headerMap 헤더 맵 객체
	 * @return Result 객체
	 */
	public static Result get(String url, Map<String, String> headerMap) {
		int statusCode = 0;
		String content = "";
		HttpClient httpClient = null;
		try {
			httpClient = getHttpClient();
			HttpGet httpGet = new HttpGet(url);
			if (headerMap != null) {
				for (Entry<String, String> entry : headerMap.entrySet()) {
					httpGet.addHeader(entry.getKey(), entry.getValue());
				}
			}
			HttpResponse response = httpClient.execute(httpGet);
			statusCode = response.getStatusLine().getStatusCode();
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				content = EntityUtils.toString(resEntity);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		return new Result(statusCode, content);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @return Result 객체
	 */
	public static Result post(String url) {
		return post(url, (String) null, (Map<String, String>) null);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramMap 파라미터 맵 객체
	 * @return Result 객체
	 */
	public static Result post(String url, Map<String, String> paramMap) {
		return post(url, paramMap, (Map<String, String>) null);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramStr 파라미터 문자열
	 * @return Result 객체
	 */
	public static Result post(String url, String paramStr) {
		return post(url, paramStr, (Map<String, String>) null);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramMap 파라미터 맵 객체
	 * @param headerMap 헤더 맵 객체
	 * @return Result 객체
	 */
	public static Result post(String url, Map<String, String> paramMap, Map<String, String> headerMap) {
		int statusCode = 0;
		String content = "";
		HttpClient httpClient = null;
		try {
			httpClient = getHttpClient();
			HttpPost httpPost = new HttpPost(url);
			if (headerMap != null) {
				for (Entry<String, String> entry : headerMap.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			if (paramMap != null) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				for (Entry<String, String> entry : paramMap.entrySet()) {
					params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, "UTF-8");
				httpPost.setEntity(ent);
			}
			HttpResponse response = httpClient.execute(httpPost);
			statusCode = response.getStatusLine().getStatusCode();
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				content = EntityUtils.toString(resEntity);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		return new Result(statusCode, content);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramStr 파라미터 문자열
	 * @param headerMap 헤더 맵 객체
	 * @return Result 객체
	 */
	public static Result post(String url, String paramStr, Map<String, String> headerMap) {
		int statusCode = 0;
		String content = "";
		HttpClient httpClient = null;
		try {
			httpClient = getHttpClient();
			HttpPost httpPost = new HttpPost(url);
			if (headerMap != null) {
				for (Entry<String, String> entry : headerMap.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			if (paramStr != null) {
				StringEntity ent = new StringEntity(paramStr, "UTF-8");
				httpPost.setEntity(ent);
			}
			HttpResponse response = httpClient.execute(httpPost);
			statusCode = response.getStatusLine().getStatusCode();
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				content = EntityUtils.toString(resEntity);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		return new Result(statusCode, content);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다. (첨부파일 포함)
	 * @param url url 주소
	 * @param paramMap 파라미터 맵 객체
	 * @param fileList 파일 리스트 객체
	 * @return Result 객체
	 */
	public static Result post(String url, Map<String, String> paramMap, List<File> fileList) {
		return post(url, paramMap, fileList, null);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다. (첨부파일 포함)
	 * @param url url 주소
	 * @param paramMap 파라미터 맵 객체
	 * @param fileList 파일 리스트 객체
	 * @param headerMap 헤더 맵 객체
	 * @return Result 객체
	 */
	public static Result post(String url, Map<String, String> paramMap, List<File> fileList, Map<String, String> headerMap) {
		int statusCode = 0;
		String content = "";
		HttpClient httpClient = null;
		try {
			httpClient = getHttpClient();
			HttpPost httpPost = new HttpPost(url);
			if (headerMap != null) {
				for (Entry<String, String> entry : headerMap.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
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
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		return new Result(statusCode, content);
	}

	//////////////////////////////////////////////////////////////////////////////////////////Private 메소드

	/**
	 * HttpClient 생성(SSL 인증서 유효성검사 무시)
	 * @return HttpClient 객체
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 */
	private static HttpClient getHttpClient() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
		HttpClient httpClient = new DefaultHttpClient();
		SSLSocketFactory sslsf = new SSLSocketFactory(new TrustStrategy() {
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		});
		httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, sslsf));
		return httpClient;
	}
}