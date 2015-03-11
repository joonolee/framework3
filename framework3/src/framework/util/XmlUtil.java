package framework.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import framework.db.RecordMap;
import framework.db.RecordSet;

/**
 * XML을 이용하여 개발할 때 이용할 수 있는 유틸리티 클래스이다.
 */
public class XmlUtil {
	private static final Log logger = LogFactory.getLog(framework.util.XmlUtil.class);

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private XmlUtil() {
	}

	/**
	 * RecordSet을 xml 형식으로 출력한다. (xml 헤더포함). 
	 * <br>
	 * ex) response로 rs를 xml 형식으로 출력하는 경우 : XmlUtil.render(response, rs, "utf-8")
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs xml 형식으로 변환할 RecordSet 객체
	 * @param encoding 헤더에 포함될 인코딩
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, RecordSet rs, String encoding) {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String[] colNms = rs.getColumns();
		rs.moveRow(0);
		pw.print(_xmlHeaderStr(encoding));
		pw.print("<items>");
		int rowCount = 0;
		while (rs.nextRow()) {
			rowCount++;
			pw.print(_xmlItemStr(rs, colNms));
		}
		pw.print("</items>");
		return rowCount;
	}

	/**
	 * RecordSet을 xml 형식으로 변환한다. (xml 헤더 미포함).
	 * <br>
	 * ex) rs를 xml 형식으로 변환하는 경우 : String xml = XmlUtil.render(rs)
	 * @param rs xml 형식으로 변환할 RecordSet 객체
	 * @return xml 형식으로 변환된 문자열
	 */
	public static String render(RecordSet rs) {
		if (rs == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		String[] colNms = rs.getColumns();
		rs.moveRow(0);
		buffer.append("<items>");
		while (rs.nextRow()) {
			buffer.append(_xmlItemStr(rs, colNms));
		}
		buffer.append("</items>");
		return buffer.toString();
	}

	/**
	 * RecordSet을 xml 형식으로 변환한다. (xml 헤더포함). 
	 * <br>
	 * ex) rs를 xml 형식으로 변환하는 경우 : String xml = XmlUtil.render(rs, "utf-8")
	 * @param rs xml 형식으로 변환할 RecordSet 객체
	 * @param encoding 헤더에 포함될 인코딩
	 * @return xml 형식으로 변환된 문자열
	 */
	public static String render(RecordSet rs, String encoding) {
		if (rs == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		buf.append(_xmlHeaderStr(encoding));
		buf.append(render(rs));
		return buf.toString();
	}

	/**
	 * ResultSet을 xml 형식으로 출력한다 (xml 헤더포함). 
	 * <br>
	 * ex) response로 rs를 xml 형식으로 출력하는 경우 : XmlUtil.render(response, rs, "utf-8")
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs xml 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param encoding 헤더에 포함될 인코딩
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, ResultSet rs, String encoding) {
		if (rs == null) {
			return 0;
		}
		try {
			PrintWriter pw = response.getWriter();
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				int cnt = rsmd.getColumnCount();
				String[] colNms = new String[cnt];
				for (int i = 1; i <= cnt; i++) {
					//Table의 Field 가 소문자 인것은 대문자로 변경처리
					colNms[i - 1] = rsmd.getColumnName(i).toUpperCase();
				}
				pw.print(_xmlHeaderStr(encoding));
				pw.print("<items>");
				int rowCount = 0;
				while (rs.next()) {
					rowCount++;
					pw.print(_xmlItemStr(rs, colNms));
				}
				pw.print("</items>");
				return rowCount;
			} finally {
				Statement stmt = null;
				try {
					stmt = rs.getStatement();
				} catch (SQLException e) {
					logger.error("", e);
				}
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						logger.error("", e);
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						logger.error("", e);
					}
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * ResultSet을 xml 형식으로 변환한다 (xml 헤더 미포함). 
	 * <br>
	 * ex) rs를 xml 형식으로 변환하는 경우 : String xml = XmlUtil.render(rs)
	 * @param rs xml 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 */
	public static String render(ResultSet rs) {
		if (rs == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		try {
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				int cnt = rsmd.getColumnCount();
				String[] colNms = new String[cnt];
				for (int i = 1; i <= cnt; i++) {
					//Table의 Field 가 소문자 인것은 대문자로 변경처리
					colNms[i - 1] = rsmd.getColumnName(i).toUpperCase();
				}
				buffer.append("<items>");
				while (rs.next()) {
					buffer.append(_xmlItemStr(rs, colNms));
				}
				buffer.append("</items>");
			} finally {
				Statement stmt = null;
				try {
					stmt = rs.getStatement();
				} catch (SQLException e) {
					logger.error("", e);
				}
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						logger.error("", e);
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						logger.error("", e);
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return buffer.toString();
	}

	/**
	 * ResultSet을 xml 형식으로 변환한다 (xml 헤더포함). 
	 * <br>
	 * ex) rs를 xml 형식으로 변환하는 경우 : String xml = XmlUtil.render(rs, "utf-8")
	 * @param rs xml 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param encoding 헤더에 포함될 인코딩
	 */
	public static String render(ResultSet rs, String encoding) {
		if (rs == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(_xmlHeaderStr(encoding));
		buffer.append(render(rs));
		return buffer.toString();
	}

	/**
	 * Map객체를 xml 형식으로 변환한다 (xml 헤더 미포함). 
	 * <br>
	 * ex) map을 xml 형식으로 변환하는 경우 : String xml = XmlUtil.render(map)
	 * @param map 변환할 Map객체
	 * @return xml 형식으로 변환된 문자열
	 */
	public static String render(RecordMap map) {
		if (map == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("<items>");
		buffer.append(_xmlItemStr(map));
		buffer.append("</items>");
		return buffer.toString();
	}

	/**
	 * Map객체를 xml 형식으로 변환한다 (xml 헤더포함). 
	 * <br>
	 * ex) map을 xml 형식으로 변환하는 경우  : String xml = XmlUtil.render(map, "utf-8")
	 * @param map 변환할 Map객체
	 * @param encoding 헤더에 포함될 인코딩
	 * @return xml 형식으로 변환된 문자열
	 */
	public static String render(RecordMap map, String encoding) {
		if (map == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(_xmlHeaderStr(encoding));
		buffer.append(render(map));
		return buffer.toString();
	}

	/**
	 * List객체를 xml 형식으로 출력한다. (xml 헤더포함). 
	 * <br>
	 * ex) response로 mapList를 xml 형식으로 출력하는 경우 : XmlUtil.render(response, mapList, "utf-8")
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param mapList 변환할 List객체
	 * @param encoding 헤더에 포함될 인코딩
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, List<RecordMap> mapList, String encoding) {
		if (mapList == null) {
			return 0;
		}
		PrintWriter pw;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		pw.print(_xmlHeaderStr(encoding));
		pw.print("<items>");
		int rowCount = 0;
		for (RecordMap map : mapList) {
			rowCount++;
			pw.print(_xmlItemStr(map));
		}
		pw.print("</items>");
		return rowCount;
	}

	/**
	 * List객체를 xml 형태로 변환한다 (xml 헤더 미포함). 
	 * <br>
	 * ex) mapList를 xml으로 변환하는 경우 : String xml = XmlUtil.render(mapList)
	 * @param mapList 변환할 List객체
	 * @return xml형식으로 변환된 문자열
	 */
	public static String render(List<RecordMap> mapList) {
		if (mapList == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("<items>");
		for (RecordMap map : mapList) {
			buffer.append(_xmlItemStr(map));
		}
		buffer.append("</items>");
		return buffer.toString();
	}

	/**
	 * List객체를 xml 형태로 변환한다 (xml 헤더포함).
	 * <br>
	 * ex) mapList를 xml으로 변환하는 경우  : String xml = XmlUtil.render(mapList, "utf-8")
	 * @param mapList 변환할 List객체
	 * @param encoding 헤더에 포함될 인코딩
	 * @return xml형식으로 변환된 문자열
	 */
	public static String render(List<RecordMap> mapList, String encoding) {
		if (mapList == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(_xmlHeaderStr(encoding));
		buffer.append(render(mapList));
		return buffer.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private 메소드

	/**
	 *  xml 헤더 문자열 생성
	 */
	private static String _xmlHeaderStr(String encoding) {
		return "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
	}

	/**
	 * xml item 문자열 생성
	 */
	@SuppressWarnings("unchecked")
	private static String _xmlItemStr(RecordMap map) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<item>");
		for (Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value == null) {
				buffer.append("<" + key.toLowerCase() + ">" + "</" + key.toLowerCase() + ">");
			} else {
				if (value instanceof Number) {
					buffer.append("<" + key.toLowerCase() + ">" + value.toString() + "</" + key.toLowerCase() + ">");
				} else if (value instanceof Map) {
					buffer.append("<" + key.toLowerCase() + ">" + render((RecordMap) value) + "</" + key.toLowerCase() + ">");
				} else if (value instanceof List) {
					buffer.append("<" + key.toLowerCase() + ">" + render((List<RecordMap>) value) + "</" + key.toLowerCase() + ">");
				} else {
					buffer.append("<" + key.toLowerCase() + ">" + "<![CDATA[" + value.toString() + "]]>" + "</" + key.toLowerCase() + ">");
				}
			}
		}
		buffer.append("</item>");
		return buffer.toString();
	}

	/**
	 * xml item 문자열 생성
	 */
	private static String _xmlItemStr(RecordSet rs, String[] colNms) {
		if (colNms == null) {
			return "<item></item>";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("<item>");
		for (int c = 0; c < colNms.length; c++) {
			Object value = rs.get(colNms[c]);
			if (value == null) {
				buffer.append("<" + colNms[c].toLowerCase() + ">" + "</" + colNms[c].toLowerCase() + ">");
			} else {
				if (value instanceof Number) {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + value.toString() + "</" + colNms[c].toLowerCase() + ">");
				} else {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + "<![CDATA[" + value.toString() + "]]>" + "</" + colNms[c].toLowerCase() + ">");
				}
			}
		}
		buffer.append("</item>");
		return buffer.toString();
	}

	private static String _xmlItemStr(ResultSet rs, String[] colNms) {
		if (colNms == null) {
			return "<item></item>";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("<item>");
		for (int c = 0; c < colNms.length; c++) {
			Object value;
			try {
				value = rs.getObject(colNms[c]);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			if (value == null) {
				buffer.append("<" + colNms[c].toLowerCase() + ">" + "</" + colNms[c].toLowerCase() + ">");
			} else {
				if (value instanceof Number) {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + value.toString() + "</" + colNms[c].toLowerCase() + ">");
				} else {
					buffer.append("<" + colNms[c].toLowerCase() + ">" + "<![CDATA[" + value.toString() + "]]>" + "</" + colNms[c].toLowerCase() + ">");
				}
			}
		}
		buffer.append("</item>");
		return buffer.toString();
	}
}