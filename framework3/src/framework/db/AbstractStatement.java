package framework.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 모든 SQL 문장을 처리하는 클래스가 상속받아야 할 추상클래스이다.
 */
public abstract class AbstractStatement {
	protected static final Log logger = LogFactory.getLog(framework.db.AbstractStatement.class);

	/**
	 * Statement의 close 를 구현하기 위한 추상 메소드
	 */
	public abstract void close();
}