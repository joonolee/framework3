package framework.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import framework.db.RecordMap;
import framework.db.RecordSet;

/**
 * OZ Report 를 이용하여 개발할 때 이용할 수 있는 유틸리티 클래스이다.
 */
public class OZUtil {
	private static final Log logger = LogFactory.getLog(framework.util.OZUtil.class);

	/**
	 * 디폴트 열 구분자
	 */
	private static final String DEFAULT_COLSEP = "##";

	/**
	 * 디폴트 행 구분자
	 */
	private static final String DEFAULT_LINESEP = "\r\n";

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private OZUtil() {
	}

	/**
	 * RecordSet을 OZ 파일 형식으로 출력한다.
	 * 행, 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) response로 rs를 OZ 파일 형식으로 출력하는 경우 : OZUtil.render(response, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs OZ 파일 형식으로 변환할 RecordSet 객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, RecordSet rs) {
		return render(response, rs, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * RecordSet을 OZ 파일 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 OZ 파일 형식으로 출력하는 경우 : OZUtil.render(response, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs OZ 파일 형식으로 변환할 RecordSet 객체
	 * @param colSep 열 구분자로 쓰일 문자열
	 * @param lineSep 행 구분자로 쓰일 문자열
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, RecordSet rs, String colSep, String lineSep) {
		if (response == null || rs == null || colSep == null || lineSep == null) {
			return 0;
		}
		PrintWriter pw;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String[] colNms = rs.getColumns();
		int[] colType = rs.getColumnsType();
		if (colNms != null) {
			// Column Name
			for (int c = 0; c < colNms.length; c++) {
				if (c > 0) {
					pw.print(colSep);
				}
				pw.print(colNms[c]);
			}
			pw.print(lineSep);
			// Column Type
			for (int c = 0; c < colNms.length; c++) {
				if (c > 0) {
					pw.print(colSep);
				}
				switch (colType[c]) {
				case Types.TINYINT:
					pw.print("TinyInt");
					break;
				case Types.SMALLINT:
					pw.print("SmallInt");
					break;
				case Types.BIGINT:
					pw.print("BigInt");
					break;
				case Types.INTEGER:
					pw.print("Integer");
					break;
				case Types.REAL:
					pw.print("Real");
					break;
				case Types.FLOAT:
					pw.print("Float");
					break;
				case Types.DOUBLE:
					pw.print("Double");
					break;
				case Types.NUMERIC:
					pw.print("Numeric");
					break;
				case Types.DECIMAL:
					pw.print("Decimal");
					break;
				case Types.CHAR:
					pw.print("Char");
					break;
				case Types.DATE:
					pw.print("Date");
					break;
				case Types.TIME:
					pw.print("Time");
					break;
				case Types.TIMESTAMP:
					pw.print("TimeStamp");
					break;
				default:
					pw.print("VarChar");
					break;
				}
			}
		}
		pw.print(lineSep);
		rs.moveRow(0);
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				pw.print(lineSep);
			}
			pw.print(ozRowStr(rs, colNms, colSep));
		}
		return rowCount;
	}

	/**
	 * RecordSet을 OZ 파일 형식으로 변환한다.
	 * 행, 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) rs를 OZ 파일 형식으로 변환하는 경우 : String oz = OZUtil.render(rs)
	 * @param rs 변환할 RecordSet 객체
	 * @return OZ 파일 형식으로 변환된 문자열
	 */
	public static String render(RecordSet rs) {
		return render(rs, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * RecordSet을 OZ 파일 형식으로 변환한다.
	 * <br>
	 * ex) rs를 열구분자 ##, 행구분자 !! 인 OZ 파일 형식으로 변환하는 경우 : String oz = OZUtil.render(rs, "##", "!!")
	 * @param rs 변환할 RecordSet 객체
	 * @param colSep 열 구분자로 쓰일 문자열
	 * @param lineSep 행 구분자로 쓰일 문자열
	 * @return OZ 파일 형식으로 변환된 문자열
	 */
	public static String render(RecordSet rs, String colSep, String lineSep) {
		if (rs == null || colSep == null || lineSep == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		String[] colNms = rs.getColumns();
		int[] colType = rs.getColumnsType();
		if (colNms != null) {
			// Column Name
			for (int c = 0; c < colNms.length; c++) {
				if (c > 0) {
					buf.append(colSep);
				}
				buf.append(colNms[c]);
			}
			buf.append(lineSep);
			// Column Type
			for (int c = 0; c < colNms.length; c++) {
				if (c > 0) {
					buf.append(colSep);
				}
				switch (colType[c]) {
				case Types.TINYINT:
					buf.append("TinyInt");
					break;
				case Types.SMALLINT:
					buf.append("SmallInt");
					break;
				case Types.BIGINT:
					buf.append("BigInt");
					break;
				case Types.INTEGER:
					buf.append("Integer");
					break;
				case Types.REAL:
					buf.append("Real");
					break;
				case Types.FLOAT:
					buf.append("Float");
					break;
				case Types.DOUBLE:
					buf.append("Double");
					break;
				case Types.NUMERIC:
					buf.append("Numeric");
					break;
				case Types.DECIMAL:
					buf.append("Decimal");
					break;
				case Types.CHAR:
					buf.append("Char");
					break;
				case Types.DATE:
					buf.append("Date");
					break;
				case Types.TIME:
					buf.append("Time");
					break;
				case Types.TIMESTAMP:
					buf.append("TimeStamp");
					break;
				default:
					buf.append("VarChar");
					break;
				}
			}
		}
		buf.append(lineSep);
		rs.moveRow(0);
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				buf.append(lineSep);
			}
			buf.append(ozRowStr(rs, colNms, colSep));
		}
		return buf.toString();
	}

