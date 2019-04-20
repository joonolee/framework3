package framework.util;

/**
 * 유효성 체크 라이브러리
 */
public final class ValidationUtil {

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private ValidationUtil() {
	}

	/**
	 * 주민등록번호/외국인등록번호 유효성 체크
	 * @param residentRegistrationNo 주민등록번호/외국인등록번호
	 * @return 매치여부
	 */
	public static boolean isResidentRegistrationNo(String residentRegistrationNo) {
		String juminNo = residentRegistrationNo.replaceAll("[^0-9]", "");
		if (juminNo.length() != 13) {
			return false;
		}
		int yy = parseInt(juminNo.substring(0, 2));
		int mm = parseInt(juminNo.substring(2, 4));
		int dd = parseInt(juminNo.substring(4, 6));
		if (yy < 1 || yy > 99 || mm > 12 || mm < 1 || dd < 1 || dd > 31) {
			return false;
		}
		int sum = 0;
		int juminNo_6 = parseInt(juminNo.charAt(6));
		if (juminNo_6 == 1 || juminNo_6 == 2 || juminNo_6 == 3 || juminNo_6 == 4) {
			//내국인
			for (int i = 0; i < 12; i++) {
				sum += parseInt(juminNo.charAt(i)) * ((i % 8) + 2);
			}
			if (parseInt(juminNo.charAt(12)) != (11 - (sum % 11)) % 10) {
				return false;
			}
			return true;
		} else if (juminNo_6 == 5 || juminNo_6 == 6 || juminNo_6 == 7 || juminNo_6 == 8) {
			//외국인
			if (parseInt(juminNo.substring(7, 9)) % 2 != 0) {
				return false;
			}
			for (int i = 0; i < 12; i++) {
				sum += parseInt(juminNo.charAt(i)) * ((i % 8) + 2);
			}
			if (parseInt(juminNo.charAt(12)) != ((11 - (sum % 11)) % 10 + 2) % 10) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 주민등록번호/외국인등록번호 유효성 체크
	 * @param juminNo 주민등록번호/외국인등록번호
	 * @return 매치여부
	 */
	public static boolean isJuminNo(String juminNo) {
		return isResidentRegistrationNo(juminNo);
	}

	/**
	 * 법인번호 유효성 체크
	 * @param corporationRegistrationNo 법인번호
	 * @return 매치여부
	 */
	public static boolean isCorporationRegistrationNo(String corporationRegistrationNo) {
		String corpRegNo = corporationRegistrationNo.replaceAll("[^0-9]", "");
		if (corpRegNo.length() != 13) {
			return false;
		}
		int sum = 0;
		for (int i = 0; i < 12; i++) {
			sum += ((i % 2) + 1) * parseInt(corpRegNo.charAt(i));
		}
		if (parseInt(corpRegNo.charAt(12)) != (10 - (sum % 10)) % 10) {
			return false;
		}
		return true;

	}

	/**
	 * 사업자등록번호 유효성 체크
	 * @param businessRegistrationNo 사업자등록번호
	 * @return 매치여부
	 */
	public static boolean isBusinessRegistrationNo(String businessRegistrationNo) {
		String bizRegNo = businessRegistrationNo.replaceAll("[^0-9]", "");
		if (bizRegNo.length() != 10) {
			return false;
		}
		int share = (int) (Math.floor(parseInt(bizRegNo.charAt(8)) * 5) / 10);
		int rest = (parseInt(bizRegNo.charAt(8)) * 5) % 10;
		int sum = (parseInt(bizRegNo.charAt(0))) + ((parseInt(bizRegNo.charAt(1)) * 3) % 10) + ((parseInt(bizRegNo.charAt(2)) * 7) % 10) + ((parseInt(bizRegNo.charAt(3)) * 1) % 10) + ((parseInt(bizRegNo.charAt(4)) * 3) % 10) + ((parseInt(bizRegNo.charAt(5)) * 7) % 10) + ((parseInt(bizRegNo.charAt(6)) * 1) % 10) + ((parseInt(bizRegNo.charAt(7)) * 3) % 10) + share + rest + (parseInt(bizRegNo.charAt(9)));
		if (sum % 10 != 0) {
			return false;
		}
		return true;
	}

	/**
	 * 신용카드번호 유효성 체크
	 * @param creditCardNo 신용카드번호
	 * @return 매치여부
	 */
	public static boolean isCreditCardNo(String creditCardNo) {
		return PatternUtil.matchCreditCardNo(creditCardNo).find();
	}

	/**
	 * 여권번호 유효성 체크
	 * @param passportNo 여권번호
	 * @return 매치여부
	 */
	public static boolean isPassportNo(String passportNo) {
		return PatternUtil.matchPassportNo(passportNo).find();
	}

	/**
	 * 운전면허번호 유효성 체크
	 * @param driversLicenseNo 운전면허번호
	 * @return 매치여부
	 */
	public static boolean isDriversLicenseNo(String driversLicenseNo) {
		return PatternUtil.matchDriversLicenseNo(driversLicenseNo).find();
	}

	/**
	 * 휴대폰번호 유효성 체크
	 * @param cellphoneNo 휴대폰번호
	 * @return 매치여부
	 */
	public static boolean isCellphoneNo(String cellphoneNo) {
		return PatternUtil.matchCellphoneNo(cellphoneNo).find();
	}

	/**
	 * 일반전화번호 유효성 체크
	 * @param telephoneNo 일반전화번호
	 * @return 매치여부
	 */
	public static boolean isTelephoneNo(String telephoneNo) {
		return PatternUtil.matchTelephoneNo(telephoneNo).find();
	}

	/**
	 * 건강보험번호 유효성 체크
	 * @param healthInsuranceNo 건강보험번호
	 * @return 매치여부
	 */
	public static boolean isHealthInsuranceNo(String healthInsuranceNo) {
		return PatternUtil.matchHealthInsuranceNo(healthInsuranceNo).find();
	}

	/**
	 * 계좌번호 유효성 체크
	 * @param bankAccountNo 은행계좌번호
	 * @return 매치여부
	 */
	public static boolean isBankAccountNo(String bankAccountNo) {
		return PatternUtil.matchBankAccountNo(bankAccountNo).find();
	}

	/**
	 * 이메일주소 유효성 체크
	 * @param emailAddress 이메일주소
	 * @return 매치여부
	 */
	public static boolean isEmailAddress(String emailAddress) {
		return PatternUtil.matchEmailAddress(emailAddress).find();
	}

	/**
	 * 아이피주소 유효성 체크
	 * @param ipAddress 아이피주소
	 * @return 매치여부
	 */
	public static boolean isIPAddress(String ipAddress) {
		return PatternUtil.matchIPAddress(ipAddress).find();
	}

	////////////////////////////////////////////////////////////////////////////////////////Private 메소드

	/**
	 * char로 표현된 숫자를 타입을 int로 변경
	 */
	private static int parseInt(char c) {
		return Integer.parseInt(String.valueOf(c));
	}

	/**
	 * String으로 표현된 숫자를 타입을 int로 변경
	 */
	private static int parseInt(String s) {
		return Integer.parseInt(s);
	}
}