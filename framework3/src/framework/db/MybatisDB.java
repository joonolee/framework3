package framework.db;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * Mybatis SqlSession 을 관리하는 클래스
 */
public class MybatisDB {
	private static final Log logger = LogFactory.getLog(framework.db.MybatisDB.class);
	private static SqlSessionFactory sqlSessionFactory = null;
	private SqlSession sqlSession = null;

	public MybatisDB(Connection connection) {
		sqlSession = getSqlSessionFactory().openSession(connection);
	}

	public SqlSession getSqlSession() {
		return sqlSession;
	}

	public void commit() {
		try {
			sqlSession.getConnection().commit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void rollback() {
		try {
			sqlSession.getConnection().rollback();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void clearCache() {
		sqlSession.clearCache();
	}

	private synchronized SqlSessionFactory getSqlSessionFactory() {
		if (sqlSessionFactory == null) {
			Reader reader = null;
			try {
				reader = Resources.getResourceAsReader("mybatis-config.xml");
				sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			} catch (Throwable e) {
				throw new RuntimeException("Something bad happened while building the SqlSessionFactory instance." + e.getMessage(), e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
		}
		return sqlSessionFactory;
	}
}