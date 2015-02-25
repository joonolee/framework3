/*
 * @(#)CreateOracleXMLFile.java
 * DAO, VO 생성에 필요한 메타정보 xml 파일을 추출
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CreateOracleXMLFile {
	private static final String _filePath = "xml";
	private static final String _jdbcDriver = "oracle.jdbc.driver.OracleDriver";
	private static final String _jdbcUrl = "jdbc:oracle:thin:@ip:port:sid";
	private static final String _jdbcUid = "";
	private static final String _jdbcPw = "";

	// 테이블 목록
	private static final List<String> _tbList = Arrays.asList();

	public static void main(String[] args) {
		if (_tbList.size() > 0) {
			_selTables();
		} else {
			_allTables();
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Private

	private static void _selTables() {
		Connection conn = null;
		Statement stmt = null;
		try {
			DriverManager.registerDriver((Driver) Class.forName(_jdbcDriver).newInstance());
			conn = DriverManager.getConnection(_jdbcUrl, _jdbcUid, _jdbcPw);
			System.out.println("JDBC Driver 로딩...");
			System.out.println("=== 선택된 테이블 목록에서 파일 생성 START ===");
			stmt = conn.createStatement();
			for (String tbName : _tbList) {
				ResultSet rs = null;
				try {
					rs = stmt.executeQuery("select * from " + tbName + " where rownum = 1");
					ResultSetMetaData rsmd = rs.getMetaData();
					_writeFile(rsmd, tbName, conn);
				} finally {
					if (rs != null) {
						rs.close();
					}
				}
			}
			System.out.println("=== 선택된 테이블 목록에서 파일 생성 END ===");
		} catch (Throwable e) {
			System.err.println("에러발생");
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("에러발생");
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					System.err.println("에러발생");
				}
			}
		}
	}

	private static void _allTables() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			DriverManager.registerDriver((Driver) Class.forName(_jdbcDriver).newInstance());
			conn = DriverManager.getConnection(_jdbcUrl, _jdbcUid, _jdbcPw);
			System.out.println("JDBC Driver 로딩...");
			System.out.println("=== 전체 테이블 목록에서 파일 생성 START ===");
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select table_name from user_tables order by 1");
			while (rs.next()) {
				Statement stmt2 = null;
				ResultSet rs2 = null;
				try {
					String tbName = rs.getString(1);
					stmt2 = conn.createStatement();
					rs2 = stmt2.executeQuery("select * from " + tbName + " where rownum = 1");
					ResultSetMetaData rsmd = rs2.getMetaData();
					_writeFile(rsmd, tbName, conn);
				} finally {
					if (rs2 != null) {
						rs2.close();
					}
					if (stmt2 != null) {
						stmt2.close();
					}
				}
			}
			System.out.println("=== 전체 테이블 목록에서 파일 생성 END ===");
		} catch (Throwable e) {
			System.err.println("에러발생");
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					System.err.println("에러발생");
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("에러발생");
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					System.err.println("에러발생");
				}
			}
		}
	}

	private static void _writeFile(ResultSetMetaData rsmd, String tbName, Connection conn) throws Throwable {
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			List<String> pkList = _getPkList(tbName, conn);
			File dir = new File(_filePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(_filePath, tbName + ".xml");
			if (file.exists()) {
				file.delete();
			}
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			bw.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
			bw.write("<table name=\"" + tbName + "\"  schema=\"" + _jdbcUid + "\" class=\"" + tbName + "\">\n");
			bw.write("  <description></description>\n");
			bw.write("  <columns>\n");
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				StringBuilder buf = new StringBuilder();
				buf.append("    <column name=\"");
				buf.append(rsmd.getColumnName(i));
				buf.append("\" type=\"");
				buf.append(_getJavaType(rsmd.getColumnType(i), rsmd.getPrecision(i), rsmd.getScale(i)));
				buf.append("\" dbType=\"");
				buf.append(_getDBType(rsmd.getColumnType(i), rsmd.getPrecision(i), rsmd.getScale(i)));
				buf.append("\" desc=\"\" notnull=\"");
				buf.append((rsmd.isNullable(i) == 0 ? "true" : "false") + "\"");
				if (rsmd.getColumnName(i).equals("ENTERID") || rsmd.getColumnName(i).equals("ENTERNAME") || rsmd.getColumnName(i).equals("ENTERPGM")) {
					buf.append(" update=\"none\"");
				}
				// 입력일, 수정일에 대한 별도 처리
				if (rsmd.getColumnName(i).equals("ENTERDATE")) {
					buf.append(" insert=\"sysdate\" update=\"none\"");
				}
				if (rsmd.getColumnName(i).equals("UPDATEDATE")) {
					buf.append(" insert=\"none\" update=\"sysdate\"");
				}
				if (pkList.contains(rsmd.getColumnName(i))) {
					buf.append(" primarykey=\"true\"");
				}
				buf.append(" />\n");
				bw.write(buf.toString());
			}
			bw.write("  </columns>\n");
			bw.write("</table>");
			bw.flush();
		} finally {
			if (bw != null) {
				bw.close();
			}
			if (fw != null) {
				fw.close();
			}
		}
		System.out.println(_filePath + "/" + tbName + ".xml created! ");
	}

	private static List<String> _getPkList(String tableName, Connection conn) throws Throwable {
		List<String> pkList = new LinkedList<String>();
		ResultSet rs = null;
		Statement stmt = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("select ");
			query.append("    b.column_name ");
			query.append("from user_constraints a ");
			query.append("    inner join user_cons_columns b on a.constraint_name = b.constraint_name ");
			query.append("where a.constraint_type = 'P' ");
			query.append("    and b.table_name = '" + tableName + "' ");
			query.append("order by b.position");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while (rs.next()) {
				pkList.add(rs.getString("COLUMN_NAME"));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return pkList;
	}

	private static String _getJavaType(int type, int len, int s) {
		switch (type) {
		case Types.INTEGER:
		case Types.SMALLINT:
			return "Integer";
		case Types.DECIMAL:
		case Types.NUMERIC:
			if (s == 0 && len < 8) {
				return "Integer";
			} else if (s == 0) {
				return "Long";
			} else {
				return "BigDecimal";
			}
		case Types.DATE:
			return "String";
		default:
			return "String";
		}
	}

	private static String _getDBType(int type, int len, int s) {
		switch (type) {
		case Types.INTEGER:
		case Types.SMALLINT:
		case Types.DECIMAL:
		case Types.NUMERIC:
			return "number(" + len + (s == 0 ? ")" : "." + s + ")");
		case Types.VARCHAR:
			return "varchar2(" + len + ")";
		case Types.CHAR:
			return "char(" + len + ")";
		case Types.DATE:
			return "date";
		default:
			return String.valueOf(type);
		}
	}
}