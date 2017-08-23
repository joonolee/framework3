package framework.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
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
	public static List<RecordMap> parse(FileItem fileItem) {
		String ext = FileUtil.getFileExtension(fileItem.getName());
		InputStream is = null;
		try {
			is = fileItem.getInputStream();
			if ("xls".equalsIgnoreCase(ext)) {
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
	public static List<RecordMap> parse(FileItem fileItem, String password) {
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
	public static List<RecordMap> parse(File file) {
		FileInputStream fis = null;
		try {
			String ext = FileUtil.getFileExtension(file);
			fis = new FileInputStream(file);
			if ("xls".equalsIgnoreCase(ext)) {
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
	public static List<RecordMap> parse(File file, String password) {
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
		if (response == null || rs == null || fileName == null) {
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
		if (file == null || rs == null) {
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
		if (response == null || rs == null || fileName == null) {
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
		if (file == null || rs == null) {
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
		if (response == null || rs == null || fileName == null) {
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
					colNms[i - 1] = rsmd.getColumnName(i).toLowerCase();
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
		if (file == null || rs == null) {
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
					colNms[i - 1] = rsmd.getColumnName(i).toLowerCase();
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
		if (response == null || rs == null || fileName == null) {
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
					colNms[i - 1] = rsmd.getColumnName(i).toLowerCase();
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
		if (file == null || rs == null) {
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
					colNms[i - 1] = rsmd.getColumnName(i).toLowerCase();
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
		if (response == null || mapList == null || fileName == null) {
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
		if (file == null || mapList == null) {
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
		if (response == null || mapList == null || fileName == null) {
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
		if (file == null || mapList == null) {
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

	////////////////////////////////////////////////////////////////////////////////////////// Private 메소드

	private static void appendHeader(Row row, String[] header, CellStyle cellStyle) {
		if (row == null || header == null || cellStyle == null) {
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
		if (row == null || rs == null || colNms == null || cellStyle == null) {
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
		if (row == null || rs == null || colNms == null || cellStyle == null) {
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
		if (row == null || map == null || cellStyle == null) {
			return;
		}
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

	private static List<RecordMap> parseExcel2003(InputStream is) {
		HSSFWorkbook workbook;
		try {
			workbook = new HSSFWorkbook(new POIFSFileSystem(is));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return parseSheet(workbook.getSheetAt(0));
	}

	private static List<RecordMap> parseExcel2003(InputStream is, String password) {
		HSSFWorkbook workbook;
		try {
			Biff8EncryptionKey.setCurrentUserPassword(password);
			workbook = new HSSFWorkbook(new POIFSFileSystem(is));
			Biff8EncryptionKey.setCurrentUserPassword(null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return parseSheet(workbook.getSheetAt(0));
	}

	private static List<RecordMap> parseExcel2007(InputStream is) {
		XSSFWorkbook workbook;
		try {
			workbook = new XSSFWorkbook(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return parseSheet(workbook.getSheetAt(0));
	}

	private static List<RecordMap> parseExcel2007(InputStream is, String password) {
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

	/**
	 * 엑셀 시트의 데이터 파싱하여 맵의 리스트로 리턴
	 */
	private static List<RecordMap> parseSheet(Sheet sheet) {
		List<RecordMap> mapList = new ArrayList<RecordMap>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		int rowCount = sheet.getPhysicalNumberOfRows();
		int colCount = sheet.getRow(0).getPhysicalNumberOfCells();
		for (int i = 0; i < rowCount; i++) {
			Row row = sheet.getRow(i);
			if (row != null) {
				RecordMap map = new RecordMap();
				for (int j = 0; j < colCount; j++) {
					Cell cell = row.getCell(j);
					String item = "";
					if (cell != null) {
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_BOOLEAN:
						case Cell.CELL_TYPE_FORMULA:
						case Cell.CELL_TYPE_STRING:
							cell.setCellType(Cell.CELL_TYPE_STRING);
							item = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_NUMERIC:
							if (DateUtil.isCellDateFormatted(cell)) {
								Date date = cell.getDateCellValue();
								item = dateFormat.format(date);
							} else {
								cell.setCellType(Cell.CELL_TYPE_STRING);
								item = cell.getStringCellValue();
							}
							break;
						}
					}
					map.put(String.valueOf(j), item);
				}
				mapList.add(map);
			}
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