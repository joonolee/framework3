package framework.action;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import framework.config.Config;
import framework.util.StringUtil;

/**
 * 요청객체, 쿠키객체의 값을 담는 해시테이블 클래스
 * 요청객체의 파라미터를 추상화 하여 Params 를 생성해 놓고 파라미터이름을 키로 해당 값을 원하는 데이타 타입으로 반환받는다.
 */
public class Params extends HashMap<String, String[]> {
	private static final long serialVersionUID = 7143941735208780214L;
	private final List<FileItem> fileItems = new ArrayList<FileItem>();
	private String name = null;

	/***
	 * Params 생성자
	 * @param name Params 객체의 이름
	 */
	public Params(String name) {
		super();
		this.name = name;
	}

	/**
	 * 요청객체의 파라미터 이름과 값을 저장한 해시테이블을 생성한다.
	 * <br>
	 * ex) request Params 객체를 얻는 경우 : Params params = Params.getParams(request)
	 * @param request HTTP 클라이언트 요청객체
	 * @return 요청Params 객체
	 */
	public static Params getParams(HttpServletRequest request) {
		Params params = new Params("Params");
		for (Object obj : request.getParameterMap().keySet()) {
			String key = (String) obj;
			params.put(key, request.getParameterValues(key));
		}
		if (ServletFileUpload.isMultipartContent(request)) {
			try {
				DiskFileItemFactory factory = new DiskFileItemFactory();
				factory.setSizeThreshold(Config.getInstance().getInt("fileupload.sizeThreshold"));
				factory.setRepository(new File(Config.getInstance().getString("fileupload.repository")));
				ServletFileUpload upload = new ServletFileUpload(factory);
				upload.setSizeMax(Config.getInstance().getInt("fileupload.sizeMax"));
				@SuppressWarnings("unchecked")
				List<FileItem> items = upload.parseRequest(request);
				for (FileItem item : items) {
					if (item.isFormField()) {
						String fieldName = item.getFieldName();
						String fieldValue = item.getString(request.getCharacterEncoding());
						String[] oldValue = params.getArray(fieldName);
						if (oldValue == null) {
							params.put(fieldName, new String[] { fieldValue });
						} else {
							int size = oldValue.length;
							String[] newValue = new String[size + 1];
							for (int i = 0; i < size; i++) {
								newValue[i] = oldValue[i];
							}
							newValue[size] = fieldValue;
							params.put(fieldName, newValue);
						}
					} else {
						params.addFileItem(item);
					}
				}
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		return params;
	}

	/**
	 * 요청객체의 쿠키 이름과 값을 저장한 해시테이블을 생성한다.
	 * <br>
	 * ex) cookie Params 객체를 얻는 경우 : Params params = Params.getParamsFromCookie(request)
	 * @param request HTTP 클라이언트 요청객체
	 * @return 쿠키Params 객체
	 */
	public static Params getParamsFromCookie(HttpServletRequest request) {
		Params cookieParams = new Params("Cookie");
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				cookieParams.put(cookie.getName(), new String[] { StringUtil.nullToBlankString(cookie.getValue()) });
			}
		}
		return cookieParams;
	}

