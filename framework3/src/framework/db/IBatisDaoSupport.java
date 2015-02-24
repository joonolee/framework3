package framework.db;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ibatis.sqlmap.client.SqlMapSession;
import com.ibatis.sqlmap.client.event.RowHandler;

/**
 * iBatis를 이용한 DAO를 작성할때 상속받는 부모 클래스이다.
 */
public class IBatisDaoSupport {
	protected static final Log logger = LogFactory.getLog(framework.db.IBatisDaoSupport.class);
	protected static SqlMapClient sqlMapClient = null;
	protected DB db = null;
	protected SqlMapSession sqlMapSession = null;

	static {
		Reader reader = null;
		try {
			reader = Resources.getResourceAsReader("ibatis-config.xml");
			sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
		} catch (Throwable e) {
			throw new RuntimeException("Something bad happened while building the SqlMapClient instance." + e, e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public IBatisDaoSupport(DB db) {
		this.db = db;
		IBatisSession session = db.createIBatisSession(sqlMapClient);
		sqlMapSession = session.getSession();
	}

	protected void commit() {
		this.db.commit();
	}

	protected void rollback() {
		this.db.rollback();
	}

	public int delete(String id) {
		try {
			return sqlMapSession.delete(id);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public int delete(String id, Object parameterObject) {
		try {
			return sqlMapSession.delete(id, parameterObject);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public int executeBatch() {
		try {
			return sqlMapSession.executeBatch();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<?> executeBatchDetailed() {
		try {
			return sqlMapSession.executeBatchDetailed();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public Object insert(String id) {
		try {
			return sqlMapSession.insert(id);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Object insert(String id, Object parameterObject) {
		try {
			return sqlMapSession.insert(id, parameterObject);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<?> queryForList(String id) {
		try {
			return sqlMapSession.queryForList(id);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<?> queryForList(String id, Object parameterObject) {
		try {
			return sqlMapSession.queryForList(id, parameterObject);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<?> queryForList(String id, int skip, int max) {
		try {
			return sqlMapSession.queryForList(id, skip, max);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<?> queryForList(String id, Object parameterObject, int skip, int max) {
		try {
			return sqlMapSession.queryForList(id, parameterObject, skip, max);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<?, ?> queryForMap(String id, Object parameterObject, String keyProp) {
		try {
			return sqlMapSession.queryForMap(id, parameterObject, keyProp);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<?, ?> queryForMap(String id, Object parameterObject, String keyProp, String valueProp) {
		try {
			return sqlMapSession.queryForMap(id, parameterObject, keyProp, valueProp);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Object queryForObject(String id) {
		try {
			return sqlMapSession.queryForObject(id);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Object queryForObject(String id, Object parameterObject) {
		try {
			return sqlMapSession.queryForObject(id, parameterObject);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Object queryForObject(String id, Object parameterObject, Object resultObject) {
		try {
			return sqlMapSession.queryForObject(id, parameterObject, resultObject);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void queryWithRowHandler(String id, RowHandler rowHandler) {
		try {
			sqlMapSession.queryWithRowHandler(id, rowHandler);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void queryWithRowHandler(String id, Object parameterObject, RowHandler rowHandler) {
		try {
			sqlMapSession.queryWithRowHandler(id, parameterObject, rowHandler);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void startBatch() {
		try {
			sqlMapSession.startBatch();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public int update(String id) {
		try {
			return sqlMapSession.update(id);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public int update(String id, Object parameterObject) {
		try {
			return sqlMapSession.update(id, parameterObject);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}