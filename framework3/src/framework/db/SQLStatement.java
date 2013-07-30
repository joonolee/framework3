/* 
 * @(#)SQLStatement.java
 * Statement 를 이용하기 위한 객체
 */
package framework.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLStatement extends DBStatement {
	private String _sql;
	private ConnectionManager _connMgr = null;
	private Statement _stmt = null;
	private RecordSet _rs = null;
	private int _upCnt = 0;
	private Object _caller = null;

	public static SQLStatement create(String sql, ConnectionManager connMgr, Object caller) {
		return new SQLStatement(sql, connMgr, caller);
	}

	private SQLStatement(String sql, ConnectionManager connMgr, Object caller) {
		_sql = sql;
		_connMgr = connMgr;
		_caller = caller;
	}

	protected Statement getStatement() {
		try {
			if (_stmt == null) {
				_stmt = _connMgr.getRawConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				_stmt.setFetchSize(100);
			}
		} catch (SQLException e) {
			getLogger().error("getStatement Error!");
			throw new RuntimeException(e);
		}
		return _stmt;
	}

	@Override
	public void close() {
		if (_stmt != null) {
			try {
				_stmt.close();
			} catch (SQLException e) {
				getLogger().error("close Error!");
				throw new RuntimeException(e);
			}
		}
	}

	public RecordSet executeQuery(int currPage, int pageSize) {
		if (getSQL() == null) {
			getLogger().error("Query is Null");
			return null;
		}
		try {
			Statement stmt = getStatement();
			if (getLogger().isDebugEnabled()) {
				StringBuilder log = new StringBuilder();
				log.append("@Sql Start (STATEMENT) FetchSize : " + stmt.getFetchSize() + " Caller : " + _caller.getClass().getName() + "\n");
				log.append("@Sql Command => \n" + getSQL());
				getLogger().debug(log.toString());
			}
			_rs = new RecordSet(stmt.executeQuery(getSQL()), currPage, pageSize);
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("@Sql End (STATEMENT)");
			}
		} catch (SQLException e) {
			getLogger().error("executeQuery Error!");
			throw new RuntimeException(e.getMessage() + "\nSQL : " + getSQL());
		}
		return _rs;
	}

	public RecordSet executeQuery() {
		return executeQuery(0, 0);
	}

	public RecordSet executeQuery(String sql) {
		setSQL(sql);
		return executeQuery(0, 0);
	}

	public RecordSet executeQuery(String sql, int currPage, int pageSize) {
		setSQL(sql);
		return executeQuery(currPage, pageSize);
	}

	public int executeUpdate() {
		if (getSQL() == null) {
			getLogger().error("Query is Null");
			return 0;
		}
		try {
			Statement stmt = getStatement();
			if (getLogger().isDebugEnabled()) {
				StringBuilder log = new StringBuilder();
				log.append("@Sql Start (STATEMENT) FetchSize : " + stmt.getFetchSize() + " Caller : " + _caller.getClass().getName() + "\n");
				log.append("@Sql Command => \n" + getSQL());
				getLogger().debug(log.toString());
			}
			_upCnt = stmt.executeUpdate(getSQL());
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("@Sql End (STATEMENT)");
			}
		} catch (SQLException e) {
			getLogger().error("executeUpdate Error!");
			throw new RuntimeException(e.getMessage() + "\nSQL : " + getSQL());
		}
		return _upCnt;
	}

	public int executeUpdate(String sql) {
		setSQL(sql);
		return executeUpdate();
	}

	public RecordSet getRecordSet() {
		return _rs;
	}

	public String getSQL() {
		return _sql;
	}

	public int getUpdateCount() {
		return _upCnt;
	}

	public void setSQL(String newSql) {
		_sql = newSql;
	}

	public String toString() {
		return "SQL : " + getSQL();
	}
}