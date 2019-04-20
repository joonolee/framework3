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
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import framework.config.Config;

/**
 * DB 컨넥션을 관리하는 클래스
 */
public class DB {
	private static final Log logger = LogFactory.getLog(DB.class);
	private static final Map<String, DataSource> dsMap = new HashMap<String, DataSource>();
	private final List<AbstractStatement> stmtList = new ArrayList<AbstractStatement>();
	private String serviceName = null;
	private Object caller = null;
	private Connection connection = null;
	// Mybatis
	private MybatisDB mybatisDB = null;

	public DB(String serviceName, Object caller) {
		this.serviceName = serviceName;
		this.caller = caller;
		Config config = Config.getInstance();
		try {
			String jndiName = config.getString("db." + serviceName + ".jndiName");
			if (!dsMap.containsKey(jndiName)) {
				InitialContext ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup(jndiName);
				dsMap.put(jndiName, ds);
			}
			setConnection(dsMap.get(jndiName).getConnection());
		} catch (Throwable e) {
			String driver = config.getString("db." + serviceName + ".driver");
			String url = config.getString("db." + serviceName + ".url");
			String username = config.getString("db." + serviceName + ".username");
			String password = config.getString("db." + serviceName + ".password");
			try {
				DriverManager.registerDriver((Driver) Class.forName(driver).newInstance());
				setConnection(DriverManager.getConnection(url, username, password));
			} catch (Throwable e2) {
				throw new RuntimeException(e2);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("DB연결 성공! : " + serviceName);
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

	public void setConnection(Connection conn) {
		connection = conn;
	}

	public Connection getConnection() {
		return connection;
	}

	public void release() {
		for (AbstractStatement stmt : stmtList) {
			try {
				stmt.close();
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
		if (mybatisDB != null) {
			mybatisDB.clearCache();
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
				logger.debug("DB연결 종료! : " + serviceName);
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

	public String getServiceName() {
		return serviceName;
	}

	public MybatisDB getMybatisDB() {
		if (mybatisDB == null) {
			mybatisDB = new MybatisDB(connection);
		}
		return mybatisDB;
	}
}