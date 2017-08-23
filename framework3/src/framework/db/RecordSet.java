package framework.db;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 데이터베이스 쿼리를 수행한 후 그 결과에 대한 접근 기반을 제공하는 클래스이다.
 */
public class RecordSet implements Iterable<RecordMap>, Serializable {
	private static final long serialVersionUID = -1248669129395067939L;
	//Rows의 값
	private final List<RecordMap> rows = new ArrayList<RecordMap>();
	/**
	 * DB의 columns 이름
	 */
	private String[] colNms = null;
	private int[] colSize = null;
	private int[] colSizeReal = null;
	private int[] colScale = null;
	private String[] colInfo = null;
	private int[] columnsType = null;
	private int currow = 0;

	public RecordSet() {
	};

	/**
	 * RecordSet의 생성자
	 */
	public RecordSet(ResultSet rs) {
		this(rs, 0, 0);
	}

	/**
	 * 주어진 범위에 포함되는 새로운 RecordSet 객체를 생성한다
	 * @param rs 쿼리 실행결과
	 * @param curpage 현재 표시할 페이지
	 * @param pagesize 한 페이지에 표시할 데이터 갯수
	 */
	public RecordSet(ResultSet rs, int curpage, int pagesize) {
		if (rs == null) {
			return;
		}
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int cnt = rsmd.getColumnCount();
			colNms = new String[cnt];
			colInfo = new String[cnt];
			colSize = new int[cnt];
			colSizeReal = new int[cnt];
			colScale = new int[cnt];
			// byte[] 데이터 처리를 위해서 추가
			columnsType = new int[cnt];
			for (int i = 1; i <= cnt; i++) {
				//Table의 Field 가 대문자 인것은 소문자로 변경처리
				colNms[i - 1] = rsmd.getColumnName(i).toLowerCase();
				columnsType[i - 1] = rsmd.getColumnType(i);
				//Fiels 의 정보 및 Size 추가
				colSize[i - 1] = rsmd.getColumnDisplaySize(i);
				colSizeReal[i - 1] = rsmd.getPrecision(i);
				colScale[i - 1] = rsmd.getScale(i);
				colInfo[i - 1] = rsmd.getColumnTypeName(i);
			}
			rs.setFetchSize(100);
			int num = 0;
			while (rs.next()) {
				// 현재 Row 저장 객체
				RecordMap columns = new RecordMap(cnt);
				num++;
				if (curpage != 0 && (num < (curpage - 1) * pagesize + 1)) {
					continue;
				}
				if (pagesize != 0 && (num > curpage * pagesize)) {
					break;
				}
				for (int i = 1; i <= cnt; i++) {
					if (rs.getObject(i) instanceof Number) {
						columns.put(colNms[i - 1], rs.getObject(i));
					} else {
						columns.put(colNms[i - 1], rs.getString(i));
					}
				}
				rows.add(columns);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	/**
	 * 주어진 쿼리를 수행 후 컬럼명을 String[] 로 반환
	 * @return String[]
	 */
	public String[] getColumns() {
		if (colNms == null) {
			return null;
		}
		return colNms.clone();
	}

	/**
	 * 주어진 쿼리를 수행 후 컬럼의 Size을 int[] 로 반환
	 * @return String[]
	 */
	public int[] getColumnsSize() {
		if (colSize == null) {
			return null;
		}
		return colSize.clone();
	}

	/**
	 * 주어진 쿼리를 수행 후 컬럼의 실제 Size(숫자속성에 사용)을 int[] 로 반환
	 * @return String[]
	 */
	public int[] getColumnsSizeReal() {
		if (colSizeReal == null) {
			return null;
		}
		return colSizeReal.clone();
	}

	/**
	 * 주어진 쿼리를 수행 후 컬럼의 소숫점 아래 사이즈를 int[] 로 반환
	 * @return String[]
	 */
	public int[] getColumnsScale() {
		if (colScale == null) {
			return null;
		}
		return colScale.clone();
	}

	/**
	 * 주어진 쿼리를 수행 후 컬럼의 성격을  String[] 로 반환
	 * @return String[]
	 */
	public String[] getColumnsInfo() {
		if (colInfo == null) {
			return null;
		}
		return colInfo.clone();
	}

	/**
	 * 주어진 쿼리를 수행 후 컬럼의 타입을 int[] 로 반환
	 * @return String[]
	 */
	public int[] getColumnsType() {
		if (columnsType == null) {
			return null;
		}
		return columnsType.clone();
	}

	/**
	 * 주어진 쿼리를 수행 후 결과를  ArrayList 로 반환
	 * @return ArrayList
	 */
	public List<RecordMap> getRows() {
		List<RecordMap> list = new ArrayList<RecordMap>();
		list.addAll(rows);
		return list;
	}

	/**
	 * 주어진 쿼리 수행 후 결과 column의 갯수를 구한다
	 * @return	int 컬럼의 갯수
	 */
	public int getColumnCount() {
		if (colNms == null) {
			return 0;
		}
		return colNms.length;
	}

	/**
	 * 주어진 쿼리 수행 후 결과 row의 갯수를 구한다
	 * @return	int Row의 갯수
	 */
	public int getRowCount() {
		if (rows == null) {
			return 0;
		}
		return rows.size();
	}

	/**
	 * 주어진 쿼리 수행 후 결과 row의 갯수를 구한다
	 * @return	int Row의 갯수
	 */
	public int size() {
		if (rows == null) {
			return 0;
		}
		return rows.size();
	}

	/**
	 * 현재 참조하고 있는 row의 위치를 구한다.
	 * @return int 현재 Row의 위치
	 */
	public int getCurrentRow() {
		return currow;
	}

	/**
	 * 쿼리 수행에 의해 얻어진 결과의 특정 column의 이름을 얻는다
	 * @param colIdx 얻고자 하는 컬럼 위치, 첫번째 컬럼은 1
	 * @return String 해당 column의 이름
	 */
	public String getColumnLabel(int colIdx) {
		if (colIdx < 1) {
			throw new IllegalArgumentException("index 0 is not vaild ");
		}
		if (colNms == null) {
			throw new RuntimeException("Column is not find");
		}
		String label = colNms[colIdx - 1];
		return label;
	}

	/**
	 * RecordSet의 처음으로 이동한다.
	 * @return boolean
	 */
	public boolean firstRow() {
		return moveRow(0);
	}

	/**
	 * RecordSet의 처음row인지 아닌지 여부 판단.
	 * @return boolean
	 */
	public boolean isFirst() {
		return (currow == 0);
	}

	/**
	 * RecordSet의 마지막row인지 아닌지 여부 판단.
	 * @return boolean
	 */
	public boolean isLast() {
		return (currow == rows.size() && rows.size() != 0);
	}

	/**
	 * RecordSet의 마지막으로 이동한다.
	 * @return boolean
	 */
	public boolean lastRow() {
		if (rows == null || rows.size() == 0) {
			return false;
		}
		currow = rows.size();
		return true;
	}

	/**
	 * RecordSet에서 현재 row의 다음 row로 이동한다.
	 * @return boolean
	 */
	public boolean nextRow() {
		currow++;
		if (currow == 0 || rows == null || rows.size() == 0 || currow > rows.size()) {
			return false;
		}
		return true;
	}

	/**
	 * RecordSet의 현재 row의 이전 row로 이동한다.
	 * @return boolean
	 */
	public boolean preRow() {
		currow--;
		if (currow == 0 || rows == null || rows.size() == 0 || currow > rows.size()) {
			return false;
		}
		return true;
	}

	/**
	 * 해당하는 하는 row로 이동
	 */
	public boolean moveRow(int row) {
		if (rows != null && rows.size() != 0 && row <= rows.size()) {
			currow = row;
			return true;
		}
		return false;
	}

	/**
	 * Recordset 데이타를 얻어온다.
	 * @param row cnt : start 1
	 * @param colName column name
	 * @return Object  column data
	 */
	public Object get(int row, String colName) {
		return rows.get(row - 1).get(colName.toLowerCase());
	}

	/**
	 * RecordSet의 column 값을 String으로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName  column name
	 * @return String  column data
	 */
	public String getString(int row, String colName) {
		if (get(row, colName) == null) {
			return "";
		}
		return get(row, colName).toString().trim();
	}

	/**
	 * RecordSet의 column 값을 int로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName   column name
	 * @return int  column data
	 */
	public int getInt(int row, String colName) {
		return getBigDecimal(row, colName).intValue();
	}

	/**
	 * RecordSet의 column 값을 int로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName   column name
	 * @return int  column data
	 */
	public int getInteger(int row, String colName) {
		return getBigDecimal(row, colName).intValue();
	}

	/**
	 * RecordSet의 column 값을 long 형으로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName   column name
	 * @return long  column data
	 */
	public long getLong(int row, String colName) {
		return getBigDecimal(row, colName).longValue();
	}

	/**
	 * RecordSet의 Column 값을 double 로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName   column name
	 * @return double column data
	 */
	public double getDouble(int row, String colName) {
		return getBigDecimal(row, colName).doubleValue();
	}

	/**
	 * RecordSet의 Column 값을 BigDecimal 로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return BigDecimal column data
	 */
	public BigDecimal getBigDecimal(int row, String colName) {
		if (get(row, colName) == null) {
			return BigDecimal.valueOf(0);
		}
		return new BigDecimal(get(row, colName).toString());
	}

	/**
	 * RecordSet의 Column 값을 BigDecimal 로 반환하는 메소드
	 * @param colName column name
	 * @return BigDecimal column data
	 */
	public BigDecimal getBigDecimal(String colName) {
		return getBigDecimal(currow, colName);
	}

	/**
	 * RecordSet의 column 값을 float로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return float  column data
	 */
	public float getFloat(int row, String colName) {
		return getBigDecimal(row, colName).floatValue();
	}

	/**
	 * RecordSet의 column 값을 Date형으로 반환하는 메소드(형식: "yyyy-MM-dd")
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return float  column data
	 */
	public Date getDate(int row, String colName) {
		return getDate(row, colName, "yyyy-MM-dd");
	}

	/**
	 * RecordSet의 column 값을 Date형으로 반환하는 메소드(형식: "yyyy-MM-dd HH:mm:ss")
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return float  column data
	 */
	public Date getDateTime(int row, String colName) {
		return getDate(row, colName, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * RecordSet의 column 값을 Date형으로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @param format date format
	 * @return float  column data
	 */
	public Date getDate(int row, String colName, String format) {
		Object value = get(row, colName);
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

	/**
	 * RecordSet의 column 값을 Timestamp형으로 반환하는 메소드
	 * YYYY-MM-DD 로 반환
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return float  column data
	 */
	public Timestamp getTimestamp(int row, String colName) {
		Object value = get(row, colName);
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

	/**
	 * 현재 pointing 된 row의 column 데이터를 읽는다
	 * @param colIdx column number, 첫번째 column 은 1
	 * @return String column data
	 */
	public Object get(int colIdx) {
		return get(currow, colNms[colIdx]);
	}

	/**
	 * 현재행의 RecordSet의 int 값을 반환하는 메소드
	 * @param colIdx column number, 첫번째 column은 1
	 * @return int
	 */
	public int getInt(int colIdx) {
		return getInt(currow, colNms[colIdx]);
	}

	/**
	 * 현재행의 RecordSet의 int 값을 반환하는 메소드
	 * @param colIdx  column number, 첫번째 column은 1
	 * @return Integer
	 */
	public int getInteger(int colIdx) {
		return getInteger(currow, colNms[colIdx]);
	}

	/**
	 * 현재 행의 RecordSet의 long 값을 반환하는 메소드
	 * @param colIdx  column number, 첫번째 column은 1
	 * @return long
	 */
	public long getLong(int colIdx) {
		return getLong(currow, colNms[colIdx]);
	}

	/**
	 * 현재 행의 RecordSet의 float 값을 반환하는 메소드
	 * @param colIdx  column number, 첫번째 column은 1
	 * @return float
	 */
	public float getFloat(int colIdx) {
		return getFloat(currow, colNms[colIdx]);
	}

	/**
	 * 현재 행의 RecordSet의 double 값을 반환하는 메소드
	 * @param colIdx  column number, 첫번째 column은 1
	 * @return double
	 */
	public double getDouble(int colIdx) {
		return getDouble(currow, colNms[colIdx]);
	}

	/**
	 * 현재 행의 RecordSet의 Date 값을 반환하는 메소드
	 * YYYY-MM-DD 로 반환
	 * @param colIdx  column number, 첫번째 column은 1
	 * @return Date
	 */
	public Date getDate(int colIdx) {
		return getDate(currow, colNms[colIdx]);
	}

	/**
	 * 현재 형의 RecordSet의 Timestamp 값을 반환하는 메소드
	 * @param colIdx
	 * @return Timestamp
	 */
	public Timestamp getTimestamp(int colIdx) {
		return getTimestamp(currow, colNms[colIdx]);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 column 데이터를 구한다
	 * @param colName	읽고자 하는 column 이름
	 * @return column data
	 */
	public Object get(String colName) {
		return get(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 int형 column 데이터를 구한다
	 * @param colName 읽고자 하는 column 이름
	 * @return int
	 */
	public int getInt(String colName) {
		return getInt(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 int형 column 데이터를 구한다
	 * @param colName 읽고자 하는 column 이름
	 * @return Integer
	 */
	public Integer getInteger(String colName) {
		Integer returnValue = null;
		returnValue = Integer.valueOf(getInt(currow, colName));
		return returnValue;
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 long형 column 데이터를 구한다
	 *
	 * @param colName 읽고자 하는 column 이름
	 *
	 * @return long
	 */
	public long getLong(String colName) {
		return getLong(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 String형 column 데이터를 구한다
	 *
	 * @param colName 읽고자 하는 column 이름
	 *
	 * @return String
	 */
	public String getString(String colName) {
		return getString(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 float형 column 데이터를 구한다
	 * @param colName 읽고자 하는 column 이름
	 * @return float
	 */
	public float getFloat(String colName) {
		return getFloat(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 double형 column 데이터를 구한다
	 * @param colName 읽고자 하는 column 이름
	 * @return double
	 */
	public double getDouble(String colName) {
		return getDouble(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 Date형 column 데이터를 구한다
	 * YYYY-MM-DD로 반환
	 * @param colName 읽고자 하는 column 이름
	 * @return Date
	 */
	public Date getDate(String colName) {
		return getDate(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 Date형 column 데이터를 구한다
	 * YYYY-MM-DD로 반환
	 * @param colName 읽고자 하는 column 이름
	 * @return Date
	 */
	public Timestamp getTimestamp(String colName) {
		return getTimestamp(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 column의 위치를 구한다.
	 * @param colName column 이름
	 * @return column index, 찾지 못하면 -1
	 */
	public int findColumn(String colName) {
		if (colName == null || colNms == null) {
			throw new RuntimeException("name or column_keys is null ");
		}
		int count = colNms.length;
		for (int i = 0; i < count; i++) {
			if (colName.equals(colNms[i])) {
				return i + 1;
			}
		}
		throw new RuntimeException("name : " + colName + " is not found ");
	}

	/**
	 * 레코드 수가 0 인지 check
	 * @return boolean True if there are no records in this object, false otherwise
	 */
	public boolean isEmpty() {
		if (rows.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 이터레이터를 반환한다.
	 */
	@Override
	public Iterator<RecordMap> iterator() {
		return rows.iterator();
	}
}