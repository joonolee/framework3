/** 
 * @(#)MyBatisDaoSupport.java
 */
package framework.db;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * MyBatis를 이용한 DAO를 작성할때 상속받는 부모 클래스이다.
 */
public class MyBatisDaoSupport {
	private static Log _logger = LogFactory.getLog(framework.db.MyBatisDaoSupport.class);
	protected DB db = null;
	protected static SqlSessionFactory sqlSessionFactory = null;

	static {
		try {
			Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			reader.close();
		} catch (Exception e) {
			throw new RuntimeException("Something bad happened while building the SqlSessionFactory instance." + e, e);
		}
	}

	public MyBatisDaoSupport(DB db) {
		this.db = db;
	}

	protected Log getLogger() {
		return MyBatisDaoSupport._logger;
	}

	public void commit() {
		this.db.commit();
	}

	public void rollback() {
		this.db.rollback();
	}

	public int delete(String statement) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.delete(statement);
	}

	public int delete(String statement, Object parameter) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.delete(statement, parameter);
	}

	public <T> T getMapper(Class<T> type) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.getMapper(type);
	}

	public int insert(String statement) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.insert(statement);
	}

	public int insert(String statement, Object parameter) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.insert(statement, parameter);
	}

	public void select(String statement, ResultHandler handler) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		sqlSession.select(statement, handler);
	}

	public void select(String statement, Object parameter, ResultHandler handler) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		sqlSession.select(statement, parameter, handler);
	}

	public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		sqlSession.select(statement, parameter, rowBounds, handler);
	}

	public <E> List<E> selectList(String statement) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.selectList(statement);
	}

	public <E> List<E> selectList(String statement, Object parameter) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.selectList(statement, parameter);
	}

	public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.selectList(statement, parameter, rowBounds);
	}

	public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.selectMap(statement, mapKey);
	}

	public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.selectMap(statement, parameter, mapKey);
	}

	public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.selectMap(statement, parameter, mapKey, rowBounds);
	}

	public <T> T selectOne(String statement) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.selectOne(statement);
	}

	public <T> T selectOne(String statement, Object parameter) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.selectOne(statement, parameter);
	}

	public int update(String statement) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.update(statement);
	}

	public int update(String statement, Object parameter) {
		SqlSession sqlSession = sqlSessionFactory.openSession(db.getConnection());
		return sqlSession.update(statement, parameter);
	}
}
