package framework.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Statement 를 이용하기 위한 객체
 */
public class Statement extends AbstractStatement {
	private String _sql;
	private DB _db = null;
	private java.sql.Statement _stmt = null;
	private RecordSet _rs = null;
	private int _upCnt = 0;
	private Object _caller = null;

	public static Statement create(String sql, DB db, Object caller) {
		return new Statement(sql, db, caller);
	}

	private Statement(String sql, DB db, Object caller) {
		_sql = sql;
		_db = db;
		_caller = caller;
	}

	protected java.sql.Statement getStatement() {
		try {
			if (_stmt == null) {
				_stmt = _db.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				_stmt.setFetchSize(100);
			}
		} catch (SQLException e) {
			logger.error("", e);
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
				logger.error("", e);
				throw new RuntimeException(e);
			}
		}
	}

	public RecordSet executeQuery(int currPage, int pageSize) {
		if (getSQL() == null) {
			logger.error("Query is Null");
			return null;
		}
		try {
			java.sql.Statement stmt = getStatement();
			if (logger.isDebugEnabled()) {
				StringBuilder log = new StringBuilder();
				log.append("@Sql Start (STATEMENT) FetchSize : " + stmt.getFetchSize() + " Caller : " + _caller.getClass().getName() + "\n");
				log.append("@Sql Command : \n" + getSQL());
				logger.debug(log.toString());
			}
			_rs = new RecordSet(stmt.executeQuery(getSQL()), currPage, pageSize);
			if (logger.isDebugEnabled()) {
				logger.debug("@Sql End (STATEMENT)");
			}
		} catch (SQLException e) {
			logger.error("", e);
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
			logger.error("Query is Null");
			return 0;
		}
		try {
			java.sql.Statement stmt = getStatement();
			if (logger.isDebugEnabled()) {
				StringBuilder log = new StringBuilder();
				log.append("@Sql Start (STATEMENT) FetchSize : " + stmt.getFetchSize() + " Caller : " + _caller.getClass().getName() + "\n");
				log.append("@Sql Command : \n" + getSQL());
				logger.debug(log.toString());
			}
			_upCnt = stmt.executeUpdate(getSQL());
			if (logger.isDebugEnabled()) {
				logger.debug("@Sql End (STATEMENT)");
			}
		} catch (SQLException e) {
			logger.error("", e);
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

	@Override
	public String toString() {
		return "SQL : " + getSQL();
	}
}