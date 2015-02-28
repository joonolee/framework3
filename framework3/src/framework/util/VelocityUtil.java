package framework.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import framework.action.Params;

/**
 * Velocity를 이용한 템플릿 처리 라이브러리
 */
public class VelocityUtil {
	protected static final Log logger = LogFactory.getLog(framework.util.VelocityUtil.class);

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private VelocityUtil() {
	}

	/**
	 * mapping.properties 파일에 설정된 key와 연결된 템플릿 파일에서 statement에 정의된 COMMAND의 문자열을 파라미터를 
	 * 적용한 문자열을 생성한다. VelocityUtil.evalutate과 동일
	 * <br>
	 * Sql 문장생성 및 이메일 발송을 위한 템플릿 생성할때 응용할 수 있다.
	 * @param servlet 서블릿 객체
	 * @param key routes.properties에 등록한 템플릿의 키 문자열 
	 * @param statement 문장식별 문자열
	 * @param param 파라미터 Param 객체
	 * @return 템플릿이 적용된 문자열
	 */
	public static String render(HttpServlet servlet, String key, String statement, Params param) {
		return _evaluate(servlet, key, statement, param);
	}

	/**
	 * routes.properties 파일에 설정된 key와 연결된 템플릿 파일에서 statement에 정의된 COMMAND의 문자열을 파라미터를 
	 * 적용한 문자열을 생성한다.
	 */
	private static String _evaluate(HttpServlet servlet, String key, String statement, Params param) {
		StringWriter writer = new StringWriter();
		try {
			Velocity.init();
			VelocityContext context = new VelocityContext();
			context.put("COMMAND", statement);
			context.put("PARAM", param);
			context.put("UTIL", StringUtil.class);

			ResourceBundle bundle = (ResourceBundle) servlet.getServletContext().getAttribute("routes-mapping");
			String fileName = ((String) bundle.getObject(key)).trim();

			String template = _readTemplate(servlet, fileName);
			StringReader reader = new StringReader(template);
			Velocity.evaluate(context, writer, "framework.util.VelocityUtil", reader);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return writer.toString();
	}

	/**
	 * 템플릿파일을 읽어들인다.
	 */
	private static String _readTemplate(HttpServlet servlet, String fileName) {
		String pathFile = servlet.getServletContext().getRealPath(fileName);
		return _read(pathFile);
	}

	/** 
	 * 파일의 path를 가지 파일명으로 파일 내용 읽어서 String으로 리턴한다 
	 */
	private static String _read(String pathFile) {
		StringBuilder ta = new StringBuilder();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(pathFile);
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				ta.append(line + "\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("Error!", e);
				}
			}
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					logger.error("Error!", e);
				}
			}
		}
		return ta.toString();
	}
}