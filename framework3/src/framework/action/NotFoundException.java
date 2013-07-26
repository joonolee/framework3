package framework.action;

/**
 * 호출한 url이 없을 경우 프레임워크 내부에서 발생하는 예외(404 not found)
 */
public class NotFoundException extends RuntimeException {
	private static final long serialVersionUID = 2427049883577660202L;

	public NotFoundException(String message) {
		super(message);
	}
}
