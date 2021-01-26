package framework.util;

/**
 * 사용자가 식별하기 쉬운 숫자-문자열로 10진수 숫자를 변환하는 유틸리티 클래스
 */
public class UciUtil {
	/**
	 * UCI 체계에서 사용하는 alpha digits 문자열 시퀀스
	 */
	private static final String APLPA_DIGITS_STRING = "0123456789ABCDEFGHJKLMNPQRSTUVWXY";

	/**
	 * UCI alpha digits 문자열 길이
	 */
	private static final int APLPA_DIGITS_LENGTH = APLPA_DIGITS_STRING.length();

	/**
	 * UCI번호 체계로 진법을 변경한다. 자릿수를 줄이기 위해서임
	 * @param decNo 10진수 숫자
	 * @return UCI 체계로 변환된 문자열
	 */
	public static String convertBase(long decNo) {
		if (decNo == 0) {
			return "0";
		}
		StringBuilder sb = new StringBuilder();
		while (decNo != 0) {
			sb.insert(0, APLPA_DIGITS_STRING.charAt((int) (decNo % APLPA_DIGITS_LENGTH)));
			decNo /= APLPA_DIGITS_LENGTH;
		}
		return sb.toString();
	}
}