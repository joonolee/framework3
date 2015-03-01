/*
 * @(#)CreateMSSqlXMLFile.java
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
import java.util.ResourceBundle;

public class DaogenForMSSQL {
	private static final String _filePath = "xml";
	private static String _jdbcDriver = "";
	private static String _jdbcUrl = "";
	private static String _jdbcUid = "";
	private static String _jdbcPwd = "";

	// 테이블 목록
	private static List<String> _tbList = Arrays.asList();

	public static void main(String[] args) {
		// db properties setting
		ResourceBundle bundle = ResourceBundle.getBundle("db");
		_jdbcDriver = bundle.getString("jdbc.driver").trim();
		_jdbcUrl = bundle.getString("jdbc.url").trim();
		_jdbcUid = bundle.getString("jdbc.uid").trim();
		_jdbcPwd = bundle.getString("jdbc.pwd").trim();
		// table list
		if (_tbList.size() == 0) {
			_tbList = Arrays.asList(args);
		}
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
			conn = DriverManager.getConnection(_jdbcUrl, _jdbcUid, _jdbcPwd);
			System.out.println("JDBC Driver 로딩...");
			System.out.println("=== 선택된 테이블 목록에서 파일 생성 START ===");
			stmt = conn.createStatement();
			for (String tbName : _tbList) {
				ResultSet rs = null;
				try {
					rs = stmt.executeQuery("select top 1 * from " + tbName);
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
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
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
			conn = DriverManager.getConnection(_jdbcUrl, _jdbcUid, _jdbcPwd);
			System.out.println("JDBC Driver 로딩...");
			System.out.println("=== 전체 테이블 목록에서 파일 생성 START ===");
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select table_name from information_schema.tables");
			while (rs.next()) {
				Statement stmt2 = null;
				ResultSet rs2 = null;
				try {
					String tbName = rs.getString(1);
					stmt2 = conn.createStatement();
					rs2 = stmt2.executeQuery("select top 1 * from " + tbName);
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
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
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
			for (int c = 1; c <= rsmd.getColumnCount(); c++) {
				StringBuffer buf = new StringBuffer();
				buf.append("    <column name=\"");
				buf.append(rsmd.getColumnName(c));
				buf.append("\" type=\"");
				buf.append(_getJavaType(rsmd.getColumnType(c), rsmd.getPrecision(c), rsmd.getScale(c)));
				buf.append("\" dbType=\"");
				buf.append(_getDBType(rsmd.getColumnType(c), rsmd.getPrecision(c), rsmd.getScale(c)));
				buf.append("\" desc=\"\" notnull=\"");
				buf.append((rsmd.isNullable(c) == 0 ? "true" : "false") + "\"");
				if (rsmd.getColumnName(c).equals("ENTERID") || rsmd.getColumnName(c).equals("ENTERNAME") || rsmd.getColumnName(c).equals("ENTERPGM")) {
					buf.append(" update=\"none\"");
				}
				// 입력일, 수정일에 대한 별도 처리
				if (rsmd.getColumnName(c).equals("ENTERDATE")) {
					buf.append(" insert=\"getdate()\" update=\"none\"");
				}
				if (rsmd.getColumnName(c).equals("UPDATEDATE")) {
					buf.append(" insert=\"none\" update=\"getdate()\"");
				}
				if (pkList.contains(rsmd.getColumnName(c))) {
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
		String cmd = "";
		if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
			cmd = "run.bat " + tbName;
		} else {
			cmd = "run.sh " + tbName;
		}
		Process p = Runtime.getRuntime().exec(cmd);
		p.waitFor();
	}

	private static List<String> _getPkList(String tableName, Connection conn) throws Throwable {
		List<String> pkList = new LinkedList<String>();
		ResultSet rs = null;
		Statement stmt = null;
		try {
			StringBuffer query = new StringBuffer();
			query.append("select col.column_name  ");
			query.append("from sysobjects cons ");
			query.append("    inner join information_schema.key_column_usage col on cons.name = col.constraint_name ");
			query.append("where cons.xtype = 'PK' ");
			query.append("    and col.table_name = '" + tableName + "' ");
			query.append("order by col.ordinal_position ");
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
			return "varchar(" + len + ")";
		case Types.NVARCHAR:
			return "nvarchar(" + len + ")";
		case Types.CHAR:
			return "char(" + len + ")";
		case Types.NCHAR:
			return "nchar(" + len + ")";
		case Types.DATE:
			return "date";
		default:
			return String.valueOf(type);
		}
	}
}