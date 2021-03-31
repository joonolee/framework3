package framework.util;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * Jasper Report 를 이용하여 개발할 때 이용할 수 있는 유틸리티 클래스
 */
public class JasperUtil {
	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private JasperUtil() {
	}

	/**
	 * Map을 Jasper 파일 형식으로 출력한다.
	 * @param sc 서블릿 컨텍스트 객체
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param jasperFilePath Jasper Report 대상 경로(.jasper 파일)
	 * @param map : Jasper 파일 형식으로 변환할 Map 객체
	 */
	public static void render(ServletContext sc, HttpServletResponse response, String jasperFilePath, Map<String, Object> map) {
		render(sc, response, jasperFilePath, map, null);
	}

	/**
	 * Map을 Jasper 파일 형식으로 출력한다.
	 * @param sc 서블릿 컨텍스트 객체
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param jasperFilePath Jasper Report 대상 경로(.jasper 파일)
	 * @param map Jasper 파일 형식으로 변환할 Map 객체
	 * @param fileName 파일명
	 */
	public static void render(ServletContext sc, HttpServletResponse response, String jasperFilePath, Map<String, Object> map, String fileName) {
		try {
			JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(sc.getRealPath(jasperFilePath));
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, new JREmptyDataSource());
			// 헤더셋팅
			response.reset();
			response.setContentType("application/pdf");
			if (StringUtil.isNotEmpty(fileName)) {
				response.setHeader("Content-Disposition", "inline; filename=\"" + URLEncoder.encode(fileName, "utf-8").replaceAll("\\+", " ") + "\"");
			}
			response.setHeader("Pragma", "no-cache;");
			response.setHeader("Expires", "-1;");
			// PDF 출력
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * List<Map<String, ?>> 객체를 JRMapCollectionDataSource 형식으로 반환한다.
	 * @param list JRMapCollectionDataSource 형식으로 변환할 List<Map<String, ?>> 객체
	 * @return JRMapCollectionDataSource 데이터소스
	 */
	public static JRMapCollectionDataSource parse(List<Map<String, ?>> list) {
		return new JRMapCollectionDataSource(list);
	}
}