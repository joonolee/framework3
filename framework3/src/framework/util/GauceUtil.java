package framework.util;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gauce.GauceDataColumn;
import com.gauce.GauceDataRow;
import com.gauce.GauceDataSet;
import com.gauce.GauceException;
import com.gauce.http.HttpGauceRequest;
import com.gauce.http.HttpGauceResponse;
import com.gauce.io.GauceInputStream;
import com.gauce.io.GauceOutputStream;

import framework.action.Params;
import framework.db.RecordSet;

/**
 * 가우스를 이용하여 개발할 때 이용할 수 있는 유틸리티 클래스이다.
 */
public final class GauceUtil {
	private static final Log logger = LogFactory.getLog(GauceUtil.class);

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private GauceUtil() {
	}

	/**
	 * RecordSet을 가우스 데이타셋으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs를 가우스 데이터셋으로 변환하여 response로 전송하는 경우 : GauceUtil.render(response, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs 가우스 데이타셋으로 변환할 RecordSet 객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, RecordSet rs) {
		return render(response, "", rs);
	}

	/**
	 * RecordSet을 가우스 데이타셋(명칭은 datasetName 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs를 가우스 데이터셋(명칭은 result)으로 변환하여 response로 전송하는 경우 : GauceUtil.render(response, "result", rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param datasetName 데이타셋 이름
	 * @param rs 가우스 데이타셋으로 변환할 RecordSet 객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, String datasetName, RecordSet rs) {
		String[] datasetNameArray = new String[] { datasetName };
		RecordSet[] rsArray = new RecordSet[] { rs };
		return render(response, datasetNameArray, rsArray);
	}

	/**
	 * RecordSet을 가우스 데이타셋(명칭은 datasetNameArray 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs1과 rs2를 가우스 데이터셋으로 변환하여 response로 전송하는 경우 : GauceUtil.render(response, new String[] { "result1", "result2" }, new RecordSet[] { rs1, rs2 })
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param datasetNameArray 데이타셋 이름 배열
	 * @param rsArray 가우스 데이타셋으로 변환할 RecordSet 객체 배열
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, String[] datasetNameArray, RecordSet[] rsArray) {
		if (response == null || datasetNameArray == null || rsArray == null) {
			return 0;
		}
		if (datasetNameArray.length != rsArray.length) {
			throw new IllegalArgumentException("DataSet이름 갯수와 RecordSet갯수가 일치하지 않습니다.");
		}
		int rowCount = 0;
		try {
			GauceOutputStream gos = getGOS(response);
			for (int i = 0, len = rsArray.length; i < len; i++) {
				GauceDataSet dSet = new GauceDataSet(datasetNameArray[i]);
				gos.fragment(dSet);
				rowCount += appendDataSet(dSet, rsArray[i]);
				gos.write(dSet);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * RecordSet을 인자로 넘어온 가우스 데이타셋으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs를 가우스 데이터셋으로 변환하여 response로 전송하는 경우 : GauceUtil.render(response, dSet, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param dSet 데이타셋
	 * @param rs 가우스 데이타셋으로 변환할 RecordSet 객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, GauceDataSet dSet, RecordSet rs) {
		GauceDataSet[] dSetArray = new GauceDataSet[] { dSet };
		RecordSet[] rsArray = new RecordSet[] { rs };
		return render(response, dSetArray, rsArray);
	}

	/**
	 * RecordSet을 인자로 넘어온 가우스 데이타셋으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs1과 rs2를 가우스 데이터셋으로 변환하여 response로 전송하는 경우 : GauceUtil.render(response, new GauceDataSet[] { dSet1, dSet2 }, new RecordSet[] { rs1, rs2 })
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param dSetArray 데이타셋 배열
	 * @param rsArray 가우스 데이타셋으로 변환할 RecordSet 객체 배열
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, GauceDataSet[] dSetArray, RecordSet[] rsArray) {
		if (response == null || dSetArray == null || rsArray == null) {
			return 0;
		}
		if (dSetArray.length != rsArray.length) {
			throw new IllegalArgumentException("DataSet 갯수와 RecordSet갯수가 일치하지 않습니다.");
		}
		int rowCount = 0;
		try {
			GauceOutputStream gos = getGOS(response);
			for (int i = 0, len = rsArray.length; i < len; i++) {
				GauceDataSet dSet = dSetArray[i];
				gos.fragment(dSet);
				rowCount += appendDataSet(dSet, rsArray[i]);
				gos.write(dSet);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * ResultSet을 가우스 데이타셋으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs를 가우스 데이터셋으로 변환하여 response로 전송하는 경우 : GauceUtil.render(response, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param rs 가우스 데이타셋으로 변환할 RecordSet 객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, ResultSet rs) {
		return render(response, "", rs);
	}

	/**
	 * ResultSet을 가우스 데이타셋(명칭은 datasetName 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs를 가우스 데이터셋(명칭은 result)으로 변환하여 response로 전송하는 경우 : GauceUtil.render(response, "result", rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param datasetName 데이타셋 이름
	 * @param rs 가우스 데이타셋으로 변환할 RecordSet 객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, String datasetName, ResultSet rs) {
		String[] datasetNameArray = new String[] { datasetName };
		ResultSet[] rsArray = new ResultSet[] { rs };
		return render(response, datasetNameArray, rsArray);
	}

	/**
	 * ResultSet을 가우스 데이타셋(명칭은 datasetNameArray 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs1과 rs2를 가우스 데이터셋으로 변환하여 response로 전송하는 경우 : GauceUtil.render(response, new String[] { "result1", "result2" }, new ResultSet[] { rs1, rs2 })
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param datasetNameArray 데이타셋 이름 배열
	 * @param rsArray 가우스 데이타셋으로 변환할 ResultSet 객체 배열, ResultSet 객체는 자동으로 close 된다.
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, String[] datasetNameArray, ResultSet[] rsArray) {
		if (response == null || datasetNameArray == null || rsArray == null) {
			return 0;
		}
		if (datasetNameArray.length != rsArray.length) {
			throw new IllegalArgumentException("DataSet이름 갯수와 RecordSet갯수가 일치하지 않습니다.");
		}
		int rowCount = 0;
		try {
			GauceOutputStream gos = getGOS(response);
			for (int i = 0, len = rsArray.length; i < len; i++) {
				GauceDataSet dSet = new GauceDataSet(datasetNameArray[i]);
				gos.fragment(dSet);
				rowCount += appendDataSet(dSet, rsArray[i]);
				gos.write(dSet);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * ResultSet을 인자로 넘어온 가우스 데이타셋으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs를 가우스 데이터셋으로 변환하여 response로 전송하는 경우 : GauceUtil.render(response, dSet, rs)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param dSet 데이타셋
	 * @param rs 가우스 데이타셋으로 변환할 RecordSet 객체
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, GauceDataSet dSet, ResultSet rs) {
		GauceDataSet[] dSetArray = new GauceDataSet[] { dSet };
		ResultSet[] rsArray = new ResultSet[] { rs };
		return render(response, dSetArray, rsArray);
	}

	/**
	 * ResultSet을 인자로 넘어온 가우스 데이타셋으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs1과 rs2를 가우스 데이터셋으로 변환하여 response로 전송하는 경우 : GauceUtil.render(response, new GauceDataSet[] { dSet1, dSet2 }, new ResultSet[] { rs1, rs2 })
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param dSetArray 데이타셋 이름 배열
	 * @param rsArray 가우스 데이타셋으로 변환할 ResultSet 객체 배열, ResultSet 객체는 자동으로 close 된다.
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, GauceDataSet[] dSetArray, ResultSet[] rsArray) {
		if (response == null || dSetArray == null || rsArray == null) {
			return 0;
		}
		if (dSetArray.length != rsArray.length) {
			throw new IllegalArgumentException("DataSet 갯수와 RecordSet갯수가 일치하지 않습니다.");
		}
		int rowCount = 0;
		try {
			GauceOutputStream gos = getGOS(response);
			for (int i = 0, len = rsArray.length; i < len; i++) {
				GauceDataSet dSet = dSetArray[i];
				gos.fragment(dSet);
				rowCount += appendDataSet(dSet, rsArray[i]);
				gos.write(dSet);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * 해당 HttpServletRequest로 부터 GauceInputStream을 반환받는다
	 * <br>
	 * ex) 요청객체로 부터 가우스 입력스트림을 구하는 경우 : GauceInputStream gis = GauceUtil.getGIS(request)
	 * @param request 클라이언트에서 요청된 Request 객체
	 * @return 요청객체에서 구한 GauceInputStream 객체
	 */
	public static GauceInputStream getGIS(HttpServletRequest request) {
		GauceInputStream inputGis = null;
		try {
			inputGis = ((HttpGauceRequest) request).getGauceInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return inputGis;
	}

	/**
	 * 해당 HttpServletResponse로 부터 GauceOutputStream을 반환받는다
	 * <br>
	 * ex) 응답객체로 부터 가우스 출력스트림을 구하는 경우 : GauceOutputStream gos = GauceUtil.getGOS(response)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @return 응답객체에서 구한 GauceOutputStream 객체
	 */
	public static GauceOutputStream getGOS(HttpServletResponse response) {
		GauceOutputStream inputGos = null;
		try {
			inputGos = ((HttpGauceResponse) response).getGauceOutputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return inputGos;
	}

	/**
	 * 세션객체가 null 인 경우 클라이언트에게 세션이 없음을 알리기 위해 예외를 설정한다.
	 * <br>
	 * ex) GauceUtil.setSessionException(getResponse())
	 * @param response response 클라이언트로 응답할 Response 객체
	 */
	public static void setSessionException(HttpServletResponse response) {
		try {
			((HttpGauceResponse) response).addException(new GauceException("SESSION", "0000", "OUT"));
			((HttpGauceResponse) response).getGauceOutputStream().close();
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	/**
	 * 클라이언트에게 가우스 예외를 설정한다.
	 * <br>
	 * ex) GauceUtil.setException(new GauceException("Native", "9999", e.toString()), getResponse())
	 * @param exception 클라이언트로 응답할 GauceException 객체
	 * @param response 클라이언트로 응답할 Response 객체
	 */
	public static void setException(GauceException exception, HttpServletResponse response) {
		try {
			((HttpGauceResponse) response).addException(exception);
			((HttpGauceResponse) response).getGauceOutputStream().close();
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	/**
	 * 해당 GauceDataSet로 부터 Box를 반환받는다
	 * <br>
	 * ex) GauceDataSet으로 부터 Box를 구하는 경우 : Box box = GauceUtil.getBox(dSet)
	 * @param dSet Box로 변환할 GauceDataSet 객체
	 * @return GauceDataSet에서 구한 Box 객체
	 */
	public static Params getParam(GauceDataSet dSet) {
		if (dSet.getDataRowCnt() != 1) { // row 수가 1개가 아니면 잘못된 인자
			throw new IllegalArgumentException("row 수는 1개 이어야 합니다.");
		}
		Params box = new Params("gauce");
		GauceDataRow dRow = dSet.getDataRow(0);
		for (GauceDataColumn column : dSet.getDataColumns()) {
			String key = column.getColName();
			box.put(key, new String[] { dRow.getColumnValue(dSet.indexOfColumn(key)).toString() });
		}
		return box;
	}

	//////////////////////////////////////////////////////////////////////////////////////// Private 메소드

	/**
	 * RecordSet을 가우스 데이타셋으로 변환한다.
	 */
	private static int appendDataSet(GauceDataSet dSet, RecordSet rs) {
		if (dSet == null || rs == null) {
			return 0;
		}
		String[] colNms = rs.getColumns();
		String[] colInfo = rs.getColumnsInfo();
		int[] colSize = rs.getColumnsSize();
		int[] colSizeReal = rs.getColumnsSizeReal();
		int[] colScale = rs.getColumnsScale();
		rs.moveRow(0); // rs의 위치를 1번째로 이동
		int rowCount = 0;
		while (rs.nextRow()) {
			rowCount++;
			appendRow(dSet, rs, colNms, colInfo, colSize, colSizeReal, colScale);
		}
		return rowCount;
	}

	/**
	 * ResultSet을 가우스 데이타셋으로 변환한다.
	 */
	private static int appendDataSet(GauceDataSet dSet, ResultSet rs) {
		if (dSet == null || rs == null) {
			return 0;
		}
		try {
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				int cnt = rsmd.getColumnCount();
				String[] colNms = new String[cnt];
				String[] colInfo = new String[cnt];
				int[] colSize = new int[cnt];
				int[] colSizeReal = new int[cnt];
				int[] colScale = new int[cnt];
				for (int i = 1; i <= cnt; i++) {
					colNms[i - 1] = rsmd.getColumnName(i).toLowerCase();
					colSize[i - 1] = rsmd.getColumnDisplaySize(i);
					colSizeReal[i - 1] = rsmd.getPrecision(i);
					colScale[i - 1] = rsmd.getScale(i);
					colInfo[i - 1] = rsmd.getColumnTypeName(i);
				}
				int rowCount = 0;
				while (rs.next()) {
					rowCount++;
					appendRow(dSet, rs, colNms, colInfo, colSize, colSizeReal, colScale);
				}
				return rowCount;
			} finally {
				Statement stmt = null;
				try {
					stmt = rs.getStatement();
				} catch (SQLException e) {
					logger.error("", e);
				}
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						logger.error("", e);
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						logger.error("", e);
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 가우스 데이타셋에 RecordSet 한행 추가
	 */
	private static void appendRow(GauceDataSet dSet, RecordSet rs, String[] colNms, String[] colInfo, int[] colSize, int[] colSizeReal, int[] colScale) {
		if (dSet == null || rs == null || colNms == null || colInfo == null || colSize == null || colSizeReal == null || colScale == null) {
			return;
		}
		for (int c = 0; c < colNms.length; c++) {
			Object value = rs.get(colNms[c]);
			if (value == null) {
				dSet.put(colNms[c], "", colSize[c], GauceDataColumn.TB_NORMAL);
			} else {
				if (value instanceof Number) {
					double dblSize = colSize[c];
					if (colSizeReal[c] > 0) {
						if (colScale[c] > 0) {
							dblSize = Double.parseDouble("" + colSizeReal[c] + "." + colScale[c]);
						} else {
							dblSize = colSizeReal[c];
						}
					}
					dSet.put(colNms[c], rs.getDouble(colNms[c]), dblSize, GauceDataColumn.TB_DECIMAL);
				} else {
					dSet.put(colNms[c], (rs.getString(colNms[c])), colSize[c], GauceDataColumn.TB_NORMAL);
				}
			}
		}
		dSet.heap();
	}

	/**
	 * 가우스 데이타셋에 ResultSet 한행 추가
	 */
	private static void appendRow(GauceDataSet dSet, ResultSet rs, String[] colNms, String[] colInfo, int[] colSize, int[] colSizeReal, int[] colScale) {
		if (dSet == null || rs == null || colNms == null || colInfo == null || colSize == null || colSizeReal == null || colScale == null) {
			return;
		}
		try {
			for (int c = 0; c < colNms.length; c++) {
				Object value = rs.getObject(colNms[c]);
				if (value == null) {
					dSet.put(colNms[c], "", colSize[c], GauceDataColumn.TB_NORMAL);
				} else {
					if (value instanceof Number) {
						double dblSize = colSize[c];
						if (colSizeReal[c] > 0) {
							if (colScale[c] > 0) {
								dblSize = Double.parseDouble("" + colSizeReal[c] + "." + colScale[c]);
							} else {
								dblSize = colSizeReal[c];
							}
						}
						dSet.put(colNms[c], rs.getDouble(colNms[c]), dblSize, GauceDataColumn.TB_DECIMAL);
					} else {
						dSet.put(colNms[c], (rs.getString(colNms[c])), colSize[c], GauceDataColumn.TB_NORMAL);
					}
				}
			}
			dSet.heap();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}