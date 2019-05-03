package framework.db;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * 키를 소문자로 관리하고, 키가 추가된 순서를 유지하는 범용 맵 클래스
 */
public class RecordMap extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = 8720579036875443934L;

	public RecordMap() {
	}

	public RecordMap(int cnt) {
		super(cnt);
	}

	@Override
	public Object put(String key, Object value) {
		if (value instanceof Clob) {
			Clob clob = (Clob) value;
			try {
				value = clob.getSubString(1, (int) clob.length());
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		return super.put(key.toLowerCase(), value);
	}

	@Override
	public Object get(Object key) {
		return super.get(key.toString().toLowerCase());
	}

	public String getString(String key) {
		Object value = get(key);
		if (value == null) {
			return "";
		}
		return value.toString();
	}

	public Integer getInt(String key) {
		return getInteger(key);
	}

	public Integer getInteger(String key) {
		Object value = get(key);
		if (value == null) {
			return Integer.valueOf(0);
		} else if (value instanceof Integer) {
			return (Integer) value;
		} else {
			try {
				return Integer.valueOf(value.toString().trim());
			} catch (NumberFormatException e) {
				return Integer.valueOf(0);
			}
		}
	}

	public Long getLong(String key) {
		Object value = get(key);
		if (value == null) {
			return Long.valueOf(0);
		} else if (value instanceof Long) {
			return (Long) value;
		} else {
			try {
				return Long.valueOf(value.toString().trim());
			} catch (NumberFormatException e) {
				return Long.valueOf(0);
			}
		}
	}

	public Float getFloat(String key) {
		Object value = get(key);
		if (value == null) {
			return Float.valueOf(0);
		} else if (value instanceof Float) {
			return (Float) value;
		} else {
			try {
				return Float.valueOf(value.toString().trim());
			} catch (NumberFormatException e) {
				return Float.valueOf(0);
			}
		}
	}

	public Double getDouble(String key) {
		Object value = get(key);
		if (value == null) {
			return Double.valueOf(0);
		} else if (value instanceof Double) {
			return (Double) value;
		} else {
			try {
				return Double.valueOf(value.toString().trim());
			} catch (NumberFormatException e) {
				return Double.valueOf(0);
			}
		}
	}

	public BigDecimal getBigDecimal(String key) {
		Object value = get(key);
		if (value == null) {
			return BigDecimal.valueOf(0);
		} else if (value instanceof BigDecimal) {
			return (BigDecimal) value;
		} else {
			try {
				return new BigDecimal(value.toString().trim());
			} catch (NumberFormatException e) {
				return BigDecimal.valueOf(0);
			}
		}
	}

	public Date getDate(String key) {
		return getDateFormat(key, "yyyy-MM-dd");
	}

	public Date getDateTime(String key) {
		return getDateFormat(key, "yyyy-MM-dd HH:mm:ss");
	}

	public Date getDateFormat(String key, String format) {
		Object value = get(key);
		if (value == null) {
			return null;
		} else if (value instanceof java.sql.Date) {
			java.sql.Date sqlDate = (java.sql.Date) value;
			return new Date(sqlDate.getTime());
		} else if (value instanceof java.util.Date) {
			return (Date) value;
		} else {
			String str = value.toString().trim();
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			sdf.setLenient(false);
			try {
				return sdf.parse(str);
			} catch (ParseException e) {
				return null;
			}
		}
	}

	public Timestamp getTimestamp(String key) {
		Object value = get(key);
		if (value == null) {
			return null;
		} else if (value instanceof java.sql.Date) {
			java.sql.Date sqlDate = (java.sql.Date) value;
			return new Timestamp(sqlDate.getTime());
		} else if (value instanceof Timestamp) {
			return (Timestamp) value;
		} else if (value instanceof Date) {
			Date date = (Date) value;
			return new Timestamp(date.getTime());
		} else {
			return Timestamp.valueOf(value.toString());
		}
	}
}