/** 
 * @(#)AbstractDaoSupport.java
 */
package framework.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractDaoSupport {
	private static Log _logger = LogFactory.getLog(framework.db.AbstractDaoSupport.class);
	protected DB db = null;

	public AbstractDaoSupport(DB db) {
		this.db = db;
	}

	protected Log getLogger() {
		return AbstractDaoSupport._logger;
	}

	protected void commit() {
		this.db.commit();
	}

	protected void rollback() {
		this.db.rollback();
	}

	protected abstract RecordSet select(String query);

	protected abstract RecordSet select(String query, Object[] where);

	protected abstract RecordSet select(String query, int currPage, int pageSize);

	protected abstract RecordSet select(String query, Object[] where, int currPage, int pageSize);

	protected abstract int insert(String query);

	protected abstract int insert(String query, Object[] where);

	protected abstract int delete(String query);

	protected abstract int delete(String query, Object[] where);

	protected abstract int update(String query);

	protected abstract int update(String query, Object[] where);

	protected abstract int[] batch(String[] queries);

	protected abstract int[] batch(String query, Object[] where);
}
