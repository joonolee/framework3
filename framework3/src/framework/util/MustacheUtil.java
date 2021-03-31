package framework.util;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.samskivert.mustache.Mustache;

/**
 * Mustache를 이용한 템플릿 처리 유틸리티 클래스
 */
public class MustacheUtil {
	private static final Log logger = LogFactory.getLog(MustacheUtil.class);

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private MustacheUtil() {
	}

	/**
	 * 템플릿 파일에 param 객체를 적용해서 문자열을 만든다.
	 * @param templateReader 템플릿 Reader 객체
	 * @param param 파라미터 객체
	 * @return 템플릿이 적용된 문자열
	 */
	public static String render(Reader templateReader, Object param) {
		try {
			return Mustache.compiler().compile(templateReader).execute(param);
		} catch (Exception e) {
			logger.error("", e);
			return "";
		}
	}

	/**
	 * 템플릿 파일에 param 객체를 적용해서 문자열을 만든다.
	 * @param templateFile 템플릿 파일 객체
	 * @param param 파라미터 객체
	 * @return 템플릿이 적용된 문자열
	 */
	public static String render(File templateFile, Object param) {
		try {
			return render(new FileReader(templateFile), param);
		} catch (Exception e) {
			logger.error("", e);
			return "";
		}
	}

	/**
	 * 템플릿 파일에 param 객체를 적용해서 문자열을 만든다.
	 * @param inputStream 템플릿 InputStream 객체
	 * @param param 파라미터 객체
	 * @return 템플릿이 적용된 문자열
	 */
	public static String render(InputStream inputStream, Object param) {
		return render(new InputStreamReader(inputStream), param);
	}
}