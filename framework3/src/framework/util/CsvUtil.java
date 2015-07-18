package framework.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import framework.db.RecordMap;
import framework.db.RecordSet;

/**
 * CSV를 이용하여 개발할 때 이용할 수 있는 유틸리티 클래스이다.
 */
public class CsvUtil {
	private static final Log logger = LogFactory.getLog(framework.util.CsvUtil.class);

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private CsvUtil() {
	}

	/**
	 * 확장자에 의해서 파일을 파싱한다.
	 * @param fileItem 파일아이템
	 * @return 데이터의 리스트
	 */
	public static List<LinkedHashMap<String, String>> parse(FileItem fileItem, String sep) {
		InputStream is = null;
		try {
			is = fileItem.getInputStream();
			return parseSep(is, sep);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
	}

	/**
	 * 확장자에 의해서 파일을 파싱한다.
	 * @param file 파일
	 * @return 데이터의 리스트
	 */
	public static List<LinkedHashMap<String, String>> parse(File file, String sep) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return parseSep(fis, sep);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
	}

	/**
	 * RecordSet을 CSV 형식으로 변환하여 응답객체로 전송한다. 
	 * @param response
	 * @param rs
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderCSV(HttpServletResponse response, RecordSet rs, String fileName) {
		return renderSep(response, rs, fileName, ",");
	}

	/**
	 * RecordSet을 CSV 형식으로 변환한다. 
	 * @param rs
	 * @return CSV 형식으로 변환된 문자열
	 */
	public static String renderCSV(RecordSet rs) {
		return renderSep(rs, ",");
	}

	/**
	 * RecordSet을 TSV 형식으로 변환하여 응답객체로 전송한다. 
	 * @param response
	 * @param rs
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderTSV(HttpServletResponse response, RecordSet rs, String fileName) {
		return renderSep(response, rs, fileName, "\t");
	}

	/**
	 * RecordSet을 TSV 형식으로 변환한다. 
	 * @param rs
	 * @return TSV 형식으로 변환된 문자열
	 */
	public static String renderTSV(RecordSet rs) {
		return renderSep(rs, "\t");
	}

