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
import org.apache.ibatis.session.SqlSessionFactory;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * 데이타베이스 컨넥션을 관리하는 클래스
 */
public class DB {
	private static Map<String, DataSource> _dsMap = new HashMap<String, DataSource>();
	protected static final Log logger = LogFactory.getLog(framework.db.DB.class);
	private List<DBStatement> _stmtList = null;
	private String _dsName = null;
	private Object _caller = null;
	private Connection _connection = null;

	public DB(String dsName, Object caller) {
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

	public IBatisSession createIBatisSession(SqlMapClient sqlMapClient) {
		IBatisSession session = IBatisSession.create(this, sqlMapClient);
		_stmtList.add(session);
		return session;
	}

	public MyBatisSession createMyBatisSession(SqlSessionFactory sqlSessionFactory) {
		MyBatisSession session = MyBatisSession.create(this, sqlSessionFactory);
		_stmtList.add(session);
		return session;
	}

	public void commit() {
		try {
			getConnection().commit();
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
		if (logger.isDebugEnabled()) {
			logger.debug("DB연결 성공! : " + _dsName);
		}
	}

	public void connect(String jdbcDriver, String url, String userID, String userPW) {
		try {
			DriverManager.registerDriver((Driver) Class.forName(jdbcDriver).newInstance());
			setConnection(DriverManager.getConnection(url, userID, userPW));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("DB연결 성공! : " + url);
		}
	}

	public Connection getConnection() {
		return _connection;
	}

	public void setConnection(Connection conn) {
		_connection = conn;
	}

	public void release() {
		if (_stmtList != null) {
			for (DBStatement stmt : _stmtList) {
				try {
					stmt.close();
				} catch (Throwable e) {
					logger.error("Statement close error!", e);
				}
			}
		}
		if (getConnection() != null) {
			try {
				getConnection().rollback();
			} catch (Throwable e) {
				logger.error("Connection rollback error!", e);
			}
			try {
				getConnection().close();
			} catch (Throwable e) {
				logger.error("Connection close error!", e);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("DB연결 종료! : " + _dsName);
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("@CONNECTION IS NULL");
			}
		}
	}

	public void rollback() {
		try {
			getConnection().rollback();
		} catch (SQLException e) {
			logger.error(e);
		}
	}

	public void setAutoCommit(boolean isAuto) {
		try {
			getConnection().setAutoCommit(isAuto);
		} catch (SQLException e) {
			logger.error(e);
		}
	}
}