	/**
	 * ResultSet을 OZ 파일 형식으로 출력한다.
	 * 행, 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) response로 rs를 OZ 파일 형식으로 출력하는 경우 : OZUtil.render(response, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs OZ 파일 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, ResultSet rs) {
		return render(response, rs, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * ResultSet을 OZ 파일 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 OZ 파일 형식으로 출력하는 경우 : OZUtil.render(response, rs, "##", "!!")
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs OZ 파일 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param colSep 열 구분자로 쓰일 문자열
	 * @param lineSep 행 구분자로 쓰일 문자열
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, ResultSet rs, String colSep, String lineSep) {
		if (response == null || rs == null || colSep == null || lineSep == null) {
			return 0;
		}
		try {
			PrintWriter pw = response.getWriter();
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				int cnt = rsmd.getColumnCount();
				String[] colNms = new String[cnt];
				int[] colType = new int[cnt];
				for (int i = 1; i <= cnt; i++) {
					colNms[i - 1] = rsmd.getColumnName(i).toLowerCase();
					colType[i - 1] = rsmd.getColumnType(i);
				}
				// Column Name
				for (int c = 0; c < colNms.length; c++) {
					if (c > 0) {
						pw.print(colSep);
					}
					pw.print(colNms[c]);
				}
				pw.print(lineSep);
				// Column Type
				for (int c = 0; c < colNms.length; c++) {
					if (c > 0) {
						pw.print(colSep);
					}
					switch (colType[c]) {
					case Types.TINYINT:
						pw.print("TinyInt");
						break;
					case Types.SMALLINT:
						pw.print("SmallInt");
						break;
					case Types.BIGINT:
						pw.print("BigInt");
						break;
					case Types.INTEGER:
						pw.print("Integer");
						break;
					case Types.REAL:
						pw.print("Real");
						break;
					case Types.FLOAT:
						pw.print("Float");
						break;
					case Types.DOUBLE:
						pw.print("Double");
						break;
					case Types.NUMERIC:
						pw.print("Numeric");
						break;
					case Types.DECIMAL:
						pw.print("Decimal");
						break;
					case Types.CHAR:
						pw.print("Char");
						break;
					case Types.DATE:
						pw.print("Date");
						break;
					case Types.TIME:
						pw.print("Time");
						break;
					case Types.TIMESTAMP:
						pw.print("TimeStamp");
						break;
					default:
						pw.print("VarChar");
						break;
					}
				}
				pw.print(lineSep);
				int rowCount = 0;
				while (rs.next()) {
					if (rowCount++ > 0) {
						pw.print(lineSep);
					}
					pw.print(ozRowStr(rs, colNms, colSep));
				}
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
	 * ResultSet을 OZ 파일 형식으로 변환한다.
	 * 행, 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) rs를 OZ 파일 형식으로 변환하는 경우 : String oz = OZUtil.render(rs)
	 * @param rs 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @return OZ 파일 형식으로 변환된 문자열
	 */
	public static String render(ResultSet rs) {
		return render(rs, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * ResultSet을 OZ 파일 형식으로 변환한다.
	 * <br>
	 * ex) rs를 열구분자 ##, 행구분자 !! 인 OZ 파일 형식으로 변환하는 경우 : String oz = OZUtil.render(rs, "##", "!!")
	 * @param rs 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param colSep 열 구분자로 쓰일 문자열
	 * @param lineSep 행 구분자로 쓰일 문자열
	 * @return OZ 파일 형식으로 변환된 문자열
	 */
	public static String render(ResultSet rs, String colSep, String lineSep) {
		if (rs == null || colSep == null || lineSep == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		try {
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				int cnt = rsmd.getColumnCount();
				String[] colNms = new String[cnt];
				int[] colType = new int[cnt];
				for (int i = 1; i <= cnt; i++) {
					colNms[i - 1] = rsmd.getColumnName(i).toLowerCase();
					colType[i - 1] = rsmd.getColumnType(i);
				}
				// Column Name
				for (int c = 0; c < colNms.length; c++) {
					if (c > 0) {
						buf.append(colSep);
					}
					buf.append(colNms[c]);
				}
				buf.append(lineSep);
				// Column Type
				for (int c = 0; c < colNms.length; c++) {
					if (c > 0) {
						buf.append(colSep);
					}
					switch (colType[c]) {
					case Types.TINYINT:
						buf.append("TinyInt");
						break;
					case Types.SMALLINT:
						buf.append("SmallInt");
						break;
					case Types.BIGINT:
						buf.append("BigInt");
						break;
					case Types.INTEGER:
						buf.append("Integer");
						break;
					case Types.REAL:
						buf.append("Real");
						break;
					case Types.FLOAT:
						buf.append("Float");
						break;
					case Types.DOUBLE:
						buf.append("Double");
						break;
					case Types.NUMERIC:
						buf.append("Numeric");
						break;
					case Types.DECIMAL:
						buf.append("Decimal");
						break;
					case Types.CHAR:
						buf.append("Char");
						break;
					case Types.DATE:
						buf.append("Date");
						break;
					case Types.TIME:
						buf.append("Time");
						break;
					case Types.TIMESTAMP:
						buf.append("TimeStamp");
						break;
					default:
						buf.append("VarChar");
						break;
					}
				}
				buf.append(lineSep);
				int rowCount = 0;
				while (rs.next()) {
					if (rowCount++ > 0) {
						buf.append(lineSep);
					}
					buf.append(ozRowStr(rs, colNms, colSep));
				}
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
		return buf.toString();
	}

	/**
	 * Map객체를 OZ 파일 형식으로 출력한다.
	 * 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) response로 map를 OZ 파일 형식으로 출력하는 경우 : OZUtil.render(response, map)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param map 변환할 Map객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, RecordMap map) {
		return render(response, map, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * Map객체를 OZ 파일 형식으로 출력한다.
	 * <br>
	 * ex) response로 map를 열구분자 ## 인 OZ 파일 형식으로 출력하는 경우 : OZUtil.render(response, map, "##", "!!")
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param map 변환할 Map객체
	 * @param colSep 열 구분자로 쓰일 문자열
	 * @param lineSep 행 구분자로 쓰일 문자열
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, RecordMap map, String colSep, String lineSep) {
		if (response == null || map == null || colSep == null || lineSep == null) {
			return 0;
		}
		PrintWriter pw;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		int rowCount = 0;
		for (String colName : map.keySet()) {
			if (rowCount++ > 0) {
				pw.print(colSep);
			}
			pw.print(colName);
		}
		pw.print(lineSep);
		rowCount = 0;
		for (Object value : map.values()) {
			if (rowCount++ > 0) {
				pw.print(colSep);
			}
			if (value instanceof Number) {
				pw.print("Numeric");
			} else {
				pw.print("VarChar");
			}
		}
		pw.print(lineSep);
		pw.print(ozRowStr(map, colSep));
		return 1;
	}

	/**
	 * Map객체를 OZ 파일 형식으로 변환한다.
	 * 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) map을 OZ 파일 형식으로 변환하는 경우 : String oz = OZUtil.render(map)
	 * @param map 변환할 Map객체
	 * @return OZ 파일 형식으로 변환된 문자열
	 */
	public static String render(RecordMap map) {
		return render(map, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * Map객체를 OZ 파일 형식으로 변환한다.
	 * <br>
	 * ex) map을 열구분자 ## 인 OZ 파일 형식으로 변환하는 경우 : String oz = OZUtil.render(map, "##", "!!")
	 * @param map 변환할 Map객체
	 * @param colSep 열 구분자로 쓰일 문자열
	 * @param lineSep 행 구분자로 쓰일 문자열
	 * @return OZ 파일 형식으로 변환된 문자열
	 */
	public static String render(RecordMap map, String colSep, String lineSep) {
		if (map == null || colSep == null || lineSep == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		int rowCount = 0;
		for (String colName : map.keySet()) {
			if (rowCount++ > 0) {
				buf.append(colSep);
			}
			buf.append(colName);
		}
		buf.append(lineSep);
		rowCount = 0;
		for (Object value : map.values()) {
			if (rowCount++ > 0) {
				buf.append(colSep);
			}
			if (value instanceof Number) {
				buf.append("Numeric");
			} else {
				buf.append("VarChar");
			}
		}
		buf.append(lineSep);
		buf.append(ozRowStr(map, colSep));
		return buf.toString();
	}

	/**
	 * List객체를 OZ 파일 형식으로 출력한다.
	 * 행, 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) response로 mapList를 OZ 파일 형식으로 출력하는 경우 : OZUtil.render(response, mapList)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param mapList 변환할 List객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, List<RecordMap> mapList) {
		return render(response, mapList, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * List객체를 OZ 파일 형식으로 출력한다.
	 * <br>
	 * ex) response로 mapList를 OZ 파일 형식으로 출력하는 경우 : OZUtil.render(response, mapList, "##", "!!")
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param mapList 변환할 List객체
	 * @param colSep 열 구분자로 쓰일 문자열
	 * @param lineSep 행 구분자로 쓰일 문자열
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, List<RecordMap> mapList, String colSep, String lineSep) {
		if (response == null || mapList == null || colSep == null || lineSep == null) {
			return 0;
		}
		PrintWriter pw;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		int rowCount = 0;
		if (mapList.size() > 0) {
			RecordMap map = mapList.get(0);
			for (String colName : map.keySet()) {
				if (rowCount++ > 0) {
					pw.print(colSep);
				}
				pw.print(colName);
			}
			pw.print(lineSep);
			rowCount = 0;
			for (Object value : map.values()) {
				if (rowCount++ > 0) {
					pw.print(colSep);
				}
				if (value instanceof Number) {
					pw.print("Numeric");
				} else {
					pw.print("VarChar");
				}
			}
			pw.print(lineSep);
		}
		rowCount = 0;
		for (RecordMap map : mapList) {
			if (rowCount++ > 0) {
				pw.print(lineSep);
			}
			pw.print(ozRowStr(map, colSep));
		}
		return rowCount;
	}

	/**
	 * List객체를 OZ 파일 형식으로 변환한다.
	 * 행, 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex1) mapList를 OZ 파일 형식으로 변환하는 경우 : String oz = OZUtil.render(mapList)
	 * @param mapList 변환할 List객체
	 * @return OZ 파일 형식으로 변환된 문자열
	 */
	public static String render(List<RecordMap> mapList) {
		return render(mapList, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * List객체를 OZ 파일 형식으로 변환한다.
	 * <br>
	 * ex1) mapList를 열구분자 ##, 행구분자 !! 인 OZ 파일 형식으로 변환하는 경우 : String oz = OZUtil.render(mapList, "##", "!!")
	 * @param mapList 변환할 List객체
	 * @param colSep 열 구분자로 쓰일 문자열
	 * @param lineSep 행 구분자로 쓰일 문자열
	 * @return OZ 파일 형식으로 변환된 문자열
	 */
	public static String render(List<RecordMap> mapList, String colSep, String lineSep) {
		if (mapList == null || colSep == null || lineSep == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		int rowCount = 0;
		if (mapList.size() > 0) {
			RecordMap map = mapList.get(0);
			for (String colName : map.keySet()) {
				if (rowCount++ > 0) {
					buf.append(colSep);
				}
				buf.append(colName);
			}
			buf.append(lineSep);
			rowCount = 0;
			for (Object value : map.values()) {
				if (rowCount++ > 0) {
					buf.append(colSep);
				}
				if (value instanceof Number) {
					buf.append("Numeric");
				} else {
					buf.append("VarChar");
				}
			}
			buf.append(lineSep);
		}
		rowCount = 0;
		for (RecordMap map : mapList) {
			if (rowCount++ > 0) {
				buf.append(lineSep);
			}
			buf.append(ozRowStr(map, colSep));
		}
		return buf.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private 메소드

	/**
	 * 캐리지리턴, 라인피드 문자들을 변환하여준다.
	 *
	 * @param str 변환할 문자열
	 */
	private static String escape(String str) {
		if (str == null) {
			return "";
		}
		return str.replaceAll("\r\n", "\\\\n").replaceAll("\r", "\\\\n").replaceAll("\n", "\\\\n");
	}

	/**
	 * OZ 리포트용 Row 문자열 생성
	 */
	private static String ozRowStr(RecordMap map, String colSep) {
		if (map == null || colSep == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		int rowCount = 0;
		for (Entry<String, Object> entry : map.entrySet()) {
			if (rowCount++ > 0) {
				buf.append(colSep);
			}
			Object value = entry.getValue();
			if (value != null) {
				buf.append(escape(value.toString()));
			}
		}
		return buf.toString();
	}

	/**
	 * OZ 리포트용 Row 문자열 생성
	 */
	private static String ozRowStr(RecordSet rs, String[] colNms, String colSep) {
		if (rs == null || colNms == null || colSep == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		for (int c = 0; c < colNms.length; c++) {
			if (c > 0) {
				buf.append(colSep);
			}
			if (rs.get(colNms[c]) != null) {
				buf.append(escape(rs.getString(colNms[c])));
			}
		}
		return buf.toString();
	}

	private static String ozRowStr(ResultSet rs, String[] colNms, String colSep) {
		if (rs == null || colNms == null || colSep == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		try {
			for (int c = 0; c < colNms.length; c++) {
				if (c > 0) {
					buf.append(colSep);
				}
				if (rs.getObject(colNms[c]) != null) {
					buf.append(escape(rs.getString(colNms[c])));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return buf.toString();
	}
}