	/**
	 * RecordSet을 구분자(CSV, TSV 등)파일 형식으로 출력한다. 
	 * <br>
	 * ex) response로 rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 => CsvUtil.renderSep(response, rs, ",")
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs 구분자(CSV, TSV 등)파일 형식으로 변환할 RecordSet 객체
	 * @param fileName
	 * @param sep 열 구분자로 쓰일 문자열
	 * @return 처리건수
	 */
	public static int renderSep(HttpServletResponse response, RecordSet rs, String fileName, String sep) {
		if (rs == null) {
			return 0;
		}
		int rowCount = 0;
		try {
			response.reset();
			response.setContentType("application/octet-stream;");
			response.setHeader("Content-Disposition", (new StringBuilder("attachment; filename=\"")).append(new String(fileName.getBytes(), "ISO-8859-1")).append("\"").toString());
			response.setHeader("Pragma", "no-cache;");
			response.setHeader("Expires", "-1;");
			PrintWriter pw = response.getWriter();
			String[] colNms = rs.getColumns();
			rs.moveRow(0);
			while (rs.nextRow()) {
				if (rowCount++ > 0) {
					pw.print("\n");
				}
				pw.print(sepRowStr(rs, colNms, sep));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * RecordSet을 구분자(CSV, TSV 등)파일 형식으로 변환한다. 
	 * <br>
	 * ex) rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 : String csv = CsvUtil.renderSep(rs, ",")
	 * @param rs 변환할 RecordSet 객체
	 * @param sep 열 구분자로 쓰일 문자열
	 * @return 구분자(CSV, TSV 등)파일 형식으로 변환된 문자열
	 */
	public static String renderSep(RecordSet rs, String sep) {
		if (rs == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		String[] colNms = rs.getColumns();
		rs.moveRow(0);
		int rowCount = 0;
		while (rs.nextRow()) {
			if (rowCount++ > 0) {
				buffer.append("\n");
			}
			buffer.append(sepRowStr(rs, colNms, sep));
		}
		return buffer.toString();
	}

	/**
	 * ResultSet을 CSV 형식으로 변환하여 응답객체로 전송한다. 
	 * @param response
	 * @param rs
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderCSV(HttpServletResponse response, ResultSet rs, String fileName) {
		return renderSep(response, rs, fileName, ",");
	}

	/**
	 * ResultSet을 CSV 형식으로 변환한다. 
	 * @param rs
	 * @return CSV 형식으로 변환된 문자열
	 */
	public static String renderCSV(ResultSet rs) {
		return renderSep(rs, ",");
	}

	/**
	 * ResultSet을 TSV 형식으로 변환하여 응답객체로 전송한다. 
	 * @param response
	 * @param rs
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderTSV(HttpServletResponse response, ResultSet rs, String fileName) {
		return renderSep(response, rs, fileName, "\t");
	}

	/**
	 * ResultSet을 TSV 형식으로 변환한다. 
	 * @param rs
	 * @return TSV 형식으로 변환된 문자열
	 */
	public static String renderTSV(ResultSet rs) {
		return renderSep(rs, "\t");
	}

	/**
	 * ResultSet을 구분자(CSV, TSV 등)파일 형식으로 출력한다. 
	 * <br>
	 * ex) response로 rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 => CsvUtil.renderSep(response, rs, ",")
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs 구분자(CSV, TSV 등)파일 형식으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param fileName
	 * @param sep 열 구분자로 쓰일 문자열
	 * @return 처리건수
	 */
	public static int renderSep(HttpServletResponse response, ResultSet rs, String fileName, String sep) {
		if (rs == null) {
			return 0;
		}
		try {
			response.reset();
			response.setContentType("application/octet-stream;");
			response.setHeader("Content-Disposition", (new StringBuilder("attachment; filename=\"")).append(new String(fileName.getBytes(), "ISO-8859-1")).append("\"").toString());
			response.setHeader("Pragma", "no-cache;");
			response.setHeader("Expires", "-1;");
			PrintWriter pw = response.getWriter();
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				int cnt = rsmd.getColumnCount();
				String[] colNms = new String[cnt];
				for (int i = 1; i <= cnt; i++) {
					//Table의 Field 가 소문자 인것은 대문자로 변경처리
					colNms[i - 1] = rsmd.getColumnName(i).toUpperCase();
				}
				int rowCount = 0;
				while (rs.next()) {
					if (rowCount++ > 0) {
						pw.print("\n");
					}
					pw.print(sepRowStr(rs, colNms, sep));
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
	 * ResultSet을 구분자(CSV, TSV 등)파일 형식으로 변환한다.
	 * <br>
	 * ex) rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 : String csv = CsvUtil.renderSep(rs, ",")
	 * @param rs 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param sep 열 구분자로 쓰일 문자열
	 * @return 구분자(CSV, TSV 등)파일 형식으로 변환된 문자열
	 */
	public static String renderSep(ResultSet rs, String sep) {
		if (rs == null) {
			return null;
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
				int rowCount = 0;
				while (rs.next()) {
					if (rowCount++ > 0) {
						buffer.append("\n");
					}
					buffer.append(sepRowStr(rs, colNms, sep));
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
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return buffer.toString();
	}

	/**
	 * List객체를 CSV 형식으로 변환하여 응답객체로 전송한다. 
	 * @param response
	 * @param mapList
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderCSV(HttpServletResponse response, List<RecordMap> mapList, String fileName) {
		return renderSep(response, mapList, fileName, ",");
	}

	/**
	 * List객체를 CSV 형식으로 변환한다. 
	 * @param mapList
	 * @return CSV 형식으로 변환된 문자열
	 */
	public static String renderCSV(List<RecordMap> mapList) {
		return renderSep(mapList, ",");
	}

	/**
	 * List객체를 TSV 형식으로 변환하여 응답객체로 전송한다. 
	 * @param response
	 * @param mapList
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderTSV(HttpServletResponse response, List<RecordMap> mapList, String fileName) {
		return renderSep(response, mapList, fileName, "\t");
	}

	/**
	 * List객체를 TSV 형식으로 변환한다. 
	 * @param mapList
	 * @return TSV 형식으로 변환된 문자열
	 */
	public static String renderTSV(List<RecordMap> mapList) {
		return renderSep(mapList, "\t");
	}

	/**
	 * List객체를 구분자(CSV, TSV 등)파일 형식으로 출력한다. 
	 * <br>
	 * ex) response로 mapList를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 => CsvUtil.renderSep(response, mapList, ",")
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param mapList 구분자(CSV, TSV 등)파일 형식으로 변환할 List 객체
	 * @param fileName
	 * @param sep 열 구분자로 쓰일 문자열
	 * @return 처리건수
	 */
	public static int renderSep(HttpServletResponse response, List<RecordMap> mapList, String fileName, String sep) {
		if (mapList == null) {
			return 0;
		}
		int rowCount = 0;
		try {
			response.reset();
			response.setContentType("application/octet-stream;");
			response.setHeader("Content-Disposition", (new StringBuilder("attachment; filename=\"")).append(new String(fileName.getBytes(), "ISO-8859-1")).append("\"").toString());
			response.setHeader("Pragma", "no-cache;");
			response.setHeader("Expires", "-1;");
			PrintWriter pw = response.getWriter();
			for (RecordMap map : mapList) {
				if (rowCount++ > 0) {
					pw.print("\n");
				}
				pw.print(sepRowStr(map, sep));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * List객체를 구분자(CSV, TSV 등)파일 형식으로 변환한다. 
	 * <br>
	 * ex1) mapList를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 : String csv = CsvUtil.renderSep(mapList, ",")
	 * @param mapList 변환할 List객체
	 * @param sep 열 구분자로 쓰일 문자열
	 * @return 구분자(CSV, TSV 등)파일 형식으로 변환된 문자열
	 */
	public static String renderSep(List<RecordMap> mapList, String sep) {
		if (mapList == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		int rowCount = 0;
		for (RecordMap map : mapList) {
			if (rowCount++ > 0) {
				buffer.append("\n");
			}
			buffer.append(sepRowStr(map, sep));
		}
		return buffer.toString();
	}

	/**
	 * Map객체를 CSV 형식으로 변환하여 응답객체로 전송한다. 
	 * @param response
	 * @param map
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderCSV(HttpServletResponse response, RecordMap map, String fileName) {
		return renderSep(response, map, fileName, ",");
	}

	/**
	 * Map객체를 CSV 형식으로 변환한다. 
	 * @param map
	 * @return CSV 형식으로 변환된 문자열
	 */
	public static String renderCSV(RecordMap map) {
		return renderSep(map, ",");
	}

	/**
	 * Map객체를 TSV 형식으로 변환하여 응답객체로 전송한다. 
	 * @param response
	 * @param map
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderTSV(HttpServletResponse response, RecordMap map, String fileName) {
		return renderSep(response, map, fileName, "\t");
	}

	/**
	 * Map객체를 TSV 형식으로 변환한다. 
	 * @param map
	 * @return TSV 형식으로 변환된 문자열
	 */
	public static String renderTSV(RecordMap map) {
		return renderSep(map, "\t");
	}

	/**
	 * Map객체를 구분자(CSV, TSV 등)파일 형식으로 출력한다. 
	 * <br>
	 * ex) response로 map을 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 => CsvUtil.renderSep(response, map, ",")
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param map 변환할 Map객체
	 * @param fileName
	 * @param sep 열 구분자로 쓰일 문자열
	 * @return 처리건수
	 */
	public static int renderSep(HttpServletResponse response, RecordMap map, String fileName, String sep) {
		if (map == null) {
			return 0;
		}
		try {
			response.reset();
			response.setContentType("application/octet-stream;");
			response.setHeader("Content-Disposition", (new StringBuilder("attachment; filename=\"")).append(new String(fileName.getBytes(), "ISO-8859-1")).append("\"").toString());
			response.setHeader("Pragma", "no-cache;");
			response.setHeader("Expires", "-1;");
			PrintWriter pw = response.getWriter();
			pw.print(sepRowStr(map, sep));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return 1;
	}

	/**
	 * Map객체를 구분자(CSV, TSV 등)파일 형식으로 변환한다. 
	 * <br>
	 * ex) map을 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 : String csv = CsvUtil.renderSep(map, ",")
	 * @param map 변환할 Map객체
	 * @param sep 열 구분자로 쓰일 문자열
	 * @return 구분자(CSV, TSV 등)파일 형식으로 변환된 문자열
	 */
	public static String renderSep(RecordMap map, String sep) {
		if (map == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append(sepRowStr(map, sep));
		return buffer.toString();
	}

	/**
	 * 구분자로 쓰이는 문자열 또는 개행문자가 값에 포함되어 있을 경우 값을 쌍따옴표로 둘러싸도록 변환한다.
	 * @param str 변환할 문자열
	 * @param sep 열 구분자로 쓰일 문자열
	 */
	public static String escapeSep(String str, String sep) {
		if (str == null) {
			return "";
		}
		return (str.contains(sep) || str.contains("\n")) ? "\"" + str + "\"" : str;
	}

	////////////////////////////////////////////////////////////////////////////////////////// Private 메소드

	/**
	 * 구분자(CSV, TSV 등)파일 생성용 Row 문자열 생성
	 * 데이타가 숫자가 아닐때에는 구분자로 쓰인 문자열 또는 개행문자를 escape 하기 위해 값을 쌍따옴표로 둘러싼다.
	 */
	private static String sepRowStr(RecordMap map, String sep) {
		StringBuilder buffer = new StringBuilder();
		Set<String> keys = map.keySet();
		int rowCount = 0;
		for (String key : keys) {
			Object value = map.get(key);
			if (rowCount++ > 0) {
				buffer.append(sep);
			}
			if (value == null) {
				buffer.append("");
			} else {
				if (value instanceof Number) {
					buffer.append(value.toString());
				} else {
					buffer.append(escapeSep(value.toString(), sep));
				}
			}
		}
		return buffer.toString();
	}

	/**
	 * 구분자(CSV, TSV 등)파일 생성용 Row 문자열 생성
	 * 데이타가 숫자가 아닐때에는 구분자로 쓰인 문자열 또는 개행문자를 escape 하기 위해 값을 쌍따옴표로 둘러싼다.
	 */
	private static String sepRowStr(RecordSet rs, String[] colNms, String sep) {
		if (colNms == null) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		int rowCount = 0;
		for (int c = 0; c < colNms.length; c++) {
			Object value = rs.get(colNms[c]);
			if (rowCount++ > 0) {
				buffer.append(sep);
			}
			if (value == null) {
				buffer.append("");
			} else {
				if (value instanceof Number) {
					buffer.append(value.toString());
				} else {
					buffer.append(escapeSep(value.toString(), sep));
				}
			}
		}
		return buffer.toString();
	}

	/**
	 * 구분자(CSV, TSV 등)파일 생성용 Row 문자열 생성
	 * 데이타가 숫자가 아닐때에는 구분자로 쓰인 문자열 또는 개행문자를 escape 하기 위해 값을 쌍따옴표로 둘러싼다.
	 */
	private static String sepRowStr(ResultSet rs, String[] colNms, String sep) {
		StringBuilder buffer = new StringBuilder();
		int rowCount = 0;
		for (int c = 0; c < colNms.length; c++) {
			Object value;
			try {
				value = rs.getObject(colNms[c]);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			if (rowCount++ > 0) {
				buffer.append(sep);
			}
			if (value == null) {
				buffer.append("");
			} else {
				if (value instanceof Number) {
					buffer.append(value.toString());
				} else {
					buffer.append(escapeSep(value.toString(), sep));
				}
			}
		}
		return buffer.toString();
	}

	private static List<LinkedHashMap<String, String>> parseSep(InputStream is, String sep) {
		List<LinkedHashMap<String, String>> mapList = new ArrayList<LinkedHashMap<String, String>>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] items = line.split(sep);
				LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
				for (int i = 0; i < items.length; i++) {
					map.put(String.valueOf(i), items[i]);
				}
				mapList.add(map);
			}
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
		return mapList;
	}
}