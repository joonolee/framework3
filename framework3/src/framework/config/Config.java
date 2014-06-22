package framework.config;

import java.util.ResourceBundle;

/** 
 * 설정파일(config.properties)에서 값을 읽어오는 클래스이다. 
 * 싱글톤 패턴으로 설정파일에 접근하는 객체의 인스턴스가 오직 한개만 생성이 된다.
 */
public class Config {
	private static Config _instance = new Config();
	private static final String _NAME = "config";
	private ResourceBundle _bundle = null;

	private Config() {
		_bundle = ResourceBundle.getBundle(_NAME);
	}

	/** 
	 * 객체의 인스턴스를 리턴해준다.
	 * @return Configuration 객체의 인스턴스
	 */
	public static Config getInstance() {
		return _instance;
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
		boolean value = false;
		value = (Boolean.valueOf(_bundle.getString(key).trim())).booleanValue();
		return value;
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 int형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 int형 변수
	 */
	public int getInt(String key) {
		int value = -1;
		value = Integer.parseInt(_bundle.getString(key).trim());
		return value;
	}

	/** 
	 * 키(key)문자열과 매핑되어 있는 String 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 String 객체
	 */
	public String getString(String key) {
		String value = null;
		value = _bundle.getString(key).trim();
		return value;
	}

	/**
	 * 키(key)가 포함되어있는지 여부를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열s
	 * @return key의 포함여부
	 */
	public boolean containsKey(String key) {
		return _bundle.containsKey(key);
	}
}