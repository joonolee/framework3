/* 
 * @(#)SQLBatchStatement.java
 * Statement의 Batch 처리를 이용하기 위한 객체
 */
package framework.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLBatchStatement extends DBStatement {
	private List<String> _sqlList = new ArrayList<String>();
	private ConnectionManager _connMgr = null;
	private Statement _stmt = null;
	private Object _caller = null;

	public static SQLBatchStatement create(ConnectionManager connMgr, Object caller) {
		return new SQLBatchStatement(connMgr, caller);
	}

	private SQLBatchStatement(ConnectionManager connMgr, Object caller) {
		_connMgr = connMgr;
		_caller = caller;
	}

	public void addBatch(String sql) {
		_sqlList.add(sql);
	}

	protected Statement getStatement() {
		try {
			if (_stmt == null) {
				_stmt = _connMgr.getRawConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				_stmt.setFetchSize(100);
			}
		} catch (SQLException e) {
			getLogger().error("getStatement Error!");
			throw new DBException(e);
		}
		return _stmt;
	}

	@Override
	public void close() {
		if (_stmt != null) {
			try {
				_stmt.close();
				_sqlList.clear();
			} catch (SQLException e) {
				getLogger().error("close Error!");
				throw new DBException(e);
			}
		}
	}

	public int[] executeBatch() {
		if (_sqlList.size() == 0) {
			getLogger().error("Query is Null");
			return new int[] { 0 };
		}
		int[] _upCnts = null;
		try {
			Statement stmt = getStatement();
			if (getLogger().isDebugEnabled()) {
				StringBuilder log = new StringBuilder();
				log.append("@Sql Start (BATCH STATEMENT) FetchSize : " + stmt.getFetchSize() + " Caller : " + _caller.getClass().getName() + "\n");
				log.append("@Sql Command => \n" + getSQL());
				getLogger().debug(log.toString());
			}
			for (int i = 0, size = _sqlList.size(); i < size; i++) {
				stmt.addBatch(_sqlList.get(i));
			}
			_upCnts = stmt.executeBatch();
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("@Sql End (BATCH STATEMENT)");
			}
		} catch (SQLException e) {
			getLogger().error("executeBatch Error!");
			throw new DBException(e.getMessage() + "\nSQL : \n" + getSQL());
		}
		return _upCnts;
	}

	public String getSQL() {
		StringBuilder str = new StringBuilder();
		for (int i = 0, size = _sqlList.size(); i < size; i++) {
			str.append(_sqlList.get(i) + "\n");
		}
		return str.toString();
	}

	public String toString() {
		return "SQL : " + getSQL();
	}
}