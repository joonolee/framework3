package framework.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import framework.db.RecordMap;
import framework.db.RecordSet;

/**
 * RD(Report Designer)를 이용하여 개발할 때 이용할 수 있는 유틸리티 클래스
 */
public final class RDUtil {
	private static final Log logger = LogFactory.getLog(RDUtil.class);

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
	private RDUtil() {
	}

	/**
	 * RecordSet을 RD 파일 형식으로 출력한다.
	 * 행, 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) response로 rs를 RD 파일 형식으로 출력하는 경우 : RDUtil.render(response, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs RD 파일 형식으로 변환할 RecordSet 객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, RecordSet rs) {
		return render(response, rs, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * RecordSet을 RD 파일 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 RD 파일 형식으로 출력하는 경우 : RDUtil.render(response, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs RD 파일 형식으로 변환할 RecordSet 객체
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
		rs.moveRow(0);
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				pw.print(lineSep);
			}
			pw.print(rdRowStr(rs, colNms, colSep));
		}
		return rowCount;
	}

	/**
	 * RecordSet을 RD 파일 형식으로 변환한다.
	 * 행, 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) rs를 RD 파일 형식으로 변환하는 경우 : String rd = RDUtil.render(rs)
	 * @param rs 변환할 RecordSet 객체
	 * @return RD 파일 형식으로 변환된 문자열
	 */
	public static String render(RecordSet rs) {
		return render(rs, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * RecordSet을 RD 파일 형식으로 변환한다.
	 * <br>
	 * ex) rs를 열구분자 ##, 행구분자 !! 인 RD 파일 형식으로 변환하는 경우 : String rd = RDUtil.render(rs, "##", "!!")
	 * @param rs 변환할 RecordSet 객체
	 * @param colSep 열 구분자로 쓰일 문자열
	 * @param lineSep 행 구분자로 쓰일 문자열
	 * @return RD 파일 형식으로 변환된 문자열
	 */
	public static String render(RecordSet rs, String colSep, String lineSep) {
		if (rs == null || colSep == null || lineSep == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		String[] colNms = rs.getColumns();
		rs.moveRow(0);
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				buf.append(lineSep);
			}
			buf.append(rdRowStr(rs, colNms, colSep));
		}
		return buf.toString();
	}

	/**
	 * ResultSet을 RD 파일 형식으로 출력한다.
	 * 행, 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) response로 rs를 RD 파일 형식으로 출력하는 경우 : RDUtil.render(response, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs RD 파일 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, ResultSet rs) {
		return render(response, rs, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * ResultSet을 RD 파일 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 RD 파일 형식으로 출력하는 경우 : RDUtil.render(response, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs RD 파일 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
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
				for (int i = 1; i <= cnt; i++) {
					colNms[i - 1] = rsmd.getColumnName(i).toLowerCase();
				}
				int rowCount = 0;
				while (rs.next()) {
					if (rowCount++ > 0) {
						pw.print(lineSep);
					}
					pw.print(rdRowStr(rs, colNms, colSep));
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
	 * ResultSet을 RD 파일 형식으로 변환한다.
	 * 행, 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) rs를 RD 파일 형식으로 변환하는 경우 : String rd = RDUtil.render(rs)
	 * @param rs 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @return RD 파일 형식으로 변환된 문자열
	 */
	public static String render(ResultSet rs) {
		return render(rs, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * ResultSet을 RD 파일 형식으로 변환한다.
	 * <br>
	 * ex) rs를 열구분자 ##, 행구분자 !! 인 RD 파일 형식으로 변환하는 경우 : String rd = RDUtil.render(rs, "##", "!!")
	 * @param rs 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param colSep 열 구분자로 쓰일 문자열
	 * @param lineSep 행 구분자로 쓰일 문자열
	 * @return RD 파일 형식으로 변환된 문자열
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
				for (int i = 1; i <= cnt; i++) {
					colNms[i - 1] = rsmd.getColumnName(i).toLowerCase();
				}
				int rowCount = 0;
				while (rs.next()) {
					if (rowCount++ > 0) {
						buf.append(lineSep);
					}
					buf.append(rdRowStr(rs, colNms, colSep));
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
	 * Map객체를 RD 파일 형식으로 출력한다.
	 * 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) response로 map를 RD 파일 형식으로 출력하는 경우 : RDUtil.render(response, map)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param map 변환할 Map객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, RecordMap map) {
		return render(response, map, DEFAULT_COLSEP);
	}

