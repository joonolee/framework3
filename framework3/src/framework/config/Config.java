package framework.config;

import java.util.ResourceBundle;

/** 
 * 설정파일(config.properties)에서 값을 읽어오는 클래스이다. 
 * 싱글톤 패턴으로 설정파일에 접근하는 객체의 인스턴스가 오직 한개만 생성이 된다.
 */
public class Config {
	private static final Config instance = new Config();
	private static final String NAME = "config";
	private ResourceBundle bundle = null;

	private Config() {
		bundle = ResourceBundle.getBundle(NAME);
	}

	/** 
	 * 객체의 인스턴스를 리턴해준다.
	 * @return Configuration 객체의 인스턴스
	 */
	public static Config getInstance() {
		return instance;
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 String 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 String 객체
	 */
	public String get(String key) {
		return getString(key);
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 boolean형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 boolean형 변수
	 */
	public boolean getBoolean(String key) {
		return (Boolean.valueOf(bundle.getString(key).trim())).booleanValue();
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 int형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 int형 변수
	 */
	public int getInt(String key) {
		try {
			return Integer.parseInt(bundle.getString(key).trim().replaceAll(",", ""));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 String 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 String 객체
	 */
	public String getString(String key) {
		return bundle.getString(key).trim();
	}

	/**
	 * 키(key)가 포함되어있는지 여부를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열s
	 * @return key의 포함여부
	 */
	public boolean containsKey(String key) {
		return bundle.containsKey(key);
	}
}