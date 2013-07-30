package framework.action;

/**
 * 호출한 url이 없을 경우 프레임워크 내부에서 발생하는 예외(404 not found)
 */
public class _404Exception extends RuntimeException {
	private static final long serialVersionUID = 2427049883577660202L;

	public _404Exception() {
		super();
	}

	public _404Exception(String message) {
		super(message);
	}

	public _404Exception(Exception e) {
		super(e);
	}
}
