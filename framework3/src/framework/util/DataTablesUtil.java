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

import framework.db.RecordSet;

/**
 * DataTables 를 이용하여 개발할 때 이용할 수 있는 유틸리티 클래스이다.
 */
public class DataTablesUtil {
	protected static final Log logger = LogFactory.getLog(framework.util.DataTablesUtil.class);

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private DataTablesUtil() {
	}

	/**
	 * RecordSet을 DataTables 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 DataTables 형식으로 출력하는 경우 : DataTablesUtil.render(response, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs DataTables 형식으로 변환할 RecordSet 객체
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
		pw.print("{");
		int rowCount = 0;
		pw.print("\"aaData\":[");
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				pw.print(",");
			}
			pw.print(_dataTablesRowStr(rs, colNms));
		}
		pw.print("]");
		pw.print("}");
		return rowCount;
	}

	/**
	 * RecordSet을 DataTables 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 DataTables 형식으로 출력하는 경우 : DataTablesUtil.render(response, rs, new String[] { "col1", "col2" })
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs DataTables 형식으로 변환할 RecordSet 객체
	 * @param colNames 컬럼이름 배열
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, RecordSet rs, String[] colNames) {
		if (rs == null) {
			return 0;
		}
		PrintWriter pw;
		try {
			pw = response.getWriter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		rs.moveRow(0);
		pw.print("{");
		int rowCount = 0;
		pw.print("\"aaData\":[");
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				pw.print(",");
			}
			pw.print(_dataTablesRowStr(rs, colNames));
		}
		pw.print("]");
		pw.print("}");
		return rowCount;
	}

	/**
	 * RecordSet을 DataTables 형식으로 변환한다.
	 * <br>
	 * ex) rs를 DataTables 형식으로 변환하는 경우 : String json = DataTablesUtil.render(rs)
	 * @param rs DataTables 형식으로 변환할 RecordSet 객체
	 * @return DataTables 형식으로 변환된 문자열
	 */
	public static String render(RecordSet rs) {
		StringBuilder buffer = new StringBuilder();
		if (rs == null) {
			return "";
		}
		String[] colNms = rs.getColumns();
		rs.moveRow(0);
		buffer.append("{");
		int rowCount = 0;
		buffer.append("\"aaData\":[");
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				buffer.append(",");
			}
			buffer.append(_dataTablesRowStr(rs, colNms));
		}
		buffer.append("]");
		buffer.append("}");
		return buffer.toString();
	}

	/**
	 * RecordSet을 DataTables 형식으로 변환한다.
	 * <br>
	 * ex) rs를 DataTables 형식으로 변환하는 경우 : String json = DataTablesUtil.render(rs, new String[] { "col1", "col2" })
	 * @param rs DataTables 형식으로 변환할 RecordSet 객체
	 * @param colNames 컬럼이름 배열
	 * @return DataTables 형식으로 변환된 문자열
	 */
	public static String render(RecordSet rs, String[] colNames) {
		StringBuilder buffer = new StringBuilder();
		if (rs == null) {
			return "";
		}
		rs.moveRow(0);
		buffer.append("{");
		int rowCount = 0;
		buffer.append("\"aaData\":[");
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				buffer.append(",");
			}
			buffer.append(_dataTablesRowStr(rs, colNames));
		}
		buffer.append("]");
		buffer.append("}");
		return buffer.toString();
	}

	/**
	 * ResultSet을 DataTables 형식으로 출력한다. 
	 * <br>
	 * ex) response로 rs를 DataTables 형식으로 출력하는 경우 : DataTablesUtil.render(response, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs DataTables 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
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
				pw.print("{");
				int rowCount = 0;
				pw.print("\"aaData\":[");
				while (rs.next()) {
					if (rowCount++ > 0) {
						pw.print(",");
					}
					pw.print(_dataTablesRowStr(rs, colNms));
				}
				pw.print("]");
				pw.print("}");
				return rowCount;
			} finally {
				Statement stmt = null;
				try {
					stmt = rs.getStatement();
				} catch (SQLException e) {
					logger.error(e);
				}
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						logger.error(e);
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						logger.error(e);
					}
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * ResultSet을 DataTables 형식으로 출력한다.
	 * <br>
	 * ex) response로 rs를 DataTables 형식으로 출력하는 경우 : DataTablesUtil.render(response, rs, new String[] { "col1", "col2" })
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs DataTables 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param colNames 컬럼이름 배열
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, ResultSet rs, String[] colNames) {
		if (rs == null) {
			return 0;
		}
		try {
			PrintWriter pw = response.getWriter();
			try {
				pw.print("{");
				int rowCount = 0;
				pw.print("\"aaData\":[");
				while (rs.next()) {
					if (rowCount++ > 0) {
						pw.print(",");
					}
					pw.print(_dataTablesRowStr(rs, colNames));
				}
				pw.print("]");
				pw.print("}");
				return rowCount;
			} finally {
				Statement stmt = null;
				try {
					stmt = rs.getStatement();
				} catch (SQLException e) {
					logger.error(e);
				}
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						logger.error(e);
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						logger.error(e);
					}
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * ResultSet을 DataTables 형식으로 변환한다.
	 * <br>
	 * ex) rs를 DataTables 형식으로 변환하는 경우 : String json = DataTablesUtil.render(rs)
	 * @param rs DataTables 형식으로 변환할 ResultSet 객체
	 * @return DataTables 형식으로 변환된 문자열
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
				int rowCount = 0;
				buffer.append("{");
				buffer.append("\"aaData\":[");
				while (rs.next()) {
					if (rowCount++ > 0) {
						buffer.append(",");
					}
					buffer.append(_dataTablesRowStr(rs, colNms));
				}
				buffer.append("]");
				buffer.append("}");
			} finally {
				Statement stmt = null;
				try {
					stmt = rs.getStatement();
				} catch (SQLException e) {
					logger.error(e);
				}
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						logger.error(e);
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						logger.error(e);
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return buffer.toString();
	}

	/**
	 * ResultSet을 DataTables 형식으로 변환한다. 
	 * <br>
	 * ex) rs를 DataTables 형식으로 변환하는 경우 : String json = DataTablesUtil.render(rs, new String[] { "col1", "col2" })
	 * @param rs DataTables 형식으로 변환할 ResultSet 객체
	 * @param colNames 컬럼이름 배열
	 * @return DataTables 형식으로 변환된 문자열
	 */
	public static String render(ResultSet rs, String[] colNames) {
		if (rs == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		try {
			try {
				int rowCount = 0;
				buffer.append("{");
				buffer.append("\"aaData\":[");
				while (rs.next()) {
					if (rowCount++ > 0) {
						buffer.append(",");
					}
					buffer.append(_dataTablesRowStr(rs, colNames));
				}
				buffer.append("]");
				buffer.append("}");
			} finally {
				Statement stmt = null;
				try {
					stmt = rs.getStatement();
				} catch (SQLException e) {
					logger.error(e);
				}
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						logger.error(e);
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						logger.error(e);
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return buffer.toString();
	}

	/**
	 * List객체를 DataTables 형식으로 변환한다. DataTablesUtil.format과 동일
	 * <br>
	 * ex1) mapList를 DataTables 형식으로 변환하는 경우 : String json = DataTablesUtil.render(mapList)
	 * @param mapList 변환할 List객체
	 * @return DataTables 형식으로 변환된 문자열
	 */
	public static String render(List<Map<String, Object>> mapList) {
		if (mapList == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("{");
		buffer.append("\"aaData\":");
		if (mapList.size() > 0) {
			buffer.append("[");
			for (Map<String, Object> map : mapList) {
				buffer.append(_dataTablesRowStr(map));
				buffer.append(",");
			}
			buffer.delete(buffer.length() - 1, buffer.length());
			buffer.append("]");
		} else {
			buffer.append("[]");
		}
		buffer.append("}");
		return buffer.toString();
	}

	/**
	 * 자바스크립트상에 특수하게 인식되는 문자들을 JSON등에 사용하기 위해 변환하여준다.
	 * @param str 변환할 문자열
	 */
	public static String escapeJS(String str) {
		if (str == null) {
			return "";
		}
		return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\r\n", "\\\\n").replaceAll("\n", "\\\\n");
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private 메소드

	/**
	 * DataTables 용 Row 문자열 생성
	 */
	private static String _dataTablesRowStr(Map<String, Object> map) {
		StringBuilder buffer = new StringBuilder();
		if (map.entrySet().size() > 0) {
			buffer.append("[");
			for (Entry<String, Object> entry : map.entrySet()) {
				Object value = entry.getValue();
				if (value == null) {
					buffer.append("\"\"");
				} else {
					buffer.append("\"" + escapeJS(value.toString()) + "\"");
				}
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
	 * DataTables 용 Row 문자열 생성
	 */
	private static String _dataTablesRowStr(RecordSet rs, String[] colNms) {
		StringBuilder buffer = new StringBuilder();
		if (colNms != null && colNms.length > 0) {
			buffer.append("[");
			for (int c = 0; c < colNms.length; c++) {
				Object value = rs.get(colNms[c].toUpperCase());
				if (value == null) {
					buffer.append("\"\"");
				} else {
					buffer.append("\"" + escapeJS(value.toString()) + "\"");
				}
				buffer.append(",");
			}
			buffer.delete(buffer.length() - 1, buffer.length());
			buffer.append("]");
		} else {
			buffer.append("[]");
		}
		return buffer.toString();
	}

	private static String _dataTablesRowStr(ResultSet rs, String[] colNms) {
		StringBuilder buffer = new StringBuilder();
		if (colNms.length > 0) {
			buffer.append("[");
			for (int c = 0; c < colNms.length; c++) {
				Object value;
				try {
					value = rs.getObject(colNms[c].toUpperCase());
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				if (value == null) {
					buffer.append("\"\"");
				} else {
					buffer.append("\"" + escapeJS(value.toString()) + "\"");
				}
				buffer.append(",");
			}
			buffer.delete(buffer.length() - 1, buffer.length());
			buffer.append("]");
		} else {
			buffer.append("[]");
		}
		return buffer.toString();
	}
}