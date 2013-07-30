package framework.db;

/**
 * DB오류가 발생할 경우 프레임워크 내부에서 발생하는 예외
 */
public class DBException extends RuntimeException {
	private static final long serialVersionUID = 1042099258893291176L;

	public DBException() {
		super();
	}

	public DBException(String message) {
		super(message);
	}

	public DBException(Exception e) {
		super(e);
	}
}