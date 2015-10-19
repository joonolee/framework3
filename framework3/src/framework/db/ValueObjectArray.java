package framework.db;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 테이블의 값을 담는 VO의 배열 클래스
 */
public class ValueObjectArray {
	public final static String INSERT = "IK";
	public final static String UPDATE = "UK";
	public final static String DELETE = "DK";
	public final static String USER_UPDATE = "UU";
	public final static String USER_DELETE = "UD";
	public final static String UPDATE_ONLY = "UO";
	private final Map<String, ValueObject> voMap = new LinkedHashMap<String, ValueObject>();
	private String[] keys = null;
	private String[] fields = null;
	private int seq = 0;

	public void add(String type, ValueObject vo) {
		if (vo == null) {
			return;
		}
		voMap.put(type + seq++, vo);
	}

	public int size() {
		return voMap.size();
	}

	public ValueObject[] get(String type) {
		if (size() == 0) {
			return null;
		}
		List<ValueObject> list = new ArrayList<ValueObject>();
		for (String key : voMap.keySet()) {
			if (key.substring(0, 2).equals(type)) {
				list.add(voMap.get(key));
			}
		}
		ValueObject[] voArray = new ValueObject[list.size()];
		for (int i = 0; i < voArray.length; i++) {
			voArray[i] = list.get(i);
		}
		return voArray;
	}

	public void clear() {
		voMap.clear();
		fields = null;
		keys = null;
	}

	public void setUserKeys(String[] keys) {
		if (keys != null) {
			this.keys = new String[keys.length];
			for (int i = 0; i < keys.length; i++) {
				this.keys[i] = keys[i];
			}
		}
	}

	public void setUserFields(String[] fields) {
		if (fields != null) {
			this.fields = new String[fields.length];
			for (int i = 0; i < fields.length; i++) {
				this.fields[i] = fields[i];
			}
		}
	}

	public String[] getUserKeys() {
		if (keys == null) {
			return null;
		}
		return keys.clone();
	}

	public String[] getUserFields() {
		if (fields == null) {
			return null;
		}
		return fields.clone();
	}
}