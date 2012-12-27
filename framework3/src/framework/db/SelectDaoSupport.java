/** 
 * @(#)SelectDaoSupport.java
 */
package framework.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SELECT 하는 DAO를 작성할때 상속받는 부모 클래스이다.
 */
public class SelectDaoSupport {
	private static Log _logger = LogFactory.getLog(framework.db.SelectDaoSupport.class);
	private ConnectionManager _connMgr = null;

	public SelectDaoSupport(ConnectionManager connMgr) {
		_connMgr = connMgr;
	}

	protected ConnectionManager getConnection() {
		return _connMgr;
	}

	protected RecordSet select(String query) {
		return select(query, null, 0, 0);
	}

	protected RecordSet select(String query, Object[] where) {
		return select(query, where, 0, 0);
	}

	protected RecordSet select(String query, int currPage, int pageSize) {
		return select(query, null, currPage, pageSize);
	}

	protected RecordSet select(String query, Object[] where, int currPage, int pageSize) {
		if (where == null) {
			return _statmentSelect(query, currPage, pageSize);
		} else {
			return _prepardSelect(query, where, currPage, pageSize);
		}
	}

	protected void commit() {
		getConnection().commit();
	}

	protected void rollback() {
		getConnection().rollback();
	}

	protected Log getLogger() {
		return SelectDaoSupport._logger;
	}

	private RecordSet _prepardSelect(String query, Object[] where, int currPage, int pageSize) {
		SQLPreparedStatement pstmt = _connMgr.createPrepareStatement(query);
		pstmt.set(where);
		RecordSet rs = pstmt.executeQuery(currPage, pageSize);
		pstmt.close();
		return rs;
	}

	private RecordSet _statmentSelect(String query, int currPage, int pageSize) {
		SQLStatement stmt = _connMgr.createStatement(query);
		RecordSet rs = stmt.executeQuery(currPage, pageSize);
		stmt.close();
		return rs;
	}
}