package framework.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Statement 를 이용하기 위한 객체
 */
public class Statement extends AbstractStatement {
	private String sql = null;
	private DB db = null;
	private java.sql.Statement stmt = null;
	private RecordSet rs = null;
	private int upCnt = 0;
	private Object caller = null;

	public static Statement create(String sql, DB db, Object caller) {
		return new Statement(sql, db, caller);
	}

	private Statement(String sql, DB db, Object caller) {
		this.sql = sql;
		this.db = db;
		this.caller = caller;
	}

	protected java.sql.Statement getStatement() {
		try {
			if (stmt == null) {
				stmt = db.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				stmt.setFetchSize(100);
			}
		} catch (SQLException e) {
			logger.error("", e);
			throw new RuntimeException(e);
		}
		return stmt;
	}

	@Override
	public void close() {
		if (stmt != null) {
			try {
				stmt.close();
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
				log.append("@Sql Start (STATEMENT) FetchSize : " + stmt.getFetchSize() + " Caller : " + caller.getClass().getName() + "\n");
				log.append("@Sql Command : \n" + getSQL());
				logger.debug(log.toString());
			}
			rs = new RecordSet(stmt.executeQuery(getSQL()), currPage, pageSize);
			if (logger.isDebugEnabled()) {
				logger.debug("@Sql End (STATEMENT)");
			}
		} catch (SQLException e) {
			logger.error("", e);
			throw new RuntimeException(e.getMessage() + "\nSQL : " + getSQL());
		}
		return rs;
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
				log.append("@Sql Start (STATEMENT) FetchSize : " + stmt.getFetchSize() + " Caller : " + caller.getClass().getName() + "\n");
				log.append("@Sql Command : \n" + getSQL());
				logger.debug(log.toString());
			}
			upCnt = stmt.executeUpdate(getSQL());
			if (logger.isDebugEnabled()) {
				logger.debug("@Sql End (STATEMENT)");
			}
		} catch (SQLException e) {
			logger.error("", e);
			throw new RuntimeException(e.getMessage() + "\nSQL : " + getSQL());
		}
		return upCnt;
	}

	public int executeUpdate(String sql) {
		setSQL(sql);
		return executeUpdate();
	}

	public RecordSet getRecordSet() {
		return rs;
	}

	public String getSQL() {
		return sql;
	}

	public int getUpdateCount() {
		return upCnt;
	}

	public void setSQL(String newSql) {
		sql = newSql;
	}

	@Override
	public String toString() {
		return "SQL : " + getSQL();
	}
}