package framework.config;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * 설정파일(application.properties)에서 값을 읽어오는 클래스
 * 싱글톤 패턴으로 설정파일에 접근하는 객체의 인스턴스가 오직 한개만 생성이 된다.
 */
public class Config {
	private static final Config INSTANCE = new Config();
	private static final String CONFIG_NAME = "application";
	private final ResourceBundle bundle;

	private Config() {
		bundle = ResourceBundle.getBundle(CONFIG_NAME);
	}

	/**
	 * 객체의 인스턴스를 리턴해준다.
	 * @return Configuration 객체의 인스턴스
	 */
	public static Config getInstance() {
		return INSTANCE;
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
	 * 키(key)문자열과 매핑되어 있는 String 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 String 객체 또는 기본 값
	 */
	public String getString(String key, String defaultValue) {
		try {
			return bundle.getString(key).trim();
		} catch (Throwable e) {
			return defaultValue;
		}
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
	 * 키(key)문자열과 매핑되어 있는 String 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 String 객체 또는 기본 값
	 */
	public String get(String key, String defaultValue) {
		return getString(key, defaultValue);
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Boolean 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 Boolean 객체
	 */
	public Boolean getBoolean(String key) {
		return Boolean.valueOf(getString(key));
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Boolean 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 Boolean 객체 또는 기본 값
	 */
	public Boolean getBoolean(String key, Boolean defaultValue) {
		try {
			return Boolean.valueOf(getString(key));
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Integer 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 Integer 형 변수
	 */
	public Integer getInt(String key) {
		return getInteger(key);
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Integer 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 Integer 형 변수
	 */
	public Integer getInt(String key, Integer defaultValue) {
		return getInteger(key, defaultValue);
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Integer 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 Integer 형 변수
	 */
	public Integer getInteger(String key) {
		return Integer.valueOf(getString(key));
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Integer 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 Integer 형 변수
	 */
	public Integer getInteger(String key, Integer defaultValue) {
		try {
			return Integer.valueOf(getString(key));
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Long 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 Long 형 변수
	 */
	public Long getLong(String key) {
		return Long.valueOf(getString(key));
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Long 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 Long 형 변수
	 */
	public Long getLong(String key, Long defaultValue) {
		try {
			return Long.valueOf(getString(key));
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Float 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 Float 형 변수
	 */
	public Float getFloat(String key) {
		return Float.valueOf(getString(key));
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Float 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 Float 형 변수
	 */
	public Float getFloat(String key, Float defaultValue) {
		try {
			return Float.valueOf(getString(key));
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Double 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 Double 형 변수
	 */
	public Double getDouble(String key) {
		return Double.valueOf(getString(key));
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Double 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 Double 형 변수
	 */
	public Double getDouble(String key, Double defaultValue) {
		try {
			return Double.valueOf(getString(key));
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 BigDecimal 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 BigDecimal 형 변수
	 */
	public BigDecimal getBigDecimal(String key) {
		return new BigDecimal(getString(key));
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 BigDecimal 형 변수를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 BigDecimal 형 변수
	 */
	public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
		try {
			return new BigDecimal(getString(key));
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Date 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열(형식: yyyy-MM-dd)
	 * @return key에 매핑되어 있는 값
	 */
	public Date getDate(String key) {
		return getDateFormat(key, "yyyy-MM-dd");
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Date 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열(형식: yyyy-MM-dd)
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 값 또는 기본 값
	 */
	public Date getDate(String key, Date defaultValue) {
		return getDateFormat(key, "yyyy-MM-dd", defaultValue);
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Date 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열(형식: yyyy-MM-dd HH:mm:ss)
	 * @return key에 매핑되어 있는 값
	 */
	public Date getDateTime(String key) {
		return getDateFormat(key, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Date 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열(형식: yyyy-MM-dd HH:mm:ss)
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 값 또는 기본 값
	 */
	public Date getDateTime(String key, Date defaultValue) {
		return getDateFormat(key, "yyyy-MM-dd HH:mm:ss", defaultValue);
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Date 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param format 날짜 포맷(예, yyyy-MM-dd HH:mm:ss)
	 * @return key에 매핑되어 있는 값
	 */
	public Date getDateFormat(String key, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		try {
			return sdf.parse(getString(key));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Date 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param format 날짜 포맷(예, yyyy-MM-dd HH:mm:ss)
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 값 또는 기본 값
	 */
	public Date getDateFormat(String key, String format, Date defaultValue) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		try {
			return sdf.parse(getString(key));
		} catch (Throwable e) {
			return defaultValue;
		}
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