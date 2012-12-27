/* 
 * @(#)ConnectionManager.java
 * 데이타베이스 컨넥션을 관리하는 클래스
 */
package framework.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConnectionManager {
	private static Map<String, DataSource> _dsMap = new HashMap<String, DataSource>();
	private static Log _logger = LogFactory.getLog(framework.db.ConnectionManager.class);
	private List<DBStatement> _stmtList = null;
	private String _dsName = null;
	private Object _caller = null;
	private Connection _connection = null;

	public ConnectionManager(String dsName, Object caller) {
		_dsName = dsName;
		_caller = caller;
		if (_stmtList == null) {
			_stmtList = new ArrayList<DBStatement>();
		}
		if (dsName != null) {
			if (_dsMap.get(dsName) == null) {
				DataSource ds;
				try {
					InitialContext ctx = new InitialContext();
					ds = (DataSource) ctx.lookup(dsName);
				} catch (NamingException e) {
					throw new RuntimeException(e);
				}
				_dsMap.put(dsName, ds);
			}
		}
	}

	public SQLPreparedStatement createPrepareStatement(String sql) {
		SQLPreparedStatement pstmt = SQLPreparedStatement.create(sql, this, _caller);
		_stmtList.add(pstmt);
		return pstmt;
	}

	public SQLBatchPreparedStatement createBatchPrepareStatement(String sql) {
		SQLBatchPreparedStatement pstmt = SQLBatchPreparedStatement.create(sql, this, _caller);
		_stmtList.add(pstmt);
		return pstmt;
	}

	public SQLStatement createStatement(String sql) {
		SQLStatement stmt = SQLStatement.create(sql, this, _caller);
		_stmtList.add(stmt);
		return stmt;
	}

	public SQLBatchStatement createBatchStatement() {
		SQLBatchStatement bstmt = SQLBatchStatement.create(this, _caller);
		_stmtList.add(bstmt);
		return bstmt;
	}

	public void commit() {
		try {
			getRawConnection().commit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void connect() {
		try {
			setConnection(_dsMap.get(_dsName).getConnection());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		if (_getLogger().isDebugEnabled()) {
			_getLogger().debug("DB연결 성공! => " + _dsName);
		}
	}

	public void connect(String jdbcDriver, String url, String userID, String userPW) {
		try {
			DriverManager.registerDriver((Driver) Class.forName(jdbcDriver).newInstance());
			setConnection(DriverManager.getConnection(url, userID, userPW));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (_getLogger().isDebugEnabled()) {
			_getLogger().debug("DB연결 성공! => " + url);
		}
	}

	public Connection getRawConnection() {
		return _connection;
	}

	public void setConnection(Connection conn) {
		_connection = conn;
	}

	public void release() {
		if (_stmtList != null) {
			for (DBStatement stmt : _stmtList) {
				stmt.close();
			}
		}
		if (getRawConnection() != null) {
			try {
				getRawConnection().rollback();
			} catch (Exception e) {
				_getLogger().error("Connection rollback error!", e);
			}
			try {
				getRawConnection().close();
			} catch (Exception e) {
				_getLogger().error("Connection close error!", e);
			}
			if (_getLogger().isDebugEnabled()) {
				_getLogger().debug("DB연결 종료! => " + _dsName);
			}
		} else {
			if (_getLogger().isDebugEnabled()) {
				_getLogger().debug("@CONNECTION IS NULL");
			}
		}
	}

	public void rollback() {
		try {
			getRawConnection().rollback();
		} catch (SQLException e) {
		}
	}

	public void setAutoCommit(boolean isAuto) {
		try {
			getRawConnection().setAutoCommit(isAuto);
		} catch (SQLException e) {
		}
	}

	private Log _getLogger() {
		return ConnectionManager._logger;
	}
}