package framework.db;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

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
		if (get(key) == null) {
			return "";
		}
		return get(key).toString().trim();
	}

	public int getInt(String key) {
		return getBigDecimal(key).intValue();
	}

	public int getInteger(String key) {
		return getBigDecimal(key).intValue();
	}

	public long getLong(String key) {
		return getBigDecimal(key).longValue();
	}

	public double getDouble(String key) {
		return getBigDecimal(key).doubleValue();
	}

	public BigDecimal getBigDecimal(String key) {
		if (get(key) == null) {
			return BigDecimal.valueOf(0);
		}
		return new BigDecimal(get(key).toString());
	}

	public float getFloat(String key) {
		return getBigDecimal(key).floatValue();
	}

	public Date getDate(String key) {
		return getDate(key, "yyyy-MM-dd");
	}

	public Date getDateTime(String key) {
		return getDate(key, "yyyy-MM-dd HH:mm:ss");
	}

	public Date getDate(String key, String format) {
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
		} else if (value instanceof java.sql.Timestamp) {
			return (Timestamp) value;
		} else if (value instanceof java.util.Date) {
			java.util.Date date = (java.util.Date) value;
			return new Timestamp(date.getTime());
		} else {
			return Timestamp.valueOf(value.toString());
		}
	}
}