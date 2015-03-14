package framework.db;

import java.io.IOException;
import java.io.Reader;
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
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * 데이타베이스 컨넥션을 관리하는 클래스
 */
public class DB {
	private static final Map<String, DataSource> _dsMap = new HashMap<String, DataSource>();
	private static final Log logger = LogFactory.getLog(framework.db.DB.class);
	private List<AbstractStatement> _stmtList = null;
	private String _dsName = null;
	private Object _caller = null;
	private Connection _connection = null;
	// MyBatis
	private static SqlSessionFactory _sqlSessionFactory = null;
	private SqlSession _sqlSession = null;

	public DB(String dsName, Object caller) {
		_dsName = dsName;
		_caller = caller;
		if (_stmtList == null) {
			_stmtList = new ArrayList<AbstractStatement>();
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

	public PreparedStatement createPrepareStatement(String sql) {
		PreparedStatement pstmt = PreparedStatement.create(sql, this, _caller);
		_stmtList.add(pstmt);
		return pstmt;
	}

	public BatchPreparedStatement createBatchPrepareStatement(String sql) {
		BatchPreparedStatement pstmt = BatchPreparedStatement.create(sql, this, _caller);
		_stmtList.add(pstmt);
		return pstmt;
	}

	public Statement createStatement(String sql) {
		Statement stmt = Statement.create(sql, this, _caller);
		_stmtList.add(stmt);
		return stmt;
	}

	public BatchStatement createBatchStatement() {
		BatchStatement bstmt = BatchStatement.create(this, _caller);
		_stmtList.add(bstmt);
		return bstmt;
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

	public void setConnection(Connection conn) {
		_connection = conn;
	}

	public Connection getConnection() {
		return _connection;
	}

	public SqlSession getSqlSession() {
		if (_sqlSession == null) {
			_sqlSession = _getSqlSessionFactory().openSession(_connection);
		}
		return _sqlSession;
	}

	public void release() {
		if (_stmtList != null) {
			for (AbstractStatement stmt : _stmtList) {
				try {
					stmt.close();
				} catch (Throwable e) {
					logger.error("", e);
				}
			}
		}
		if (_connection != null) {
			try {
				_connection.rollback();
			} catch (Throwable e) {
				logger.error("", e);
			}
			try {
				_connection.close();
			} catch (Throwable e) {
				logger.error("", e);
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

	public void commit() {
		try {
			_connection.commit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void rollback() {
		try {
			_connection.rollback();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setAutoCommit(boolean isAuto) {
		try {
			_connection.setAutoCommit(isAuto);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private synchronized SqlSessionFactory _getSqlSessionFactory() {
		if (_sqlSessionFactory == null) {
			Reader reader = null;
			try {
				reader = Resources.getResourceAsReader("mybatis-config.xml");
				_sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			} catch (Throwable e) {
				throw new RuntimeException("Something bad happened while building the SqlSessionFactory instance." + e, e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
		}
		return _sqlSessionFactory;
	}
}