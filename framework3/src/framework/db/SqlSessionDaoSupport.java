package framework.db;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

/**
 * MyBatis를 이용한 DAO를 작성할때 상속받는 부모 클래스이다.
 */
public class SqlSessionDaoSupport {
	protected static final Log logger = LogFactory.getLog(framework.db.SqlSessionDaoSupport.class);
	protected MybatisDB db = null;
	protected SqlSession sqlSession = null;

	public SqlSessionDaoSupport(DB db) {
		this.db = db.getMybatisDB();
		this.sqlSession = this.db.getSqlSession();
	}

	protected void commit() {
		this.db.commit();
	}

	protected void rollback() {
		this.db.rollback();
	}

	protected void clearCache() {
		sqlSession.clearCache();
	}

	protected int delete(String statement) {
		return sqlSession.delete(statement);
	}

	protected int delete(String statement, Object parameter) {
		return sqlSession.delete(statement, parameter);
	}

	protected <T> T getMapper(Class<T> type) {
		return sqlSession.getMapper(type);
	}

	protected int insert(String statement) {
		return sqlSession.insert(statement);
	}

	protected int insert(String statement, Object parameter) {
		return sqlSession.insert(statement, parameter);
	}

	protected void select(String statement, ResultHandler handler) {
		sqlSession.select(statement, handler);
	}

	protected void select(String statement, Object parameter, ResultHandler handler) {
		sqlSession.select(statement, parameter, handler);
	}

	protected void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
		sqlSession.select(statement, parameter, rowBounds, handler);
	}

	protected <E> List<E> selectList(String statement) {
		return sqlSession.selectList(statement);
	}

	protected <E> List<E> selectList(String statement, Object parameter) {
		return sqlSession.selectList(statement, parameter);
	}

	protected <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
		return sqlSession.selectList(statement, parameter, rowBounds);
	}

	protected <K, V> Map<K, V> selectMap(String statement, String mapKey) {
		return sqlSession.selectMap(statement, mapKey);
	}

	protected <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
		return sqlSession.selectMap(statement, parameter, mapKey);
	}

	protected <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
		return sqlSession.selectMap(statement, parameter, mapKey, rowBounds);
	}

	protected <T> T selectOne(String statement) {
		return sqlSession.selectOne(statement);
	}

	protected <T> T selectOne(String statement, Object parameter) {
		return sqlSession.selectOne(statement, parameter);
	}

	protected int update(String statement) {
		return sqlSession.update(statement);
	}

	protected int update(String statement, Object parameter) {
		return sqlSession.update(statement, parameter);
	}

	protected RecordSet selectRecordSet(String statement) {
		return sqlSession.selectOne(statement);
	}

	protected RecordSet selectRecordSet(String statement, Object parameter) {
		return sqlSession.selectOne(statement, parameter);
	}
}