	/**
	 * 요청객체의 헤더 이름과 값을 저장한 해시테이블을 생성한다.
	 * <br>
	 * ex) header Params 객체를 얻는 경우 : Params params = Params.getParamsFromHeader(request)
	 * @param request HTTP 클라이언트 요청객체
	 * @return 헤더Params 객체
	 */
	public static Params getParamsFromHeader(HttpServletRequest request) {
		Params headerParams = new Params("Header");
		Enumeration<?> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = (String) headerNames.nextElement();
			headerParams.put(headerName.toLowerCase(), new String[] { StringUtil.nullToBlankString(request.getHeader(headerName)) });
		}
		return headerParams;
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 문자열 배열을 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 값
	 */
	public String[] getArray(String key) {
		return getArray(key, new String[] {});
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 문자열 배열을 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 값 또는 기본 값
	 */
	public String[] getArray(String key, String[] defaultValue) {
		String[] value = super.get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Boolean 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 값
	 */
	public Boolean getBoolean(String key) {
		return getBoolean(key, Boolean.FALSE);
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Boolean 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 값 또는 기본 값
	 */
	public Boolean getBoolean(String key, Boolean defaultValue) {
		String value = getRawString(key).trim();
		if (value.isEmpty()) {
			return defaultValue;
		}
		return Boolean.valueOf(value);
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Double 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 값
	 */
	public Double getDouble(String key) {
		return getDouble(key, Double.valueOf(0));
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Double 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 값 또는 기본 값
	 */
	public Double getDouble(String key, Double defaultValue) {
		try {
			String value = getRawString(key).trim().replaceAll(",", "");
			if (value.isEmpty()) {
				return defaultValue;
			}
			return Double.valueOf(value);
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 BigDecimal 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 값
	 */
	public BigDecimal getBigDecimal(String key) {
		return getBigDecimal(key, BigDecimal.ZERO);
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 BigDecimal 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 값 또는 기본 값
	 */
	public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
		try {
			String value = getRawString(key).trim().replaceAll(",", "");
			if (value.isEmpty()) {
				return defaultValue;
			}
			return new BigDecimal(value);
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Float 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 값
	 */
	public Float getFloat(String key) {
		return getFloat(key, Float.valueOf(0));
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Float 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 값 또는 기본 값
	 */
	public Float getFloat(String key, Float defaultValue) {
		try {
			String value = getRawString(key).trim().replaceAll(",", "");
			if (value.isEmpty()) {
				return defaultValue;
			}
			return Float.valueOf(value);
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Integer 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 값
	 */
	public Integer getInteger(String key) {
		return getInteger(key, Integer.valueOf(0));
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Integer 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 값 또는 기본 값
	 */
	public Integer getInteger(String key, Integer defaultValue) {
		try {
			String value = getRawString(key).trim().replaceAll(",", "");
			if (value.isEmpty()) {
				return defaultValue;
			}
			return Integer.valueOf(value);
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Long 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 값
	 */
	public Long getLong(String key) {
		return getLong(key, Long.valueOf(0));
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 Long 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 값 또는 기본 값
	 */
	public Long getLong(String key, Long defaultValue) {
		try {
			String value = getRawString(key).trim().replaceAll(",", "");
			if (value.isEmpty()) {
				return defaultValue;
			}
			return Long.valueOf(value);
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 String 객체를 리턴한다.
	 * 크로스사이트 스크립팅 공격 방지를 위해 &lt;, &gt; 치환을 수행한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 값
	 */
	@Deprecated
	public String get(String key) {
		return getString(key);
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 String 객체를 리턴한다.
	 * 크로스사이트 스크립팅 공격 방지를 위해 &lt;, &gt; 치환을 수행한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 값
	 */
	public String getString(String key) {
		return getString(key, "");
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 String 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 값 또는 기본 값
	 */
	@Deprecated
	public String get(String key, String defaultValue) {
		return getString(key, defaultValue);
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 String 객체를 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 값 또는 기본 값
	 */
	public String getString(String key, String defaultValue) {
		return StringUtil.escapeHtmlSpecialChars(getRawString(key, defaultValue));
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 String 객체를 변환없이 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @return key에 매핑되어 있는 값
	 */
	public String getRawString(String key) {
		return getRawString(key, "");
	}

	/**
	 * 키(key)문자열과 매핑되어 있는 String 객체를 변환없이 리턴한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param defaultValue 값이 없을 때 리턴할 기본 값
	 * @return key에 매핑되어 있는 값 또는 기본 값
	 */
	public String getRawString(String key, String defaultValue) {
		String[] value = super.get(key);
		if (value == null || value.length == 0 || StringUtil.isEmpty(value[0])) {
			return defaultValue;
		}
		return value[0];
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
			return sdf.parse(getRawString(key).trim());
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
			return sdf.parse(getRawString(key).trim());
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * 파일아이템(FileItem)의 리스트 객체를 리턴한다.
	 * @return 파일아이템 리스트 객체
	 */
	public List<FileItem> getFileItems() {
		List<FileItem> list = new ArrayList<FileItem>();
		list.addAll(fileItems);
		return list;
	}

	/**
	 * 키(key)에 매핑되는 스트링을 셋팅한다.
	 * @param key 값을 찾기 위한 키 문자열
	 * @param value 키에 매핑되는 문자열
	 * @return 원래 key에 매핑되어 있는 스트링 배열
	 */
	public String[] putString(String key, String value) {
		return put(key, new String[] { value });
	}

	/**
	 * Param 객체가 가지고 있는 값들을 화면 출력을 위해 문자열로 변환한다.
	 * @return 화면에 출력하기 위해 변환된 문자열
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("{ ");
		long currentRow = 0;
		for (String key : keySet()) {
			String value = null;
			String[] o = super.get(key);
			if (o == null) {
				value = "";
			} else {
				int length = o.length;
				if (length == 0) {
					value = "";
				} else if (length == 1) {
					String item = o[0];
					if (item == null) {
						value = "";
					} else {
						value = item;
					}
				} else {
					StringBuilder valueBuf = new StringBuilder();
					valueBuf.append("[");
					for (int j = 0; j < length; j++) {
						String item = o[j];
						if (item != null) {
							valueBuf.append(item);
						}
						if (j < length - 1) {
							valueBuf.append(",");
						}
					}
					valueBuf.append("]");
					value = valueBuf.toString();
				}
			}
			if (currentRow++ > 0) {
				buf.append(", ");
			}
			buf.append(key + "=" + value);
		}
		buf.append(" }");
		return name + "=" + buf.toString();
	}

	/**
	 * Params 객체가 가지고 있는 값들을 쿼리 스트링으로 변환한다.
	 * @return 쿼리 스트링으로 변환된 문자열
	 */
	public String toQueryString() {
		StringBuilder buf = new StringBuilder();
		long currentRow = 0;
		for (String key : keySet()) {
			String[] o = super.get(key);
			if (currentRow++ > 0) {
				buf.append("&");
			}
			if (o == null) {
				buf.append(key + "=" + "");
			} else {
				StringBuilder valueBuf = new StringBuilder();
				for (int j = 0, length = o.length; j < length; j++) {
					String item = o[j];
					if (item != null) {
						valueBuf.append(key + "=" + item);
					}
					if (j < length - 1) {
						valueBuf.append("&");
					}
				}
				buf.append(valueBuf.toString());
			}
		}
		return buf.toString();
	}

	/**
	 * Params 객체가 가지고 있는 값들을 Xml로 변환한다.
	 * @return Xml로 변환된 문자열
	 */
	public String toXml() {
		StringBuilder buf = new StringBuilder();
		buf.append("<items>");
		buf.append("<item>");
		for (String key : keySet()) {
			String[] o = super.get(key);
			if (o == null) {
				buf.append("<" + key.toLowerCase() + ">" + "</" + key.toLowerCase() + ">");
			} else {
				int length = o.length;
				if (length == 0) {
					buf.append("<" + key.toLowerCase() + ">" + "</" + key.toLowerCase() + ">");
				} else if (length == 1) {
					String item = o[0];
					if (item == null || "".equals(item)) {
						buf.append("<" + key.toLowerCase() + ">" + "</" + key.toLowerCase() + ">");
					} else {
						buf.append("<" + key.toLowerCase() + ">" + "<![CDATA[" + item + "]]>" + "</" + key.toLowerCase() + ">");
					}
				} else {
					for (int j = 0; j < length; j++) {
						String item = o[j];
						if (item == null || "".equals(item)) {
							buf.append("<" + key.toLowerCase() + ">" + "</" + key.toLowerCase() + ">");
						} else {
							buf.append("<" + key.toLowerCase() + ">" + "<![CDATA[" + item + "]]>" + "</" + key.toLowerCase() + ">");
						}
					}
				}
			}
		}
		buf.append("</item>");
		buf.append("</items>");
		return buf.toString();
	}

	/**
	 * Params 객체가 가지고 있는 값들을 Json 표기법으로 변환한다.
	 * @return Json 표기법으로 변환된 문자열
	 */
	public String toJson() {
		StringBuilder buf = new StringBuilder();
		buf.append("{ ");
		long currentRow = 0;
		for (String key : keySet()) {
			String value = null;
			String[] o = super.get(key);
			if (o == null) {
				value = "\"\"";
			} else {
				int length = o.length;
				if (length == 0) {
					value = "\"\"";
				} else if (length == 1) {
					String item = o[0];
					if (item == null) {
						value = "\"\"";
					} else {
						value = "\"" + escapeJS(item) + "\"";
					}
				} else {
					StringBuilder valueBuf = new StringBuilder();
					valueBuf.append("[");
					for (int j = 0; j < length; j++) {
						String item = o[j];
						if (item != null) {
							valueBuf.append("\"" + escapeJS(item) + "\"");
						}
						if (j < length - 1) {
							valueBuf.append(",");
						}
					}
					valueBuf.append("]");
					value = valueBuf.toString();
				}
			}
			if (currentRow++ > 0) {
				buf.append(", ");
			}
			buf.append("\"" + escapeJS(key) + "\"" + ":" + value);
		}
		buf.append(" }");
		return buf.toString();
	}

	//////////////////////////////////////////////////////////////////////////////////////////Private 메소드
	/**
	 * 자바스크립트상에 특수하게 인식되는 문자들을 JSON등에 사용하기 위해 변환하여준다.
	 * @param str 변환할 문자열
	 */
	private String escapeJS(String str) {
		if (str == null) {
			return "";
		}
		return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\").replaceAll("\r\n", "\\\\n").replaceAll("\n", "\\\\n");
	}

	/**
	 * Multipart 파일업로드시 파일 아이템을 리스트에 추가한다.
	 * @param item 파일을 담고 있는 객체
	 * @return 성공여부
	 */
	private boolean addFileItem(FileItem item) {
		return fileItems.add(item);
	}
}