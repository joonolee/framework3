package framework.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

/**
 * PDF 변환시 이용할 수 있는 유틸리티 클래스이다.
 */
public class PdfUtil {
	private static final Log logger = LogFactory.getLog(framework.util.PdfUtil.class);

	public enum Orientation {
		PORTRAIT, LANDSCAPE
	};

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private PdfUtil() {
	}

	/**
	 * HTML 문자열을 PDF 형식(A4사이즈)으로 변환한 파일을 다운로드 한다.
	 * @param html HTML 문자열(CSS 포함)
	 * @param response pdf 파일을 전송할 응답객체
	 * @param fileName 다운로드할 파일명
	 * @param marginLeft 좌측 여백
	 * @param marginRight 우측 여백
	 * @param marginTop 상단 여백
	 * @param marginBottom 하단 여백
	 * @param orientation 용지 방향
	 * @param fontDir 폰트파일(ttf)이 저장되어 있는 디렉토리 경로
	 */
	public static void htmlToPdf(String html, HttpServletResponse response, String fileName, float marginLeft, float marginRight, float marginTop, float marginBottom, Orientation orientation, String fontDir) {
		try {
			response.reset();
			response.setContentType("application/octet-stream;");
			response.setHeader("Content-Disposition", (new StringBuilder("attachment; filename=\"")).append(new String(fileName.getBytes(), "ISO-8859-1")).append("\"").toString());
			response.setHeader("Pragma", "no-cache;");
			response.setHeader("Expires", "-1;");
			htmlToPdf(html, response.getOutputStream(), marginLeft, marginRight, marginTop, marginBottom, orientation, fontDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * HTML 문자열을 PDF 형식(A4사이즈)으로 변환하여 응답객체에 전송한다.
	 * @param html HTML 문자열(CSS 포함)
	 * @param response pdf 파일을 전송할 응답객체
	 * @param marginLeft 좌측 여백
	 * @param marginRight 우측 여백
	 * @param marginTop 상단 여백
	 * @param marginBottom 하단 여백
	 * @param orientation 용지 방향
	 * @param fontDir 폰트파일(ttf)이 저장되어 있는 디렉토리 경로
	 */
	public static void htmlToPdf(String html, HttpServletResponse response, float marginLeft, float marginRight, float marginTop, float marginBottom, Orientation orientation, String fontDir) {
		try {
			response.reset();
			response.setContentType("application/pdf");
			response.setHeader("Pragma", "no-cache;");
			response.setHeader("Expires", "-1;");
			htmlToPdf(html, response.getOutputStream(), marginLeft, marginRight, marginTop, marginBottom, orientation, fontDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * HTML 문자열을 PDF 형식(A4사이즈)으로 변환하여 파일에 저장한다.
	 * @param html HTML 문자열(CSS 포함)
	 * @param destPath 저장할 파일명
	 * @param marginLeft 좌측 여백
	 * @param marginRight 우측 여백
	 * @param marginTop 상단 여백
	 * @param marginBottom 하단 여백
	 * @param orientation 용지 방향
	 * @param fontDir 폰트파일(ttf)이 저장되어 있는 디렉토리 경로
	 */
	public static void htmlToPdf(String html, String destPath, float marginLeft, float marginRight, float marginTop, float marginBottom, Orientation orientation, String fontDir) {
		htmlToPdf(html, new File(destPath), marginLeft, marginRight, marginTop, marginBottom, orientation, fontDir);
	}

	/**
	 * HTML 문자열을 PDF 형식(A4사이즈)으로 변환하여 파일에 저장한다.
	 * @param html HTML 문자열(CSS 포함)
	 * @param destFile 저장할 파일 객체
	 * @param marginLeft 좌측 여백
	 * @param marginRight 우측 여백
	 * @param marginTop 상단 여백
	 * @param marginBottom 하단 여백
	 * @param orientation 용지 방향
	 * @param fontDir 폰트파일(ttf)이 저장되어 있는 디렉토리 경로
	 */
	public static void htmlToPdf(String html, File destFile, float marginLeft, float marginRight, float marginTop, float marginBottom, Orientation orientation, String fontDir) {
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(destFile);
			bos = new BufferedOutputStream(fos);
			htmlToPdf(html, bos, marginLeft, marginRight, marginTop, marginBottom, orientation, fontDir);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
	}

	/**
	 * HTML 문자열을 PDF 형식(A4사이즈)으로 변환하여 파일에 저장한다.
	 * @param html HTML 문자열(CSS 포함)
	 * @param os 출력 스트림
	 * @param marginLeft 좌측 여백
	 * @param marginRight 우측 여백
	 * @param marginTop 상단 여백
	 * @param marginBottom 하단 여백
	 * @param orientation 용지 방향
	 * @param fontDir 폰트파일(ttf)이 저장되어 있는 디렉토리 경로
	 */
	public static void htmlToPdf(String html, OutputStream os, float marginLeft, float marginRight, float marginTop, float marginBottom, Orientation orientation, String fontDir) {
		Document doc = null;
		PdfWriter pdfWriter = null;
		try {
			Rectangle pageSize = null;
			if (orientation == Orientation.PORTRAIT) {
				pageSize = PageSize.A4;
			} else {
				pageSize = PageSize.A4.rotate();
			}
			doc = new Document(pageSize, marginLeft, marginRight, marginRight, marginBottom);
			pdfWriter = PdfWriter.getInstance(doc, os);
			pdfWriter.setInitialLeading(12.5f);
			doc.open();
			XMLWorkerFontProvider fontProvider = new XMLWorkerFontProvider(XMLWorkerFontProvider.DONTLOOKFORFONTS);
			if (fontDir != null && !"".equals(fontDir)) {
				fontProvider.registerDirectory(fontDir);
			}
			fontProvider.setUseUnicode(true);
			HtmlPipelineContext context = new HtmlPipelineContext(new CssAppliersImpl(fontProvider));
			context.setTagFactory(Tags.getHtmlTagProcessorFactory());
			context.charSet(Charset.forName("UTF-8"));
			CSSResolver cssResolver = new StyleAttrCSSResolver();
			CssResolverPipeline pipeline = new CssResolverPipeline(cssResolver, new HtmlPipeline(context, new PdfWriterPipeline(doc, pdfWriter)));
			XMLWorker worker = new XMLWorker(pipeline, true);
			XMLParser parser = new XMLParser(worker, Charset.forName("UTF-8"));
			parser.parse(new StringReader(_stripMetaTag(html)));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (doc != null) {
				doc.close();
			}
			if (pdfWriter != null) {
				pdfWriter.flush();
				pdfWriter.close();
			}
		}
	}

	private static String _stripMetaTag(String src) {
		Pattern pattern = Pattern.compile("<\\s*[m|M][e|E][t|T][a|A].*?>", Pattern.DOTALL);
		return pattern.matcher(src).replaceAll("");
	}
}