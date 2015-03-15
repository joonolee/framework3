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
public class SqlSessionDao {
	protected static final Log logger = LogFactory.getLog(framework.db.SqlSessionDao.class);
	protected DB db = null;
	protected SqlSession sqlSession = null;

	public SqlSessionDao(DB db) {
		this.db = db;
		this.sqlSession = db.getSqlSession();
	}

	public void commit() {
		this.db.commit();
	}

	public void rollback() {
		this.db.rollback();
	}

	public int delete(String statement) {
		return this.sqlSession.delete(statement);
	}

	public int delete(String statement, Object parameter) {
		return this.sqlSession.delete(statement, parameter);
	}

	public <T> T getMapper(Class<T> type) {
		return this.sqlSession.getMapper(type);
	}

	public int insert(String statement) {
		return this.sqlSession.insert(statement);
	}

	public int insert(String statement, Object parameter) {
		return this.sqlSession.insert(statement, parameter);
	}

	public void select(String statement, ResultHandler handler) {
		this.sqlSession.select(statement, handler);
	}

	public void select(String statement, Object parameter, ResultHandler handler) {
		this.sqlSession.select(statement, parameter, handler);
	}

	public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
		this.sqlSession.select(statement, parameter, rowBounds, handler);
	}

	public <E> List<E> selectList(String statement) {
		return this.sqlSession.selectList(statement);
	}

	public <E> List<E> selectList(String statement, Object parameter) {
		return this.sqlSession.selectList(statement, parameter);
	}

	public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
		return this.sqlSession.selectList(statement, parameter, rowBounds);
	}

	public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
		return this.sqlSession.selectMap(statement, mapKey);
	}

	public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
		return this.sqlSession.selectMap(statement, parameter, mapKey);
	}

	public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
		return this.sqlSession.selectMap(statement, parameter, mapKey, rowBounds);
	}

	public <T> T selectOne(String statement) {
		return this.sqlSession.selectOne(statement);
	}

	public <T> T selectOne(String statement, Object parameter) {
		return this.sqlSession.selectOne(statement, parameter);
	}

	public int update(String statement) {
		return this.sqlSession.update(statement);
	}

	public int update(String statement, Object parameter) {
		return this.sqlSession.update(statement, parameter);
	}
}