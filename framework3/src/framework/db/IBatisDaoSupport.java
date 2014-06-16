/** 
 * @(#)IBatisDaoSupport.java
 */
package framework.db;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ibatis.sqlmap.client.event.RowHandler;

/**
 * iBatis를 이용한 DAO를 작성할때 상속받는 부모 클래스이다.
 */
public class IBatisDaoSupport {
	private static Log _logger = LogFactory.getLog(framework.db.IBatisDaoSupport.class);
	protected DB db = null;
	protected static SqlMapClient sqlMapClient = null;

	static {
		try {
			Reader reader = Resources.getResourceAsReader("ibatis-config.xml");
			sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
			reader.close();
		} catch (Exception e) {
			throw new RuntimeException("Something bad happened while building the SqlMapClient instance." + e, e);
		}
	}

	public IBatisDaoSupport(DB db) {
		this.db = db;
		try {
			sqlMapClient.setUserConnection(db.getConnection());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Log getLogger() {
		return IBatisDaoSupport._logger;
	}

	protected void commit() {
		this.db.commit();
	}

	protected void rollback() {
		this.db.rollback();
	}

	public int delete(String id) {
		try {
			return sqlMapClient.delete(id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int delete(String id, Object parameterObject) {
		try {
			return sqlMapClient.delete(id, parameterObject);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int executeBatch() {
		try {
			return sqlMapClient.executeBatch();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<?> executeBatchDetailed() {
		try {
			return sqlMapClient.executeBatchDetailed();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object insert(String id) {
		try {
			return sqlMapClient.insert(id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object insert(String id, Object parameterObject) {
		try {
			return sqlMapClient.insert(id, parameterObject);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<?> queryForList(String id) {
		try {
			return sqlMapClient.queryForList(id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<?> queryForList(String id, Object parameterObject) {
		try {
			return sqlMapClient.queryForList(id, parameterObject);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<?> queryForList(String id, int skip, int max) {
		try {
			return sqlMapClient.queryForList(id, skip, max);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<?> queryForList(String id, Object parameterObject, int skip, int max) {
		try {
			return sqlMapClient.queryForList(id, parameterObject, skip, max);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Map<?, ?> queryForMap(String id, Object parameterObject, String keyProp) {
		try {
			return sqlMapClient.queryForMap(id, parameterObject, keyProp);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Map<?, ?> queryForMap(String id, Object parameterObject, String keyProp, String valueProp) {
		try {
			return sqlMapClient.queryForMap(id, parameterObject, keyProp, valueProp);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object queryForObject(String id) {
		try {
			return sqlMapClient.queryForObject(id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object queryForObject(String id, Object parameterObject) {
		try {
			return sqlMapClient.queryForObject(id, parameterObject);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object queryForObject(String id, Object parameterObject, Object resultObject) {
		try {
			return sqlMapClient.queryForObject(id, parameterObject, resultObject);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void queryWithRowHandler(String id, RowHandler rowHandler) {
		try {
			sqlMapClient.queryWithRowHandler(id, rowHandler);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void queryWithRowHandler(String id, Object parameterObject, RowHandler rowHandler) {
		try {
			sqlMapClient.queryWithRowHandler(id, parameterObject, rowHandler);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void startBatch() {
		try {
			sqlMapClient.startBatch();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int update(String id) {
		try {
			return sqlMapClient.update(id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int update(String id, Object parameterObject) {
		try {
			return sqlMapClient.update(id, parameterObject);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
