package framework.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JDBC를 이용한 DAO를 작성할때 상속받는 부모 클래스이다.
 */
public class JdbcDaoSupport {
	protected static final Log logger = LogFactory.getLog(JdbcDaoSupport.class);
	protected DB db = null;

	public JdbcDaoSupport(DB db) {
		this.db = db;
	}

	protected void commit() {
		this.db.commit();
	}

	protected void rollback() {
		this.db.rollback();
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
			return statmentSelect(query, currPage, pageSize);
		} else {
			return preparedSelect(query, where, currPage, pageSize);
		}
	}

	protected int update(String query) {
		return update(query, null);
	}

	protected int update(String query, Object[] where) {
		if (where == null) {
			return statmentUpdate(query);
		} else {
			return preparedUpdate(query, where);
		}
	}

	protected int[] batch(String[] queries) {
		return statmentBatch(queries);
	}

	protected int[] batch(String query, Object[] where) {
		if (where == null) {
			return statmentBatch(new String[] { query });
		} else {
			return preparedBatch(query, where);
		}
	}

	private RecordSet preparedSelect(String query, Object[] where, int currPage, int pageSize) {
		PreparedStatement pstmt = null;
		try {
			pstmt = this.db.createPrepareStatement(query);
			pstmt.set(where);
			return pstmt.executeQuery(currPage, pageSize);
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}

	private RecordSet statmentSelect(String query, int currPage, int pageSize) {
		Statement stmt = null;
		try {
			stmt = this.db.createStatement(query);
			return stmt.executeQuery(currPage, pageSize);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	private int preparedUpdate(String query, Object[] where) {
		PreparedStatement pstmt = null;
		try {
			pstmt = this.db.createPrepareStatement(query);
			pstmt.set(where);
			return pstmt.executeUpdate();
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}

	private int statmentUpdate(String query) {
		Statement stmt = null;
		try {
			stmt = this.db.createStatement(query);
			return stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	private int[] preparedBatch(String query, Object[] where) {
		BatchPreparedStatement pstmt = null;
		try {
			pstmt = this.db.createBatchPrepareStatement(query);
			pstmt.addBatch(where);
			return pstmt.executeBatch();
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}

	private int[] statmentBatch(String[] queries) {
		BatchStatement stmt = null;
		try {
			stmt = this.db.createBatchStatement();
			for (String query : queries) {
				stmt.addBatch(query);
			}
			return stmt.executeBatch();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}
}