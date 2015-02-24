package framework.db;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapSession;

/**
 * IBatisSession 을 이용하기 위한 객체
 */
public class IBatisSession extends DBStatement {
	private SqlMapSession _session = null;

	public static IBatisSession create(DB db, SqlMapClient sqlMapClient) {
		return new IBatisSession(db, sqlMapClient);
	}

	private IBatisSession(DB db, SqlMapClient sqlMapClient) {
		_session = sqlMapClient.openSession(db.getConnection());
	}

	public SqlMapSession getSession() {
		return _session;
	}

	@Override
	public void close() {
		if (_session != null) {
			_session.close();
		}
	}
}