	/**
	 * Map객체를 RD 파일 형식으로 출력한다.
	 * <br>
	 * ex) response로 map를 열구분자 ## 인 RD 파일 형식으로 출력하는 경우 : RDUtil.render(response, map, "##")
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param map 변환할 Map객체
	 * @param colSep 열 구분자로 쓰일 문자열
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, RecordMap map, String colSep) {
		if (response == null || map == null || colSep == null) {
			return 0;
		}
		PrintWriter pw;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		pw.print(rdRowStr(map, colSep));
		return 1;
	}

	/**
	 * Map객체를 RD 파일 형식으로 변환한다.
	 * 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) map을 RD 파일 형식으로 변환하는 경우 : String rd = RDUtil.render(map)
	 * @param map 변환할 Map객체
	 * @return RD 파일 형식으로 변환된 문자열
	 */
	public static String render(RecordMap map) {
		return render(map, DEFAULT_COLSEP);
	}

	/**
	 * Map객체를 RD 파일 형식으로 변환한다.
	 * <br>
	 * ex) map을 열구분자 ## 인 RD 파일 형식으로 변환하는 경우 : String rd = RDUtil.render(map, "##")
	 * @param map 변환할 Map객체
	 * @param colSep 열 구분자로 쓰일 문자열
	 * @return RD 파일 형식으로 변환된 문자열
	 */
	public static String render(RecordMap map, String colSep) {
		if (map == null || colSep == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		buf.append(rdRowStr(map, colSep));
		return buf.toString();
	}

	/**
	 * List객체를 RD 파일 형식으로 출력한다.
	 * 행, 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex) response로 mapList를 RD 파일 형식으로 출력하는 경우 : RDUtil.render(response, mapList)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param mapList 변환할 List객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, List<RecordMap> mapList) {
		return render(response, mapList, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * List객체를 RD 파일 형식으로 출력한다.
	 * <br>
	 * ex) response로 mapList를 RD 파일 형식으로 출력하는 경우 : RDUtil.render(response, mapList)
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
		for (RecordMap map : mapList) {
			if (rowCount++ > 0) {
				pw.print(lineSep);
			}
			pw.print(rdRowStr(map, colSep));
		}
		return rowCount;
	}

	/**
	 * List객체를 RD 파일 형식으로 변환한다.
	 * 행, 열 구분자로 디폴트 구분자를 사용한다.
	 * <br>
	 * ex1) mapList를 RD 파일 형식으로 변환하는 경우 : String rd = RDUtil.render(mapList)
	 * @param mapList 변환할 List객체
	 * @return RD 파일 형식으로 변환된 문자열
	 */
	public static String render(List<RecordMap> mapList) {
		return render(mapList, DEFAULT_COLSEP, DEFAULT_LINESEP);
	}

	/**
	 * List객체를 RD 파일 형식으로 변환한다.
	 * <br>
	 * ex1) mapList를 열구분자 ##, 행구분자 !! 인 RD 파일 형식으로 변환하는 경우 : String rd = RDUtil.render(mapList, "##", "!!")
	 * @param mapList 변환할 List객체
	 * @param colSep 열 구분자로 쓰일 문자열
	 * @param lineSep 행 구분자로 쓰일 문자열
	 * @return RD 파일 형식으로 변환된 문자열
	 */
	public static String render(List<RecordMap> mapList, String colSep, String lineSep) {
		if (mapList == null || colSep == null || lineSep == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		if (mapList.size() > 0) {
			for (RecordMap map : mapList) {
				buf.append(rdRowStr(map, colSep));
				buf.append(lineSep);
			}
			buf.delete(buf.length() - lineSep.length(), buf.length());
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
	 * RD(리포트디자이너) 용 Row 문자열 생성
	 */
	private static String rdRowStr(RecordMap map, String colSep) {
		if (map == null || colSep == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		for (Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value != null) {
				buf.append(escape(value.toString()));
			}
			buf.append(colSep);
		}
		return buf.toString();
	}

	/**
	 * RD(리포트디자이너) 용 Row 문자열 생성
	 */
	private static String rdRowStr(RecordSet rs, String[] colNms, String colSep) {
		if (rs == null || colNms == null || colSep == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		for (int c = 0; c < colNms.length; c++) {
			if (rs.get(colNms[c]) != null) {
				buf.append(escape(rs.getString(colNms[c])));
			}
			buf.append(colSep);
		}
		return buf.toString();
	}

	private static String rdRowStr(ResultSet rs, String[] colNms, String colSep) {
		if (rs == null || colNms == null || colSep == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		try {
			for (int c = 0; c < colNms.length; c++) {
				if (rs.getObject(colNms[c]) != null) {
					buf.append(escape(rs.getString(colNms[c])));
				}
				buf.append(colSep);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return buf.toString();
	}
}