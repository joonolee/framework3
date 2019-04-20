package framework.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Statement의 Batch 처리를 이용하기 위한 객체
 */
public final class BatchStatement extends AbstractStatement {
	private final List<String> sqlList = new ArrayList<String>();
	private DB db = null;
	private Statement stmt = null;
	private Object caller = null;

	public static BatchStatement create(DB db, Object caller) {
		return new BatchStatement(db, caller);
	}

	private BatchStatement(DB db, Object caller) {
		this.db = db;
		this.caller = caller;
	}

	public void addBatch(String sql) {
		sqlList.add(sql);
	}

	protected Statement getStatement() {
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
				sqlList.clear();
			} catch (SQLException e) {
				logger.error("", e);
				throw new RuntimeException(e);
			}
		}
	}

	public int[] executeBatch() {
		if (sqlList.size() == 0) {
			logger.error("Query is Null");
			return new int[] { 0 };
		}
		int[] upCnts = null;
		try {
			Statement stmt = getStatement();
			if (logger.isDebugEnabled()) {
				StringBuilder log = new StringBuilder();
				log.append("@Sql Start (BATCH STATEMENT) FetchSize : " + stmt.getFetchSize() + " Caller : " + caller.getClass().getName() + "\n");
				log.append("@Sql Command : \n" + getSQL());
				logger.debug(log.toString());
			}
			for (int i = 0, size = sqlList.size(); i < size; i++) {
				stmt.addBatch(sqlList.get(i));
			}
			upCnts = stmt.executeBatch();
			if (logger.isDebugEnabled()) {
				logger.debug("@Sql End (BATCH STATEMENT)");
			}
		} catch (SQLException e) {
			if (logger.isDebugEnabled()) {
				logger.error("", e);
			}
			throw new RuntimeException(e.getMessage() + "\nSQL : \n" + getSQL(), e);
		}
		return upCnts;
	}

	public String getSQL() {
		StringBuilder buf = new StringBuilder();
		for (int i = 0, size = sqlList.size(); i < size; i++) {
			buf.append(sqlList.get(i) + "\n");
		}
		return buf.toString();
	}

	@Override
	public String toString() {
		return "SQL : " + getSQL();
	}
}