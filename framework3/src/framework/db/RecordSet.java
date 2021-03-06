package framework.db;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 데이터베이스 쿼리를 수행한 후 그 결과에 대한 접근 기반을 제공하는 클래스
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

	/**
	 * RecordSet의 생성자
	 * @param rs 쿼리 실행결과
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
		return Collections.unmodifiableList(rows);
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
		return getRowCount();
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
		return currow == 0;
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
	 * @param row cnt : start 1
	 * @return 이동 성공 여부
	 */
	public boolean moveRow(int row) {
		if (rows != null && rows.size() != 0 && row <= rows.size()) {
			currow = row;
			return true;
		}
		return false;
	}

	///////////////////////////////////////////
	// 현재 행 컬럼명으로 찾기
	///////////////////////////////////////////
	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 column 데이터를 구한다
	 * @param colName	읽고자 하는 column 이름
	 * @return column data
	 */
	public Object get(String colName) {
		return get(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 String 형 column 데이터를 구한다
	 * @param colName 읽고자 하는 column 이름
	 * @return String
	 */
	public String getString(String colName) {
		return getString(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 Integer 형 column 데이터를 구한다
	 * @param colName 읽고자 하는 column 이름
	 * @return Integer
	 */
	public Integer getInt(String colName) {
		return getInt(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 Integer 형 column 데이터를 구한다
	 * @param colName 읽고자 하는 column 이름
	 * @return Integer
	 */
	public Integer getInteger(String colName) {
		return getInteger(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 Long 형 column 데이터를 구한다
	 * @param colName 읽고자 하는 column 이름
	 * @return Long
	 */
	public Long getLong(String colName) {
		return getLong(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 Float 형 column 데이터를 구한다
	 * @param colName 읽고자 하는 column 이름
	 * @return Float
	 */
	public Float getFloat(String colName) {
		return getFloat(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 Double 형 column 데이터를 구한다
	 * @param colName 읽고자 하는 column 이름
	 * @return Double
	 */
	public Double getDouble(String colName) {
		return getDouble(currow, colName);
	}

	/**
	 * RecordSet의 Column 값을 BigDecimal 형으로 반환하는 메소드
	 * @param colName column name
	 * @return BigDecimal column data
	 */
	public BigDecimal getBigDecimal(String colName) {
		return getBigDecimal(currow, colName);
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
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 Date형 column 데이터를 구한다(형식: "yyyy-MM-dd HH:mm:ss")
	 * @param colName 읽고자 하는 column 이름
	 * @return Date
	 */
	public Date getDateTime(String colName) {
		return getDateTime(currow, colName);
	}

	/**
	 * 인자로 전해진 이름을 가지는 현재 pointing된 row의 Date형 column 데이터를 구한다
	 * @param colName 읽고자 하는 column 이름
	 * @param format date format
	 * @return Date
	 */
	public Date getDateFormat(String colName, String format) {
		return getDateFormat(currow, colName, format);
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

	///////////////////////////////////////////
	// 현재 행 컬럼 인덱스로 찾기
	///////////////////////////////////////////
	/**
	 * 현재 pointing 된 row의 column 데이터를 읽는다
	 * @param colIdx column number, 첫번째 column 은 1
	 * @return Object column data
	 */
	public Object get(int colIdx) {
		return get(currow, colNms[colIdx]);
	}

	/**
	 * 현재 pointing 된 row의 String 값을 반환하는 메소드
	 * @param colIdx column number, 첫번째 column 은 1
	 * @return String
	 */
	public String getString(int colIdx) {
		return getString(currow, colNms[colIdx]);
	}

	/**
	 * 현재행의 RecordSet의 Integer 값을 반환하는 메소드
	 * @param colIdx column number, 첫번째 column은 1
	 * @return Integer
	 */
	public Integer getInt(int colIdx) {
		return getInt(currow, colNms[colIdx]);
	}

	/**
	 * 현재행의 RecordSet의 Integer 값을 반환하는 메소드
	 * @param colIdx column number, 첫번째 column은 1
	 * @return Integer
	 */
	public Integer getInteger(int colIdx) {
		return getInteger(currow, colNms[colIdx]);
	}

	/**
	 * 현재 행의 RecordSet의 Long 값을 반환하는 메소드
	 * @param colIdx column number, 첫번째 column은 1
	 * @return Long
	 */
	public Long getLong(int colIdx) {
		return getLong(currow, colNms[colIdx]);
	}

	/**
	 * 현재 행의 RecordSet의 Float 값을 반환하는 메소드
	 * @param colIdx column number, 첫번째 column은 1
	 * @return Float
	 */
	public Float getFloat(int colIdx) {
		return getFloat(currow, colNms[colIdx]);
	}

	/**
	 * 현재 행의 RecordSet의 Double 값을 반환하는 메소드
	 * @param colIdx column number, 첫번째 column은 1
	 * @return Double
	 */
	public Double getDouble(int colIdx) {
		return getDouble(currow, colNms[colIdx]);
	}

	/**
	 * 현재 행의 RecordSet의 BigDecimal 값을 반환하는 메소드
	 * @param colIdx column number, 첫번째 column은 1
	 * @return BigDecimal
	 */
	public BigDecimal getBigDecimal(int colIdx) {
		return getBigDecimal(currow, colNms[colIdx]);
	}

	/**
	 * 현재 행의 RecordSet의 Date 값을 반환하는 메소드
	 * YYYY-MM-DD 로 반환
	 * @param colIdx column number, 첫번째 column은 1
	 * @return Date
	 */
	public Date getDate(int colIdx) {
		return getDate(currow, colNms[colIdx]);
	}

	/**
	 * 현재 행의 RecordSet의 Date 값을 반환하는 메소드(형식: "yyyy-MM-dd HH:mm:ss")
	 * @param colIdx column number, 첫번째 column은 1
	 * @return Date
	 */
	public Date getDateTime(int colIdx) {
		return getDateTime(currow, colNms[colIdx]);
	}

	/**
	 * 현재 행의 RecordSet의 Date 값을 반환하는 메소드
	 * @param colIdx column number, 첫번째 column은 1
	 * @param format date format
	 * @return Date
	 */
	public Date getDateFormat(int colIdx, String format) {
		return getDateFormat(currow, colNms[colIdx], format);
	}

	/**
	 * 현재 형의 RecordSet의 Timestamp 값을 반환하는 메소드
	 * @param colIdx column number, 첫번째 column은 1
	 * @return Timestamp
	 */
	public Timestamp getTimestamp(int colIdx) {
		return getTimestamp(currow, colNms[colIdx]);
	}

	///////////////////////////////////////////
	// 행번호와 컬럼명으로 찾기
	///////////////////////////////////////////
	/**
	 * Recordset 데이타를 얻어온다.
	 * @param row cnt : start 1
	 * @param colName column name
	 * @return Object  column data
	 */
	public Object get(int row, String colName) {
		return rows.get(row - 1).get(colName);
	}

	/**
	 * RecordSet의 column 값을 String 형으로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName  column name
	 * @return String  column data
	 */
	public String getString(int row, String colName) {
		return rows.get(row - 1).getString(colName);
	}

	/**
	 * RecordSet의 column 값을 Integer 형으로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return Integer  column data
	 */
	public Integer getInt(int row, String colName) {
		return rows.get(row - 1).getInt(colName);
	}

	/**
	 * RecordSet의 column 값을 Integer 형으로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return Integer  column data
	 */
	public Integer getInteger(int row, String colName) {
		return rows.get(row - 1).getInteger(colName);
	}

	/**
	 * RecordSet의 column 값을 Long 형으로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return Long  column data
	 */
	public Long getLong(int row, String colName) {
		return rows.get(row - 1).getLong(colName);
	}

	/**
	 * RecordSet의 column 값을 Float 형으로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return Float column data
	 */
	public Float getFloat(int row, String colName) {
		return rows.get(row - 1).getFloat(colName);
	}

	/**
	 * RecordSet의 Column 값을 Double 형으로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return Double column data
	 */
	public Double getDouble(int row, String colName) {
		return rows.get(row - 1).getDouble(colName);
	}

	/**
	 * RecordSet의 Column 값을 BigDecimal 형으로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return BigDecimal column data
	 */
	public BigDecimal getBigDecimal(int row, String colName) {
		return rows.get(row - 1).getBigDecimal(colName);
	}

	/**
	 * RecordSet의 column 값을 Date 형으로 반환하는 메소드(형식: "yyyy-MM-dd")
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return Date column data
	 */
	public Date getDate(int row, String colName) {
		return getDateFormat(row, colName, "yyyy-MM-dd");
	}

	/**
	 * RecordSet의 column 값을 Date 형으로 반환하는 메소드(형식: "yyyy-MM-dd HH:mm:ss")
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return Date column data
	 */
	public Date getDateTime(int row, String colName) {
		return getDateFormat(row, colName, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * RecordSet의 column 값을 Date형으로 반환하는 메소드
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @param format date format
	 * @return Date column data
	 */
	public Date getDateFormat(int row, String colName, String format) {
		return rows.get(row - 1).getDateFormat(colName, format);
	}

	/**
	 * RecordSet의 column 값을 Timestamp 형으로 반환하는 메소드
	 * YYYY-MM-DD 로 반환
	 * @param row  row number, 첫번째 row는 1
	 * @param colName column name
	 * @return Timestamp column data
	 */
	public Timestamp getTimestamp(int row, String colName) {
		return rows.get(row - 1).getTimestamp(colName);
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