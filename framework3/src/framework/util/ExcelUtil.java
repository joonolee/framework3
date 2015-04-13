package framework.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import framework.db.RecordMap;
import framework.db.RecordSet;

/**
 * Excel 출력을 위해 이용할 수 있는 유틸리티 클래스이다.
 */
public class ExcelUtil {
	private static final Log logger = LogFactory.getLog(framework.util.ExcelUtil.class);

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private ExcelUtil() {
	}

	/**
	 * 확장자에 의해서 엑셀파일을 파싱한다.
	 * @param fileItem 파일아이템
	 * @return 데이터의 리스트
	 */
	public static List<LinkedHashMap<String, String>> parse(FileItem fileItem) {
		String ext = FileUtil.getFileExtension(fileItem.getName());
		InputStream is = null;
		try {
			is = fileItem.getInputStream();
			if ("csv".equalsIgnoreCase(ext)) {
				return parseCSV(is);
			} else if ("tsv".equalsIgnoreCase(ext)) {
				return parseTSV(is);
			} else if ("xls".equalsIgnoreCase(ext)) {
				return parseExcel2003(is);
			} else if ("xlsx".equalsIgnoreCase(ext)) {
				return parseExcel2007(is);
			} else {
				throw new RuntimeException("지원하지 않는 파일포맷입니다.");
			}
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
	 * 암호화된 엑셀파일을 파싱한다.
	 * @param fileItem 파일아이템
	 * @param password 비밀번호
	 * @return 데이터의 리스트
	 */
	public static List<LinkedHashMap<String, String>> parse(FileItem fileItem, String password) {
		String ext = FileUtil.getFileExtension(fileItem.getName());
		InputStream is = null;
		try {
			is = fileItem.getInputStream();
			if ("xls".equalsIgnoreCase(ext)) {
				return parseExcel2003(is, password);
			} else if ("xlsx".equalsIgnoreCase(ext)) {
				return parseExcel2007(is, password);
			} else {
				throw new RuntimeException("지원하지 않는 파일포맷입니다.");
			}
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
	 * 확장자에 의해서 엑셀파일을 파싱한다.
	 * @param file 파일
	 * @return 데이터의 리스트
	 */
	public static List<LinkedHashMap<String, String>> parse(File file) {
		FileInputStream fis = null;
		try {
			String ext = FileUtil.getFileExtension(file);
			fis = new FileInputStream(file);
			if ("csv".equalsIgnoreCase(ext)) {
				return parseCSV(fis);
			} else if ("tsv".equalsIgnoreCase(ext)) {
				return parseTSV(fis);
			} else if ("xls".equalsIgnoreCase(ext)) {
				return parseExcel2003(fis);
			} else if ("xlsx".equalsIgnoreCase(ext)) {
				return parseExcel2007(fis);
			} else {
				throw new RuntimeException("지원하지 않는 파일포맷입니다.");
			}
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
	 * 암호화된 엑셀파일을 파싱한다.
	 * @param file 파일
	 * @return 데이터의 리스트
	 */
	public static List<LinkedHashMap<String, String>> parse(File file, String password) {
		FileInputStream fis = null;
		try {
			String ext = FileUtil.getFileExtension(file);
			fis = new FileInputStream(file);
			if ("xls".equalsIgnoreCase(ext)) {
				return parseExcel2003(fis, password);
			} else if ("xlsx".equalsIgnoreCase(ext)) {
				return parseExcel2007(fis, password);
			} else {
				throw new RuntimeException("지원하지 않는 파일포맷입니다.");
			}
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
	 * RecordSet을 엑셀2003 형식으로 변환하여 응답객체로 전송한다.
	 * @param response
	 * @param rs
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderExcel2003(HttpServletResponse response, RecordSet rs, String fileName) {
		return renderExcel2003(response, rs, fileName, null);
	}

	/**
	 * RecordSet을 엑셀2003 형식으로 변환하여 응답객체로 전송한다.
	 * @param response
	 * @param rs
	 * @param fileName
	 * @param header
	 * @return 처리건수
	 */
	public static int renderExcel2003(HttpServletResponse response, RecordSet rs, String fileName, String[] header) {
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
			Workbook workbook = new HSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			OutputStream os = response.getOutputStream();
			String[] colNms = rs.getColumns();
			if (header != null) {
				Row row = sheet.createRow(rowCount);
				CellStyle cellStyle = headerStyle(workbook);
				appendHeader(row, header, cellStyle);
				rowCount++;
			}
			rs.moveRow(0);
			CellStyle cellStyle = rowStyle(workbook);
			while (rs.nextRow()) {
				Row row = sheet.createRow(rowCount);
				appendRow(row, rs, colNms, cellStyle);
				rowCount++;
			}
			if (colNms != null) {
				for (int i = 0; i < colNms.length; i++) {
					sheet.autoSizeColumn(i);
					sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
				}
			}
			workbook.write(os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * RecordSet을 엑셀2003 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @return 처리건수
	 */
	public static int writeExcel2003(File file, RecordSet rs) {
		return writeExcel2003(file, rs, null);
	}

	/**
	 * RecordSet을 엑셀2003 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @param header
	 * @return 처리건수
	 */
	public static int writeExcel2003(File file, RecordSet rs, String[] header) {
		if (rs == null) {
			return 0;
		}
		int rowCount = 0;
		FileOutputStream fos = null;
		try {
			Workbook workbook = new HSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			fos = new FileOutputStream(file);
			String[] colNms = rs.getColumns();
			if (header != null) {
				Row row = sheet.createRow(rowCount);
				CellStyle cellStyle = headerStyle(workbook);
				appendHeader(row, header, cellStyle);
				rowCount++;
			}
			rs.moveRow(0);
			CellStyle cellStyle = rowStyle(workbook);
			while (rs.nextRow()) {
				Row row = sheet.createRow(rowCount);
				appendRow(row, rs, colNms, cellStyle);
				rowCount++;
			}
			if (colNms != null) {
				for (int i = 0; i < colNms.length; i++) {
					sheet.autoSizeColumn(i);
					sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
				}
			}
			workbook.write(fos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
		return rowCount;
	}

	/**
	 * RecordSet을 엑셀2007 형식으로 변환하여 응답객체로 전송한다. 
	 * @param response
	 * @param rs
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderExcel2007(HttpServletResponse response, RecordSet rs, String fileName) {
		return renderExcel2007(response, rs, fileName, null);
	}

	/**
	 * RecordSet을 엑셀2007 형식으로 변환하여 응답객체로 전송한다. 
	 * @param response
	 * @param rs
	 * @param fileName
	 * @param header
	 * @return 처리건수
	 */
	public static int renderExcel2007(HttpServletResponse response, RecordSet rs, String fileName, String[] header) {
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
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			OutputStream os = response.getOutputStream();
			String[] colNms = rs.getColumns();
			if (header != null) {
				Row row = sheet.createRow(rowCount);
				CellStyle cellStyle = headerStyle(workbook);
				appendHeader(row, header, cellStyle);
				rowCount++;
			}
			rs.moveRow(0);
			CellStyle cellStyle = rowStyle(workbook);
			while (rs.nextRow()) {
				Row row = sheet.createRow(rowCount);
				appendRow(row, rs, colNms, cellStyle);
				rowCount++;
			}
			if (colNms != null) {
				for (int i = 0; i < colNms.length; i++) {
					sheet.autoSizeColumn(i);
					sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
				}
			}
			workbook.write(os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * RecordSet을 엑셀2007 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @return 처리건수
	 */
	public static int writeExcel2007(File file, RecordSet rs) {
		return writeExcel2007(file, rs, null);
	}

	/**
	 * RecordSet을 엑셀2007 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @param header
	 * @return 처리건수
	 */
	public static int writeExcel2007(File file, RecordSet rs, String[] header) {
		if (rs == null) {
			return 0;
		}
		int rowCount = 0;
		FileOutputStream fos = null;
		try {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			fos = new FileOutputStream(file);
			String[] colNms = rs.getColumns();
			if (header != null) {
				Row row = sheet.createRow(rowCount);
				CellStyle cellStyle = headerStyle(workbook);
				appendHeader(row, header, cellStyle);
				rowCount++;
			}
			rs.moveRow(0);
			CellStyle cellStyle = rowStyle(workbook);
			while (rs.nextRow()) {
				Row row = sheet.createRow(rowCount);
				appendRow(row, rs, colNms, cellStyle);
				rowCount++;
			}
			if (colNms != null) {
				for (int i = 0; i < colNms.length; i++) {
					sheet.autoSizeColumn(i);
					sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
				}
			}
			workbook.write(fos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
		return rowCount;
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
	 * RecordSet을 CSV 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @return 처리건수
	 */
	public static int writeCSV(File file, RecordSet rs) {
		return writeSep(file, rs, ",");
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
	 * RecordSet을 TSV 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @return 처리건수
	 */
	public static int writeTSV(File file, RecordSet rs) {
		return writeSep(file, rs, "\t");
	}

	/**
	 * RecordSet을 구분자(CSV, TSV 등)파일 형식으로 출력한다. 
	 * <br>
	 * ex) response로 rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 => ExcelUtil.renderSep(response, rs, ",")
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
	 * RecordSet을 구분자(CSV, TSV 등)파일 형식으로 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @param sep
	 * @return 처리건수
	 */
	public static int writeSep(File file, RecordSet rs, String sep) {
		if (rs == null) {
			return 0;
		}
		int rowCount = 0;
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			String[] colNms = rs.getColumns();
			rs.moveRow(0);
			while (rs.nextRow()) {
				if (rowCount++ > 0) {
					fw.write("\n");
				}
				fw.write(sepRowStr(rs, colNms, sep));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
		return rowCount;
	}

	/**
	 * RecordSet을 구분자(CSV, TSV 등)파일 형식으로 변환한다. 
	 * <br>
	 * ex) rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 : String csv = ExcelUtil.renderSep(rs, ",")
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
	 * ResultSet을 엑셀2003 형식으로 변환하여 응답객체로 전송한다.
	 * @param response
	 * @param rs
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderExcel2003(HttpServletResponse response, ResultSet rs, String fileName) {
		return renderExcel2003(response, rs, fileName, null);
	}

	/**
	 * ResultSet을 엑셀2003 형식으로 변환하여 응답객체로 전송한다.
	 * @param response
	 * @param rs
	 * @param fileName
	 * @param header
	 * @return 처리건수
	 */
	public static int renderExcel2003(HttpServletResponse response, ResultSet rs, String fileName, String[] header) {
		if (rs == null) {
			return 0;
		}
		try {
			response.reset();
			response.setContentType("application/octet-stream;");
			response.setHeader("Content-Disposition", (new StringBuilder("attachment; filename=\"")).append(new String(fileName.getBytes(), "ISO-8859-1")).append("\"").toString());
			response.setHeader("Pragma", "no-cache;");
			response.setHeader("Expires", "-1;");
			Workbook workbook = new HSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			OutputStream os = response.getOutputStream();
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				int cnt = rsmd.getColumnCount();
				String[] colNms = new String[cnt];
				for (int i = 1; i <= cnt; i++) {
					//Table의 Field 가 소문자 인것은 대문자로 변경처리
					colNms[i - 1] = rsmd.getColumnName(i).toUpperCase();
				}
				int rowCount = 0;
				if (header != null) {
					Row row = sheet.createRow(rowCount);
					CellStyle cellStyle = headerStyle(workbook);
					appendHeader(row, header, cellStyle);
					rowCount++;
				}
				CellStyle cellStyle = rowStyle(workbook);
				while (rs.next()) {
					Row row = sheet.createRow(rowCount);
					appendRow(row, rs, colNms, cellStyle);
					rowCount++;
				}
				if (colNms != null) {
					for (int i = 0; i < colNms.length; i++) {
						sheet.autoSizeColumn(i);
						sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
					}
				}
				workbook.write(os);
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
	 * ResultSet을 엑셀2003 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @return 처리건수
	 */
	public static int writeExcel2003(File file, ResultSet rs) {
		return writeExcel2003(file, rs, null);
	}

	/**
	 * ResultSet을 엑셀2003 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @param header
	 * @return 처리건수
	 */
	public static int writeExcel2003(File file, ResultSet rs, String[] header) {
		if (rs == null) {
			return 0;
		}
		try {
			Workbook workbook = new HSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				ResultSetMetaData rsmd = rs.getMetaData();
				int cnt = rsmd.getColumnCount();
				String[] colNms = new String[cnt];
				for (int i = 1; i <= cnt; i++) {
					//Table의 Field 가 소문자 인것은 대문자로 변경처리
					colNms[i - 1] = rsmd.getColumnName(i).toUpperCase();
				}
				int rowCount = 0;
				if (header != null) {
					Row row = sheet.createRow(rowCount);
					CellStyle cellStyle = headerStyle(workbook);
					appendHeader(row, header, cellStyle);
					rowCount++;
				}
				CellStyle cellStyle = rowStyle(workbook);
				while (rs.next()) {
					Row row = sheet.createRow(rowCount);
					appendRow(row, rs, colNms, cellStyle);
					rowCount++;
				}
				if (colNms != null) {
					for (int i = 0; i < colNms.length; i++) {
						sheet.autoSizeColumn(i);
						sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
					}
				}
				workbook.write(fos);
				return rowCount;
			} finally {
				Statement stmt = null;
				try {
					stmt = rs.getStatement();
				} catch (SQLException e) {
					logger.error("", e);
				}
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						logger.error("", e);
					}
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
	 * ResultSet을 엑셀2007 형식으로 변환하여 응답객체로 전송한다.
	 * @param response
	 * @param rs
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderExcel2007(HttpServletResponse response, ResultSet rs, String fileName) {
		return renderExcel2007(response, rs, fileName, null);
	}

	/**
	 * ResultSet을 엑셀2007 형식으로 변환하여 응답객체로 전송한다.
	 * @param response
	 * @param rs
	 * @param fileName
	 * @param header
	 * @return 처리건수
	 */
	public static int renderExcel2007(HttpServletResponse response, ResultSet rs, String fileName, String[] header) {
		if (rs == null) {
			return 0;
		}
		try {
			response.reset();
			response.setContentType("application/octet-stream;");
			response.setHeader("Content-Disposition", (new StringBuilder("attachment; filename=\"")).append(new String(fileName.getBytes(), "ISO-8859-1")).append("\"").toString());
			response.setHeader("Pragma", "no-cache;");
			response.setHeader("Expires", "-1;");
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			OutputStream os = response.getOutputStream();
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				int cnt = rsmd.getColumnCount();
				String[] colNms = new String[cnt];
				for (int i = 1; i <= cnt; i++) {
					//Table의 Field 가 소문자 인것은 대문자로 변경처리
					colNms[i - 1] = rsmd.getColumnName(i).toUpperCase();
				}
				int rowCount = 0;
				if (header != null) {
					Row row = sheet.createRow(rowCount);
					CellStyle cellStyle = headerStyle(workbook);
					appendHeader(row, header, cellStyle);
					rowCount++;
				}
				CellStyle cellStyle = rowStyle(workbook);
				while (rs.next()) {
					Row row = sheet.createRow(rowCount);
					appendRow(row, rs, colNms, cellStyle);
					rowCount++;
				}
				if (colNms != null) {
					for (int i = 0; i < colNms.length; i++) {
						sheet.autoSizeColumn(i);
						sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
					}
				}
				workbook.write(os);
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
	 * ResultSet을 엑셀2007 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @return 처리건수
	 */
	public static int writeExcel2007(File file, ResultSet rs) {
		return writeExcel2007(file, rs, null);
	}

	/**
	 * ResultSet을 엑셀2007 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @param header
	 * @return 처리건수
	 */
	public static int writeExcel2007(File file, ResultSet rs, String[] header) {
		if (rs == null) {
			return 0;
		}
		try {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				ResultSetMetaData rsmd = rs.getMetaData();
				int cnt = rsmd.getColumnCount();
				String[] colNms = new String[cnt];
				for (int i = 1; i <= cnt; i++) {
					//Table의 Field 가 소문자 인것은 대문자로 변경처리
					colNms[i - 1] = rsmd.getColumnName(i).toUpperCase();
				}
				int rowCount = 0;
				if (header != null) {
					Row row = sheet.createRow(rowCount);
					CellStyle cellStyle = headerStyle(workbook);
					appendHeader(row, header, cellStyle);
					rowCount++;
				}
				CellStyle cellStyle = rowStyle(workbook);
				while (rs.next()) {
					Row row = sheet.createRow(rowCount);
					appendRow(row, rs, colNms, cellStyle);
					rowCount++;
				}
				if (colNms != null) {
					for (int i = 0; i < colNms.length; i++) {
						sheet.autoSizeColumn(i);
						sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
					}
				}
				workbook.write(fos);
				return rowCount;
			} finally {
				Statement stmt = null;
				try {
					stmt = rs.getStatement();
				} catch (SQLException e) {
					logger.error("", e);
				}
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						logger.error("", e);
					}
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
	 * ResultSet을 CSV 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @return 처리건수
	 */
	public static int writeCSV(File file, ResultSet rs) {
		return writeSep(file, rs, ",");
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
	 * ResultSet을 TSV 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @return 처리건수
	 */
	public static int writeTSV(File file, ResultSet rs) {
		return writeSep(file, rs, "\t");
	}

	/**
	 * ResultSet을 구분자(CSV, TSV 등)파일 형식으로 출력한다. 
	 * <br>
	 * ex) response로 rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 => ExcelUtil.renderSep(response, rs, ",")
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
	 * ResultSet을 구분자(CSV, TSV 등)파일 형식으로 파일로 저장한다.
	 * @param file
	 * @param rs
	 * @param sep
	 * @return 처리건수
	 */
	public static int writeSep(File file, ResultSet rs, String sep) {
		if (rs == null) {
			return 0;
		}
		try {
			FileWriter fw = null;
			try {
				fw = new FileWriter(file);
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
						fw.write("\n");
					}
					fw.write(sepRowStr(rs, colNms, sep));
				}
				return rowCount;
			} finally {
				Statement stmt = null;
				try {
					stmt = rs.getStatement();
				} catch (SQLException e) {
					logger.error("", e);
				}
				if (fw != null) {
					try {
						fw.close();
					} catch (IOException e) {
						logger.error("", e);
					}
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
	 * ex) rs를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 : String csv = ExcelUtil.renderSep(rs, ",")
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
	 * List객체를 엑셀2003 형식으로 변환하여 응답객체로 전송한다.
	 * @param response
	 * @param mapList
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderExcel2003(HttpServletResponse response, List<RecordMap> mapList, String fileName) {
		return renderExcel2003(response, mapList, fileName, null);
	}

	/**
	 * List객체를 엑셀2003 형식으로 변환하여 응답객체로 전송한다.
	 * @param response
	 * @param mapList
	 * @param fileName
	 * @param header
	 * @return 처리건수
	 */
	public static int renderExcel2003(HttpServletResponse response, List<RecordMap> mapList, String fileName, String[] header) {
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
			Workbook workbook = new HSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			OutputStream os = response.getOutputStream();
			if (header != null) {
				Row row = sheet.createRow(rowCount);
				CellStyle cellStyle = headerStyle(workbook);
				appendHeader(row, header, cellStyle);
				rowCount++;
			}
			CellStyle cellStyle = rowStyle(workbook);
			for (RecordMap map : mapList) {
				Row row = sheet.createRow(rowCount);
				appendRow(row, map, cellStyle);
				rowCount++;
			}
			if (header != null) {
				for (int i = 0; i < header.length; i++) {
					sheet.autoSizeColumn(i);
					sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
				}
			}
			workbook.write(os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * List객체를 엑셀2003 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param mapList
	 * @return 처리건수
	 */
	public static int writeExcel2003(File file, List<RecordMap> mapList) {
		return writeExcel2003(file, mapList, null);
	}

	/**
	 * List객체를 엑셀2003 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param mapList
	 * @param header
	 * @return 처리건수
	 */
	public static int writeExcel2003(File file, List<RecordMap> mapList, String[] header) {
		if (mapList == null) {
			return 0;
		}
		int rowCount = 0;
		FileOutputStream fos = null;
		try {
			Workbook workbook = new HSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			fos = new FileOutputStream(file);
			if (header != null) {
				Row row = sheet.createRow(rowCount);
				CellStyle cellStyle = headerStyle(workbook);
				appendHeader(row, header, cellStyle);
				rowCount++;
			}
			CellStyle cellStyle = rowStyle(workbook);
			for (RecordMap map : mapList) {
				Row row = sheet.createRow(rowCount);
				appendRow(row, map, cellStyle);
				rowCount++;
			}
			if (header != null) {
				for (int i = 0; i < header.length; i++) {
					sheet.autoSizeColumn(i);
					sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
				}
			}
			workbook.write(fos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
		return rowCount;
	}

	/**
	 * List객체를 엑셀2007 형식으로 변환하여 응답객체로 전송한다. 
	 * @param response
	 * @param mapList
	 * @param fileName
	 * @return 처리건수
	 */
	public static int renderExcel2007(HttpServletResponse response, List<RecordMap> mapList, String fileName) {
		return renderExcel2007(response, mapList, fileName, null);
	}

	/**
	 * List객체를 엑셀2007 형식으로 변환하여 응답객체로 전송한다. 
	 * @param response
	 * @param mapList
	 * @param fileName
	 * @param header
	 * @return 처리건수
	 */
	public static int renderExcel2007(HttpServletResponse response, List<RecordMap> mapList, String fileName, String[] header) {
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
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			OutputStream os = response.getOutputStream();
			if (header != null) {
				Row row = sheet.createRow(rowCount);
				CellStyle cellStyle = headerStyle(workbook);
				appendHeader(row, header, cellStyle);
				rowCount++;
			}
			CellStyle cellStyle = rowStyle(workbook);
			for (RecordMap map : mapList) {
				Row row = sheet.createRow(rowCount);
				appendRow(row, map, cellStyle);
				rowCount++;
			}
			if (header != null) {
				for (int i = 0; i < header.length; i++) {
					sheet.autoSizeColumn(i);
					sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
				}
			}
			workbook.write(os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * List객체를 엑셀2007 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param mapList
	 * @return 처리건수
	 */
	public static int writeExcel2007(File file, List<RecordMap> mapList) {
		return writeExcel2007(file, mapList, null);
	}

	/**
	 * List객체를 엑셀2007 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param mapList
	 * @param header
	 * @return 처리건수
	 */
	public static int writeExcel2007(File file, List<RecordMap> mapList, String[] header) {
		if (mapList == null) {
			return 0;
		}
		int rowCount = 0;
		FileOutputStream fos = null;
		try {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			fos = new FileOutputStream(file);
			if (header != null) {
				Row row = sheet.createRow(rowCount);
				CellStyle cellStyle = headerStyle(workbook);
				appendHeader(row, header, cellStyle);
				rowCount++;
			}
			CellStyle cellStyle = rowStyle(workbook);
			for (RecordMap map : mapList) {
				Row row = sheet.createRow(rowCount);
				appendRow(row, map, cellStyle);
				rowCount++;
			}
			if (header != null) {
				for (int i = 0; i < header.length; i++) {
					sheet.autoSizeColumn(i);
					sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
				}
			}
			workbook.write(fos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
		return rowCount;
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
	 * List객체를 CSV 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param mapList
	 * @return 처리건수
	 */
	public static int writeCSV(File file, List<RecordMap> mapList) {
		return writeSep(file, mapList, ",");
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
	 * List객체를 TSV 형식으로 변환하여 파일로 저장한다.
	 * @param file
	 * @param mapList
	 * @return 처리건수
	 */
	public static int writeTSV(File file, List<RecordMap> mapList) {
		return writeSep(file, mapList, "\t");
	}

	/**
	 * List객체를 구분자(CSV, TSV 등)파일 형식으로 출력한다. 
	 * <br>
	 * ex) response로 mapList를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 출력하는 경우 => ExcelUtil.renderSep(response, mapList, ",")
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
	 * List객체를 구분자(CSV, TSV 등)파일 형식으로 파일로 저장한다.
	 * @param file
	 * @param mapList
	 * @param sep
	 * @return 처리건수
	 */
	public static int writeSep(File file, List<RecordMap> mapList, String sep) {
		if (mapList == null) {
			return 0;
		}
		int rowCount = 0;
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			for (RecordMap map : mapList) {
				if (rowCount++ > 0) {
					fw.write("\n");
				}
				fw.write(sepRowStr(map, sep));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
		return rowCount;
	}

	/**
	 * List객체를 구분자(CSV, TSV 등)파일 형식으로 변환한다. 
	 * <br>
	 * ex1) mapList를 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 : String csv = ExcelUtil.renderSep(mapList, ",")
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
	 * Map객체를 구분자(CSV, TSV 등)파일 형식으로 변환한다. 
	 * <br>
	 * ex) map을 열구분자 콤마(,) 인 구분자(CSV, TSV 등)파일 형식으로 변환하는 경우 : String csv = ExcelUtil.renderSep(map, ",")
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

	private static void appendHeader(Row row, String[] header, CellStyle cellStyle) {
		if (header == null) {
			return;
		}
		for (int c = 0; c < header.length; c++) {
			Cell cell = row.createCell(c);
			cell.setCellType(Cell.CELL_TYPE_STRING);
			cell.setCellValue(header[c]);
			cell.setCellStyle(cellStyle);
		}
	}

	private static void appendRow(Row row, RecordSet rs, String[] colNms, CellStyle cellStyle) {
		if (colNms == null) {
			return;
		}
		for (int c = 0; c < colNms.length; c++) {
			Cell cell = row.createCell(c);
			Object value = rs.get(colNms[c]);
			if (value == null) {
				cell.setCellType(Cell.CELL_TYPE_STRING);
				cell.setCellValue("");
			} else {
				if (value instanceof Number) {
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					cell.setCellValue(Double.valueOf(value.toString()));
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellValue(value.toString());
				}
			}
			cell.setCellStyle(cellStyle);
		}
	}

	private static void appendRow(Row row, ResultSet rs, String[] colNms, CellStyle cellStyle) {
		if (colNms == null) {
			return;
		}
		try {
			for (int c = 0; c < colNms.length; c++) {
				Cell cell = row.createCell(c);
				Object value = rs.getObject(colNms[c]);
				if (value == null) {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellValue("");
				} else {
					if (value instanceof Number) {
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						cell.setCellValue(Double.valueOf(value.toString()));
					} else {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(value.toString());
					}
				}
				cell.setCellStyle(cellStyle);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static void appendRow(Row row, RecordMap map, CellStyle cellStyle) {
		int c = 0;
		for (Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			Cell cell = row.createCell(c++);
			if (value == null) {
				cell.setCellType(Cell.CELL_TYPE_STRING);
				cell.setCellValue("");
			} else {
				if (value instanceof Number) {
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					cell.setCellValue(Double.valueOf(value.toString()));
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellValue(value.toString());
				}
			}
			cell.setCellStyle(cellStyle);
		}
	}

	private static List<LinkedHashMap<String, String>> parseExcel2003(InputStream is) {
		POIFSFileSystem poiFileSystem;
		HSSFSheet sheet;
		try {
			poiFileSystem = new POIFSFileSystem(is);
			HSSFWorkbook workbook = new HSSFWorkbook(poiFileSystem);
			sheet = workbook.getSheetAt(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return parseSheet(sheet);
	}

	private static List<LinkedHashMap<String, String>> parseExcel2003(InputStream is, String password) {
		POIFSFileSystem poiFileSystem;
		HSSFSheet sheet;
		try {
			poiFileSystem = new POIFSFileSystem(is);
			Biff8EncryptionKey.setCurrentUserPassword(password);
			HSSFWorkbook workbook = new HSSFWorkbook(poiFileSystem);
			Biff8EncryptionKey.setCurrentUserPassword(null);
			sheet = workbook.getSheetAt(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return parseSheet(sheet);
	}

	private static List<LinkedHashMap<String, String>> parseExcel2007(InputStream is) {
		XSSFWorkbook workbook;
		try {
			workbook = new XSSFWorkbook(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return parseSheet(workbook.getSheetAt(0));
	}

	private static List<LinkedHashMap<String, String>> parseExcel2007(InputStream is, String password) {
		XSSFWorkbook workbook;
		try {
			POIFSFileSystem fs = new POIFSFileSystem(is);
			EncryptionInfo info = new EncryptionInfo(fs);
			Decryptor d = new Decryptor(info);
			d.verifyPassword(password);
			workbook = new XSSFWorkbook(d.getDataStream(fs));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return parseSheet(workbook.getSheetAt(0));
	}

	private static List<LinkedHashMap<String, String>> parseCSV(InputStream is) {
		return parseSep(is, ",");
	}

	private static List<LinkedHashMap<String, String>> parseTSV(InputStream is) {
		return parseSep(is, "\t");
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

	/**
	 * 엑셀 시트의 데이터 파싱하여 맵의 리스트로 리턴
	 */
	private static List<LinkedHashMap<String, String>> parseSheet(Sheet sheet) {
		List<LinkedHashMap<String, String>> mapList = new ArrayList<LinkedHashMap<String, String>>();
		int rowCount = sheet.getPhysicalNumberOfRows();
		int colCount = sheet.getRow(0).getPhysicalNumberOfCells();
		for (int i = 0; i < rowCount; i++) {
			Row row = sheet.getRow(i);
			LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
			for (int j = 0; j < colCount; j++) {
				Cell cell = row.getCell(j);
				String item = "";
				if (cell == null) {
					item = "";
				} else {
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_ERROR:
						throw new RuntimeException("EXCEL에 수식 에러가 포함되어 있어 분석에 실패하였습니다.");
					case Cell.CELL_TYPE_FORMULA:
						throw new RuntimeException("EXCEL에 수식이 포함되어 있어 분석에 실패하였습니다.");
					case Cell.CELL_TYPE_NUMERIC:
						cell.setCellType(Cell.CELL_TYPE_STRING);
						item = cell.getStringCellValue();
						break;
					case Cell.CELL_TYPE_STRING:
						item = cell.getStringCellValue();
						break;
					}
				}
				map.put(String.valueOf(j), item);
			}
			mapList.add(map);
		}
		return mapList;
	}

	/** 
	 * 헤더 셀 스타일 리턴
	 */
	private static CellStyle headerStyle(Workbook workbook) {
		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) 11);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setFontName("돋움");
		font.setColor(HSSFColor.BLACK.index);
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFont(font);
		cellStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		cellStyle.setBottomBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.setLeftBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		cellStyle.setRightBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		cellStyle.setTopBorderColor(HSSFColor.BLACK.index);
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		cellStyle.setWrapText(true);
		return cellStyle;
	}

	/** 
	 * 로우 셀 스타일 리턴
	 */
	private static CellStyle rowStyle(Workbook workbook) {
		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) 11);
		font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
		font.setFontName("돋움");
		font.setColor(HSSFColor.BLACK.index);
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFont(font);
		cellStyle.setFillForegroundColor(HSSFColor.WHITE.index);
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		cellStyle.setBottomBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.setLeftBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		cellStyle.setRightBorderColor(HSSFColor.BLACK.index);
		cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		cellStyle.setTopBorderColor(HSSFColor.BLACK.index);
		cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cellStyle.setWrapText(true);
		return cellStyle;
	}
}