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
import org.stringtree.json.JSONReader;
import org.stringtree.json.JSONWriter;

import framework.db.RecordMap;
import framework.db.RecordSet;

/**
 * JSON(JavaScript Object Notation)를 이용하여 개발할 때 이용할 수 있는 유틸리티 클래스이다.
 */
public class JsonUtil {
	private static final Log logger = LogFactory.getLog(framework.util.JsonUtil.class);

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private JsonUtil() {
	}

	/**
	 * RecordSet을 JSON 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 JSON 형식으로 출력하는 경우 : JsonUtil.render(response, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs JSON 형식으로 변환할 RecordSet 객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, RecordSet rs) {
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
		pw.print("[");
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				pw.print(",");
			}
			pw.print(jsonRowStr(rs, colNms));
		}
		pw.print("]");
		return rowCount;
	}

	/**
	 * RecordSet을 Json 배열 형태로 변환한다.
	 * <br>
	 * ex) rs를 JSON 형식으로 변환하는 경우 : String json = JsonUtil.render(rs) 
	 * @param rs JSON 형식으로 변환할 RecordSet 객체
	 * @return JSON 형식으로 변환된 문자열
	 */
	public static String render(RecordSet rs) {
		if (rs == null) {
			return "";
		}
		String[] colNms = rs.getColumns();
		rs.moveRow(0);
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				buf.append(",");
			}
			buf.append(jsonRowStr(rs, colNms));
		}
		buf.append("]");
		return buf.toString();
	}

	/**
	 * ResultSet을 JSON 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 JSON 형식으로 출력하는 경우 : JsonUtil.render(response, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs JSON 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, ResultSet rs) {
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
				pw.print("[");
				int rowCount = 0;
				while (rs.next()) {
					if (rowCount++ > 0) {
						pw.print(",");
					}
					pw.print(jsonRowStr(rs, colNms));
				}
				pw.print("]");
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
	 * ResultSet을 Json 배열 형태로 변환한다.
	 * <br>
	 * ex) rs를 JSON 형식으로 변환하는 경우 : String json = JsonUtil.render(rs)
	 * @param rs JSON 형식으로 변환할 ResultSet 객체
	 * @return JSON 형식으로 변환된 문자열
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
				buffer.append("[");
				int rowCount = 0;
				while (rs.next()) {
					if (rowCount++ > 0) {
						buffer.append(",");
					}
					buffer.append(jsonRowStr(rs, colNms));
				}
				buffer.append("]");
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
		return buffer.toString();
	}

	/**
	 * List객체를 JSON 형식으로 출력한다.
	 * <br>
	 * ex) response로 mapList를 JSON 형식으로 출력하는 경우 : JsonUtil.render(response, mapList)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param mapList 변환할 List객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, List<RecordMap> mapList) {
		if (mapList == null) {
			return 0;
		}
		PrintWriter pw;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		pw.print("[");
		int rowCount = 0;
		for (RecordMap map : mapList) {
			if (rowCount++ > 0) {
				pw.print(",");
			}
			pw.print(jsonRowStr(map));
		}
		pw.print("]");
		return rowCount;
	}

	/**
	 * List객체를 JSON 형식으로 변환한다.
	 * <br>
	 * ex1) mapList를 JSON 형식으로 변환하는 경우 : String json = JsonUtil.render(mapList)
	 * @param mapList 변환할 List객체
	 * @return JSON 형식으로 변환된 문자열
	 */
	public static String render(List<RecordMap> mapList) {
		if (mapList == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		if (mapList.size() > 0) {
			buffer.append("[");
			for (RecordMap map : mapList) {
				buffer.append(jsonRowStr(map));
				buffer.append(",");
			}
			buffer.delete(buffer.length() - 1, buffer.length());
			buffer.append("]");
		} else {
			buffer.append("[]");
		}
		return buffer.toString();
	}

	/**
	 * Map객체를 JSON 형식으로 출력한다.
	 * <br>
	 * ex) response로 map을 JSON 형식으로 출력하는 경우 : JsonUtil.render(response, map)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param map 변환할 Map객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, RecordMap map) {
		if (map == null) {
			return 0;
		}
		PrintWriter pw;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		pw.print(jsonRowStr(map));
		return 1;
	}

	/**
	 * Map객체를 JSON 형식으로 변환한다.
	 * <br>
	 * ex) map을 JSON 형식으로 변환하는 경우 : String json = JsonUtil.render(map)
	 * @param map 변환할 Map객체
	 * @return JSON 형식으로 변환된 문자열
	 */
	public static String render(RecordMap map) {
		if (map == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(jsonRowStr(map));
		return buffer.toString();
	}

	/**
	 * 객체를 JSON 형식으로 변환한다.
	 * <br>
	 * ex1) obj를 JSON 형식으로 변환하는 경우 : String json = JsonUtil.stringify(obj)
	 * @param obj 변환할 객체
	 * @return JSON 형식으로 변환된 문자열
	 */
	public static String stringify(Object obj) {
		JSONWriter writer = new JSONWriter();
		return writer.write(obj);
	}

	/**
	 * JSON 문자열을 Object 로 변환한다.
	 * <br>
	 * ex1) json를 Object 형식으로 변환하는 경우 : Object obj = JsonUtil.parse(json)
	 * @param json 변환할 JSON 문자열
	 * @return Object 형식으로 변환된 객체
	 */
	public static Object parse(String json) {
		JSONReader reader = new JSONReader();
		return reader.read(json);
	}

	/**
	 * JSON 문자열을 예쁘게 들여쓰기를 적용하여 정렬한다.
	 * @param json json 변환할 JSON 문자열
	 * @return Object 형식으로 변환된 객체
	 */
	public static String pretty(String json) {
		return pretty(json, "    ");
	}

	/**
	 * JSON 문자열을 예쁘게 들여쓰기를 적용하여 정렬한다.
	 * @param json json json 변환할 JSON 문자열
	 * @param indent 들여쓰기에 사용할 문자열
	 * @return Object 형식으로 변환된 객체
	 */
	public static String pretty(String json, String indent) {
		StringBuilder buf = new StringBuilder();
		int level = 0;
		String target = null;
		for (int i = 0; i < json.length(); i++) {
			target = json.substring(i, i + 1);
			if (target.equals("{") || target.equals("[")) {
				buf.append(target).append("\n");
				level++;
				for (int j = 0; j < level; j++) {
					buf.append(indent);
				}
			} else if (target.equals("}") || target.equals("]")) {
				buf.append("\n");
				level--;
				for (int j = 0; j < level; j++) {
					buf.append(indent);
				}
				buf.append(target);
			} else if (target.equals(",")) {
				buf.append(target);
				buf.append("\n");
				for (int j = 0; j < level; j++) {
					buf.append(indent);
				}
			} else {
				buf.append(target);
			}
		}
		return buf.toString();
	}

	/**
	 * 자바스크립트상에 특수하게 인식되는 문자들을 JSON등에 사용하기 위해 변환하여준다.
	 * @param str 변환할 문자열
	 */
	public static String escapeJS(String str) {
		if (str == null) {
			return "";
		}
		return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\r\n", "\\\\n").replaceAll("\n", "\\\\n").replaceAll("\t", "\\\\t");
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private 메소드

	/**
	 * JSON 용 Row 문자열 생성
	 */
	@SuppressWarnings("unchecked")
	private static String jsonRowStr(RecordMap map) {
		StringBuilder buf = new StringBuilder();
		if (map.entrySet().size() > 0) {
			buf.append("{");
			for (Entry<String, Object> entry : map.entrySet()) {
				String key = "\"" + escapeJS(entry.getKey().toLowerCase()) + "\"";
				Object value = entry.getValue();
				if (value == null) {
					buf.append(key + ":" + "\"\"");
				} else {
					if (value instanceof Number || value instanceof Boolean) {
						buf.append(key + ":" + value.toString());
					} else if (value instanceof Map) {
						buf.append(key + ":" + render((RecordMap) value));
					} else if (value instanceof List) {
						buf.append(key + ":" + render((List<RecordMap>) value));
					} else {
						buf.append(key + ":" + "\"" + escapeJS(value.toString()) + "\"");
					}
				}
				buf.append(",");
			}
			buf.delete(buf.length() - 1, buf.length());
			buf.append("}");
		} else {
			buf.append("{}");
		}
		return buf.toString();
	}

	/**
	 * JSON 용 Row 문자열 생성
	 */
	private static String jsonRowStr(RecordSet rs, String[] colNms) {
		StringBuilder buf = new StringBuilder();
		if (colNms != null && colNms.length > 0) {
			buf.append("{");
			for (int c = 0; c < colNms.length; c++) {
				Object value = rs.get(colNms[c]);
				String key = "\"" + escapeJS(colNms[c].toLowerCase()) + "\"";

				if (value == null) {
					buf.append(key + ":" + "\"\"");
				} else {
					if (value instanceof Number || value instanceof Boolean) {
						buf.append(key + ":" + value.toString());
					} else {
						buf.append(key + ":" + "\"" + escapeJS(value.toString()) + "\"");
					}
				}
				buf.append(",");
			}
			buf.delete(buf.length() - 1, buf.length());
			buf.append("}");
		} else {
			buf.append("{}");
		}
		return buf.toString();
	}

	private static String jsonRowStr(ResultSet rs, String[] colNms) {
		StringBuilder buf = new StringBuilder();
		if (colNms.length > 0) {
			buf.append("{");
			for (int c = 0; c < colNms.length; c++) {
				Object value;
				try {
					value = rs.getObject(colNms[c]);
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				String key = "\"" + escapeJS(colNms[c].toLowerCase()) + "\"";

				if (value == null) {
					buf.append(key + ":" + "\"\"");
				} else {
					if (value instanceof Number || value instanceof Boolean) {
						buf.append(key + ":" + value.toString());
					} else {
						buf.append(key + ":" + "\"" + escapeJS(value.toString()) + "\"");
					}
				}
				buf.append(",");
			}
			buf.delete(buf.length() - 1, buf.length());
			buf.append("}");
		} else {
			buf.append("{}");
		}
		return buf.toString();
	}
}