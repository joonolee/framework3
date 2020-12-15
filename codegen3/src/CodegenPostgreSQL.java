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

public class CodegenPostgreSQL {
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
					rs = stmt.executeQuery("select * from " + tbName + " limit 1");
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
			rs = stmt.executeQuery("select table_name from information_schema.tables where table_schema = 'public' order by 1");
			while (rs.next()) {
				Statement stmt2 = null;
				ResultSet rs2 = null;
				try {
					String tbName = rs.getString(1);
					stmt2 = conn.createStatement();
					rs2 = stmt2.executeQuery("select * from " + tbName + " limit 1");
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
			File file = new File(_filePath, tbName.toUpperCase() + ".xml");
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
				StringBuffer buf = new StringBuffer();
				String columnName = rsmd.getColumnName(i);
				buf.append("    <column name=\"");
				buf.append(columnName);
				buf.append("\" type=\"");
				buf.append(_getJavaType(rsmd.getColumnType(i), rsmd.getPrecision(i), rsmd.getScale(i)));
				buf.append("\" dbType=\"");
				buf.append(_getDBType(rsmd.getColumnType(i)));
				buf.append("\" dbPrecision=\"");
				buf.append(rsmd.getPrecision(i));
				buf.append("\" dbScale=\"");
				buf.append(rsmd.getScale(i));
				buf.append("\" notnull=\"");
				buf.append((rsmd.isNullable(i) == 0 ? "true" : "false") + "\"");
				if (columnName.equals("enterid") || columnName.equals("entername") || columnName.equals("enterpgm")
					|| columnName.equals("reg_id") || columnName.equals("reg_nm") || columnName.equals("reg_pgm")) {
					buf.append(" update=\"none\"");
				}
				if (columnName.equals("updateid") || columnName.equals("updatename") || columnName.equals("updatepgm")
					|| columnName.equals("upd_id") || columnName.equals("upd_nm") || columnName.equals("upd_pgm")) {
					buf.append(" insert=\"none\"");
				}
				// 입력일, 수정일에 대한 별도 처리
				if (columnName.equals("enterdate") || columnName.equals("reg_dttm")) {
					buf.append(" insert=\"now()\" update=\"none\"");
				}
				if (columnName.equals("updatedate") || columnName.equals("upd_dttm")) {
					buf.append(" insert=\"none\" update=\"now()\"");
				}
				if (pkList.contains(columnName)) {
					buf.append(" primarykey=\"true\"");
				}
				if (rsmd.isAutoIncrement(i)) {
					buf.append(" auto_increment=\"true\"");
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
		System.out.println(tbName.toUpperCase() + " created!");
		String cmd = "";
		if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
			cmd = "run.bat " + tbName.toUpperCase();
		} else {
			cmd = "./run.sh " + tbName.toUpperCase();
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
			query.append("select col.column_name ");
			query.append("from information_schema.table_constraints cons ");
			query.append("    inner join information_schema.key_column_usage col on cons.constraint_name = col.constraint_name ");
			query.append("where cons.constraint_type = 'PRIMARY KEY' ");
			query.append("    and col.table_name = '" + tableName + "' ");
			query.append("order by col.ordinal_position ");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query.toString());
			while (rs.next()) {
				pkList.add(rs.getString("column_name"));
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
		case Types.BIGINT:
			if (s == 0 && len < 8) {
				return "Integer";
			} else if (s == 0) {
				return "Long";
			} else {
				return "BigDecimal";
			}
		case Types.TIME:
		case Types.TIMESTAMP:
		case Types.DATE:
			return "Date";
		default:
			return "String";
		}
	}

	private static String _getDBType(int type) {
		switch (type) {
		case Types.INTEGER:
		case Types.SMALLINT:
		case Types.DECIMAL:
		case Types.NUMERIC:
		case Types.BIGINT:
			return "number";
		case Types.VARCHAR:
			return "varchar";
		case Types.CHAR:
			return "char";
		case Types.TIME:
		case Types.TIMESTAMP:
		case Types.DATE:
			return "date";
		default:
			return "varchar";
		}
	}
}