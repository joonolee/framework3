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
		String value = getString(key).trim().replaceAll("[^\\d]", "");
		if (value.isEmpty()) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		sdf.setLenient(false);
		try {
			return sdf.parse(value);
		} catch (ParseException e) {
			return null;
		}
	}

	public Timestamp getTimestamp(String key) {
		if (get(key) == null) {
			return null;
		} else {
			return Timestamp.valueOf(getString(key));
		}
	}
}