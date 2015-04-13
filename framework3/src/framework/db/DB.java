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
	private static final Map<String, DataSource> dsMap = new HashMap<String, DataSource>();
	private static final Log logger = LogFactory.getLog(framework.db.DB.class);
	private List<AbstractStatement> stmtList = null;
	private String dsName = null;
	private Object caller = null;
	private Connection connection = null;
	// MyBatis
	private static SqlSessionFactory sqlSessionFactory = null;
	private SqlSession sqlSession = null;

	public DB(String dsName, Object caller) {
		this.dsName = dsName;
		this.caller = caller;
		if (stmtList == null) {
			stmtList = new ArrayList<AbstractStatement>();
		}
		if (dsName != null) {
			if (dsMap.get(dsName) == null) {
				DataSource ds;
				try {
					InitialContext ctx = new InitialContext();
					ds = (DataSource) ctx.lookup(dsName);
				} catch (NamingException e) {
					throw new RuntimeException(e);
				}
				dsMap.put(dsName, ds);
			}
		}
	}

	public PreparedStatement createPrepareStatement(String sql) {
		PreparedStatement pstmt = PreparedStatement.create(sql, this, caller);
		stmtList.add(pstmt);
		return pstmt;
	}

	public BatchPreparedStatement createBatchPrepareStatement(String sql) {
		BatchPreparedStatement pstmt = BatchPreparedStatement.create(sql, this, caller);
		stmtList.add(pstmt);
		return pstmt;
	}

	public Statement createStatement(String sql) {
		Statement stmt = Statement.create(sql, this, caller);
		stmtList.add(stmt);
		return stmt;
	}

	public BatchStatement createBatchStatement() {
		BatchStatement bstmt = BatchStatement.create(this, caller);
		stmtList.add(bstmt);
		return bstmt;
	}

	public void connect() {
		try {
			setConnection(dsMap.get(dsName).getConnection());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("DB연결 성공! : " + dsName);
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
		connection = conn;
	}

	public Connection getConnection() {
		return connection;
	}

	public SqlSession getSqlSession() {
		if (sqlSession == null) {
			sqlSession = getSqlSessionFactory().openSession(connection);
		}
		return sqlSession;
	}

	public void release() {
		if (stmtList != null) {
			for (AbstractStatement stmt : stmtList) {
				try {
					stmt.close();
				} catch (Throwable e) {
					logger.error("", e);
				}
			}
		}
		if (connection != null) {
			try {
				connection.rollback();
			} catch (Throwable e) {
				logger.error("", e);
			}
			try {
				connection.close();
			} catch (Throwable e) {
				logger.error("", e);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("DB연결 종료! : " + dsName);
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("@CONNECTION IS NULL");
			}
		}
	}

	public void commit() {
		try {
			connection.commit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void rollback() {
		try {
			connection.rollback();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setAutoCommit(boolean isAuto) {
		try {
			connection.setAutoCommit(isAuto);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private synchronized SqlSessionFactory getSqlSessionFactory() {
		if (sqlSessionFactory == null) {
			Reader reader = null;
			try {
				reader = Resources.getResourceAsReader("mybatis-config.xml");
				sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
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
		return sqlSessionFactory;
	}
}