package framework.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexacro.xapi.data.DataSet;
import com.nexacro.xapi.data.DataSetList;
import com.nexacro.xapi.data.DataTypes;
import com.nexacro.xapi.data.PlatformData;
import com.nexacro.xapi.data.VariableList;
import com.nexacro.xapi.tx.HttpPlatformRequest;
import com.nexacro.xapi.tx.HttpPlatformResponse;
import com.nexacro.xapi.tx.PlatformException;
import com.nexacro.xapi.tx.PlatformRequest;
import com.nexacro.xapi.tx.PlatformResponse;
import com.nexacro.xapi.tx.PlatformType;

import framework.action.Params;
import framework.db.RecordMap;
import framework.db.RecordSet;

/**
 * 넥사크로플랫폼을 이용하여 개발할 때 이용할 수 있는 유틸리티 클래스이다.
 */
public class NexacroUtil {
	private static final Log logger = LogFactory.getLog(framework.util.NexacroUtil.class);

	/**
	 * 미압축 방식의 바이너리 송수신 형식
	 */
	public static final String BIN = PlatformType.CONTENT_TYPE_BINARY;

	/**
	 * 미압축 방식의 XML 송수신 형식
	 */
	public static final String XML = PlatformType.CONTENT_TYPE_XML;

	/**
	 * Zlib 압축 방식의 바이너리 송수신 형식
	 */
	public static final String ZLIB = PlatformType.PROTOCOL_TYPE_ZLIB;

	/**
	 * 미압축 방식의 SSV 송수신 형식
	 */
	public static final String SSV = PlatformType.CONTENT_TYPE_SSV;

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private NexacroUtil() {
	}

	/**
	 * RecordSet을 넥사크로플랫폼 데이타셋(명칭은 datasetName 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs를 넥사크로플랫폼 데이터셋(명칭은 result)으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, "result", rs, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param datasetName 데이타셋 이름
	 * @param rs 넥사크로플랫폼 데이타셋으로 변환할 RecordSet 객체
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, String datasetName, RecordSet rs, String platformType) {
		String[] datasetNameArray = new String[] { datasetName };
		RecordSet[] rsArray = new RecordSet[] { rs };
		return render(response, datasetNameArray, rsArray, platformType);
	}

	/**
	 * RecordSet을 넥사크로플랫폼 데이타셋(명칭은 datasetName 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs를 넥사크로플랫폼 데이터셋(명칭은 result)으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, vl, "result", rs, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param vl 넥사크로플랫폼 VariableList 객체
	 * @param datasetName 데이타셋 이름
	 * @param rs 넥사크로플랫폼 데이타셋으로 변환할 RecordSet 객체
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, VariableList vl, String datasetName, RecordSet rs, String platformType) {
		String[] datasetNameArray = new String[] { datasetName };
		RecordSet[] rsArray = new RecordSet[] { rs };
		return render(response, vl, datasetNameArray, rsArray, platformType);
	}

	/**
	 * RecordSet을 넥사크로플랫폼 데이타셋(명칭은 datasetNameArray 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs1과 rs2를 넥사크로플랫폼 데이터셋으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, new String[] { "result1", "result2" }, new RecordSet[] { rs1, rs2 }, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param datasetNameArray 데이타셋 이름 배열
	 * @param rsArray 넥사크로플랫폼 데이타셋으로 변환할 RecordSet 객체 배열
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, String[] datasetNameArray, RecordSet[] rsArray, String platformType) {
		VariableList vl = new VariableList();
		vl.add("ErrorCode", "0");
		vl.add("ErrorMsg", "SUCC");
		return render(response, vl, datasetNameArray, rsArray, platformType);
	}

	/**
	 * RecordSet을 넥사크로플랫폼 데이타셋(명칭은 datasetNameArray 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs1과 rs2를 넥사크로플랫폼 데이터셋으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, vl, new String[] { "result1", "result2" }, new RecordSet[] { rs1, rs2 }, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param vl 넥사크로플랫폼 VariableList 객체
	 * @param datasetNameArray 데이타셋 이름 배열
	 * @param rsArray 넥사크로플랫폼 데이타셋으로 변환할 RecordSet 객체 배열
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, VariableList vl, String[] datasetNameArray, RecordSet[] rsArray, String platformType) {
		if (response == null || datasetNameArray == null || rsArray == null) {
			return 0;
		}
		if (datasetNameArray.length != rsArray.length) {
			throw new IllegalArgumentException("Dataset이름 갯수와 RecordSet갯수가 일치하지 않습니다.");
		}
		int rowCount = 0;
		DataSetList dl = new DataSetList();
		for (int i = 0, len = rsArray.length; i < len; i++) {
			DataSet dSet = new DataSet(datasetNameArray[i]);
			rowCount += appendDataset(dSet, rsArray[i]);
			dl.add(dSet);
		}
		sendData(response, vl, dl, platformType);
		return rowCount;
	}

	/**
	 * ResultSet을 넥사크로플랫폼 데이타셋(명칭은 datasetName 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs를 넥사크로플랫폼 데이터셋(명칭은 result)으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, "result", rs, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param datasetName 데이타셋 이름
	 * @param rs 넥사크로플랫폼 데이타셋으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, String datasetName, ResultSet rs, String platformType) {
		String[] datasetNameArray = new String[] { datasetName };
		ResultSet[] rsArray = new ResultSet[] { rs };
		return render(response, datasetNameArray, rsArray, platformType);
	}

	/**
	 * ResultSet을 넥사크로플랫폼 데이타셋(명칭은 datasetName 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs를 넥사크로플랫폼 데이터셋(명칭은 result)으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, vl, "result", rs, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param vl 넥사크로플랫폼 VariableList 객체
	 * @param datasetName 데이타셋 이름
	 * @param rs 넥사크로플랫폼 데이타셋으로 변환할 ResultSet 객체, ResultSet 객체는 자동으로 close 된다.
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, VariableList vl, String datasetName, ResultSet rs, String platformType) {
		String[] datasetNameArray = new String[] { datasetName };
		ResultSet[] rsArray = new ResultSet[] { rs };
		return render(response, vl, datasetNameArray, rsArray, platformType);
	}

	/**
	 * ResultSet을 넥사크로플랫폼 데이타셋(명칭은 datasetNameArray 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs1과 rs2를 넥사크로플랫폼 데이터셋으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, new String[] { "result1", "result2" }, new ResultSet[] { rs1, rs2 }, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param datasetNameArray 데이타셋 이름 배열
	 * @param rsArray 넥사크로플랫폼 데이타셋으로 변환할 ResultSet 객체 배열, ResultSet 객체는 자동으로 close 된다.
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, String[] datasetNameArray, ResultSet[] rsArray, String platformType) {
		VariableList vl = new VariableList();
		vl.add("ErrorCode", "0");
		vl.add("ErrorMsg", "SUCC");
		return render(response, vl, datasetNameArray, rsArray, platformType);
	}

	/**
	 * ResultSet을 넥사크로플랫폼 데이타셋(명칭은 datasetNameArray 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) rs1과 rs2를 넥사크로플랫폼 데이터셋으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, vl, new String[] { "result1", "result2" }, new ResultSet[] { rs1, rs2 }, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param vl 넥사크로플랫폼 VariableList 객체
	 * @param datasetNameArray 데이타셋 이름 배열
	 * @param rsArray 넥사크로플랫폼 데이타셋으로 변환할 ResultSet 객체 배열, ResultSet 객체는 자동으로 close 된다.
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, VariableList vl, String[] datasetNameArray, ResultSet[] rsArray, String platformType) {
		if (response == null || datasetNameArray == null || rsArray == null) {
			return 0;
		}
		if (datasetNameArray.length != rsArray.length) {
			throw new IllegalArgumentException("Dataset이름 갯수와 ResultSet갯수가 일치하지 않습니다.");
		}
		int rowCount = 0;
		DataSetList dl = new DataSetList();
		for (int i = 0, len = rsArray.length; i < len; i++) {
			DataSet dSet = new DataSet(datasetNameArray[i]);
			rowCount += appendDataset(dSet, rsArray[i]);
			dl.add(dSet);
		}
		sendData(response, vl, dl, platformType);
		return rowCount;
	}

	/**
	 * Map객체를 넥사크로플랫폼 데이타셋(명칭은 datasetName 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) map을 넥사크로플랫폼 데이터셋(명칭은 result)으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, "result", map, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param datasetName 데이타셋 이름
	 * @param map 넥사크로플랫폼 데이타셋으로 변환할 Map객체
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, String datasetName, RecordMap map, String platformType) {
		return render(response, datasetName, Arrays.asList(map), platformType);
	}

	/**
	 * Map객체를 넥사크로플랫폼 데이타셋(명칭은 datasetName 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) map을 넥사크로플랫폼 데이터셋(명칭은 result)으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, vl, "result", map, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param vl 넥사크로플랫폼 VariableList 객체
	 * @param datasetName 데이타셋 이름
	 * @param map 넥사크로플랫폼 데이타셋으로 변환할 Map객체
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, VariableList vl, String datasetName, RecordMap map, String platformType) {
		return render(response, vl, datasetName, Arrays.asList(map), platformType);
	}

	/**
	 * List객체를 넥사크로플랫폼 데이타셋(명칭은 datasetName 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) list를 넥사크로플랫폼 데이터셋(명칭은 result)으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, "result", list, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param datasetName 데이타셋 이름
	 * @param list 넥사크로플랫폼 데이타셋으로 변환할 List객체
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	@SuppressWarnings("unchecked")
	public static int render(HttpServletResponse response, String datasetName, List<RecordMap> list, String platformType) {
		String[] datasetNameArray = new String[] { datasetName };
		List<RecordMap>[] listArray = new List[] { list };
		return render(response, datasetNameArray, listArray, platformType);
	}

	/**
	 * List객체를 넥사크로플랫폼 데이타셋(명칭은 datasetName 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) list를 넥사크로플랫폼 데이터셋(명칭은 result)으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, vl, "result", list, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param vl 넥사크로플랫폼 VariableList 객체
	 * @param datasetName 데이타셋 이름
	 * @param list 넥사크로플랫폼 데이타셋으로 변환할 List객체
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	@SuppressWarnings("unchecked")
	public static int render(HttpServletResponse response, VariableList vl, String datasetName, List<RecordMap> list, String platformType) {
		String[] datasetNameArray = new String[] { datasetName };
		List<RecordMap>[] listArray = new List[] { list };
		return render(response, vl, datasetNameArray, listArray, platformType);
	}

	/**
	 * List객체를 넥사크로플랫폼 데이타셋(명칭은 datasetNameArray 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) list1과 list2를 넥사크로플랫폼 데이터셋으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, new String[] { "result1", "result2" }, new List<RecordMap>[] { list1, list2 }, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param datasetNameArray 데이타셋 이름 배열
	 * @param listArray 넥사크로플랫폼 데이타셋으로 변환할 List객체 배열
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, String[] datasetNameArray, List<RecordMap>[] listArray, String platformType) {
		VariableList vl = new VariableList();
		vl.add("ErrorCode", "0");
		vl.add("ErrorMsg", "SUCC");
		return render(response, vl, datasetNameArray, listArray, platformType);
	}

	/**
	 * List객체를 넥사크로플랫폼 데이타셋(명칭은 datasetNameArray 인자 값)으로 변환하여 응답객체로 전송한다.
	 * <br>
	 * ex) list1과 list2를 넥사크로플랫폼 데이터셋으로 변환하여 response로 XML 형식으로 전송하는 경우 : NexacroUtil.render(response, vl, new String[] { "result1", "result2" }, new List<RecordMap>[] { list1, list2 }, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param vl 넥사크로플랫폼 VariableList 객체
	 * @param datasetNameArray 데이타셋 이름 배열
	 * @param listArray 넥사크로플랫폼 데이타셋으로 변환할 List객체 배열
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 처리건수
	 */
	public static int render(HttpServletResponse response, VariableList vl, String[] datasetNameArray, List<RecordMap>[] listArray, String platformType) {
		if (response == null || datasetNameArray == null || listArray == null) {
			return 0;
		}
		if (datasetNameArray.length != listArray.length) {
			throw new IllegalArgumentException("Dataset이름 갯수와 리스트 갯수가 일치하지 않습니다.");
		}
		int rowCount = 0;
		DataSetList dl = new DataSetList();
		for (int i = 0, len = listArray.length; i < len; i++) {
			DataSet dSet = new DataSet(datasetNameArray[i]);
			rowCount += appendDataset(dSet, listArray[i]);
			dl.add(dSet);
		}
		sendData(response, vl, dl, platformType);
		return rowCount;
	}

	/**
	 * 해당 HttpServletRequest로 부터 PlatformRequest 반환받는다
	 * <br>
	 * ex) 요청객체로 부터 넥사크로플랫폼 요청객체를 구하는 경우 : PlatformRequest pReq = NexacroUtil.getPReq(request)
	 * @param request 클라이언트에서 요청된 Request 객체
	 * @return 요청객체에서 구한 PlatformRequest 객체
	 */
	public static PlatformRequest getPReq(HttpServletRequest request) {
		PlatformRequest inputPR = null;
		try {
			inputPR = new HttpPlatformRequest(request);
			inputPR.receiveData();
		} catch (PlatformException e) {
			throw new RuntimeException(e);
		}
		return inputPR;
	}

	/**
	 * 해당 HttpServletRequest로 부터 encoding 형식의 PlatformRequest 반환받는다
	 * <br>
	 * ex) 요청객체로 부터 utf-8 형식의 넥사크로플랫폼 요청객체를 구하는 경우 : PlatformRequest pReq = NexacroUtil.getPReq(request, "utf-8")
	 * @param request 클라이언트에서 요청된 Request 객체
	 * @param encoding 인코딩할 형식
	 * @return 요청객체에서 구한 PlatformRequest 객체
	 */
	public static PlatformRequest getPReq(HttpServletRequest request, String encoding) {
		PlatformRequest inputPR = null;
		try {
			inputPR = new HttpPlatformRequest(request, encoding);
			inputPR.receiveData();
		} catch (PlatformException e) {
			throw new RuntimeException(e);
		}
		return inputPR;
	}

	/**
	 * 해당 HttpServletResponse로 부터 PlatformResponse 반환받는다
	 * <br>
	 * ex) 응답객체로 부터 XML 송수신 형식의 넥사크로플랫폼 응답객체를 구하는 경우 : PlatformResponse pRes = NexacroUtil.getPRes(response, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @return 응답객체에서 구한 PlatformResponse 객체
	 */
	public static PlatformResponse getPRes(HttpServletResponse response, String platformType) {
		PlatformResponse inputPRes = null;
		inputPRes = new HttpPlatformResponse(response, platformType);
		return inputPRes;
	}

	/**
	 * 해당 HttpServletResponse로 부터 encoding 형식의 PlatformResponse 반환받는다
	 * <br>
	 * ex) 응답객체로 부터 utf-8 형식의 XML 송수신 형식의 넥사크로플랫폼 응답객체를 구하는 경우 : PlatformResponse pRes = NexacroUtil.getPRes(response, NexacroUtil.XML, "utf-8")
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 * @param encoding 인코딩할 형식
	 * @return 응답객체에서 구한 PlatformResponse 객체
	 */
	public static PlatformResponse getPRes(HttpServletResponse response, String platformType, String encoding) {
		PlatformResponse inputPRes = null;
		inputPRes = new HttpPlatformResponse(response, platformType, encoding);
		return inputPRes;
	}

	/**
	 * 해당 DataSet로 부터 Param를 반환받는다
	 * <br>
	 * ex) DataSet으로 부터 Param를 구하는 경우 : Param box = NexacroUtil.getParam(dSet)
	 * @param dSet Box로 변환할 DataSet 객체
	 * @return DataSet에서 구한 Box 객체
	 */
	public static Params getParam(DataSet dSet) {
		if (dSet.getRowCount() != 1) { // row 수가 1개가 아니면 잘못된 인자
			throw new IllegalArgumentException("row 수는 1개 이어야 합니다.");
		}
		Params box = new Params("nexacro");
		for (int i = 0, col = dSet.getColumnCount(); i < col; i++) {
			String key = dSet.getColumn(i).getName();
			box.put(key, new String[] { dSet.getString(0, i) });
		}
		return box;
	}

	/**
	 * VariableList와 DatasetList를 응답객체로 전송한다.
	 * <br>
	 * ex) vl과 dl을 response로 XML 형식으로 전송하는 경우 : NexacroUtil.sendData(response, vl, dl, NexacroUtil.XML)
	 * @param response 클라이언트로 응답할 Response 객체
	 * @param vl 넥사크로플랫폼 VariableList 객체
	 * @param dl 넥사크로플랫폼 DatasetList 객체
	 * @param platformType 송수신 형식 (NexacroUtil.BIN, NexacroUtil.ZLIB, NexacroUtil.XML, NexacroUtil.SSV)
	 */
	public static void sendData(HttpServletResponse response, VariableList vl, DataSetList dl, String platformType) {
		try {
			response.reset();
			PlatformResponse pResponse = getPRes(response, platformType, "utf-8");
			PlatformData pData = new PlatformData();
			pData.setVariableList(vl);
			pData.setDataSetList(dl);
			pResponse.setData(pData);
			pResponse.sendData();
		} catch (PlatformException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Dataset의 값을 추출하여 String 객체를 리턴한다.
	 * @param dSet 값을 추출할 Dataset
	 * @param row 추출할 행번호
	 * @param colName 추출할 열이름
	 * @return 추출된 값을 담고 있는 String 객체
	 */
	public static String getString(DataSet dSet, int row, String colName) {
		String str = dSet.getString(row, colName);
		if (str == null) {
			return "";
		}
		return str;
	}

	/**
	 * Dataset의 값을 추출하여 Double 객체를 리턴한다.
	 * @param dSet 값을 추출할 Dataset
	 * @param row 추출할 행번호
	 * @param colName 추출할 열이름
	 * @return 추출된 값을 담고 있는 Double 객체
	 */
	public static Double getDouble(DataSet dSet, int row, String colName) {
		String value = getString(dSet, row, colName).trim().replaceAll(",", "");
		if ("".equals(value)) {
			return Double.valueOf(0);
		}
		Double num = null;
		try {
			num = Double.valueOf(value);
		} catch (NumberFormatException e) {
			num = Double.valueOf(0);
		}
		return num;
	}

	/**
	 * Dataset의 값을 추출하여 Long 객체를 리턴한다.
	 * @param dSet 값을 추출할 Dataset
	 * @param row 추출할 행번호
	 * @param colName 추출할 열이름
	 * @return 추출된 값을 담고 있는 Long 객체
	 */
	public static Long getLong(DataSet dSet, int row, String colName) {
		Double value = getDouble(dSet, row, colName);
		return Long.valueOf(value.longValue());
	}

	/**
	 * Dataset의 값을 추출하여 Integer 객체를 리턴한다.
	 * @param dSet 값을 추출할 Dataset
	 * @param row 추출할 행번호
	 * @param colName 추출할 열이름
	 * @return 추출된 값을 담고 있는 Integer 객체
	 */
	public static Integer getInteger(DataSet dSet, int row, String colName) {
		Double value = getDouble(dSet, row, colName);
		return Integer.valueOf(value.intValue());
	}

	/**
	 * Dataset의 값을 추출하여 Float 객체를 리턴한다.
	 * @param dSet 값을 추출할 Dataset
	 * @param row 추출할 행번호
	 * @param colName 추출할 열이름
	 * @return 추출된 값을 담고 있는 Float 객체
	 */
	public static Float getFloat(DataSet dSet, int row, String colName) {
		return new Float(getDouble(dSet, row, colName).doubleValue());
	}

	/**
	 * Dataset의 값을 추출하여 BigDecimal 객체를 리턴한다.
	 * @param dSet 값을 추출할 Dataset
	 * @param row 추출할 행번호
	 * @param colName 추출할 열이름
	 * @return 추출된 값을 담고 있는 BigDecimal 객체
	 */
	public static BigDecimal getBigDecimal(DataSet dSet, int row, String colName) {
		String value = getString(dSet, row, colName).trim().replaceAll(",", "");
		if ("".equals(value)) {
			return BigDecimal.valueOf(0);
		}
		try {
			return new BigDecimal(value);
		} catch (NumberFormatException e) {
			return BigDecimal.valueOf(0);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////// Private 메소드

	/**
	 * RecordSet을 넥사크로플랫폼 데이타셋으로 변환한다.
	 */
	private static int appendDataset(DataSet dSet, List<RecordMap> mapList) {
		if (dSet == null || mapList == null) {
			return 0;
		}
		if (mapList.size() > 0) {
			for (Entry<String, Object> entry : mapList.get(0).entrySet()) {
				String key = entry.getKey().toLowerCase();
				Object value = entry.getValue();
				if (value instanceof Number) {
					dSet.addColumn(key, DataTypes.FLOAT);
				} else {
					dSet.addColumn(key, DataTypes.STRING);
				}
			}
		}
		int rowCount = 0;
		for (RecordMap map : mapList) {
			rowCount++;
			appendRow(dSet, map);
		}
		return rowCount;
	}

	/**
	 * RecordSet을 넥사크로플랫폼 데이타셋으로 변환한다.
	 */
	private static int appendDataset(DataSet dSet, RecordSet rs) {
		if (dSet == null || rs == null) {
			return 0;
		}
		String[] colNms = rs.getColumns();
		int[] colSize = rs.getColumnsSize();
		int[] colType = rs.getColumnsType();
		// 컬럼 레이아웃 셋팅
		if (colNms != null) {
			for (int c = 0; c < colNms.length; c++) {
				switch (colType[c]) {
				case Types.BIGINT:
				case Types.DECIMAL:
				case Types.DOUBLE:
				case Types.FLOAT:
				case Types.INTEGER:
				case Types.NUMERIC:
				case Types.REAL:
				case Types.SMALLINT:
				case Types.TINYINT:
					dSet.addColumn(colNms[c].toLowerCase(), DataTypes.FLOAT, colSize[c]);
					break;
				default:
					dSet.addColumn(colNms[c].toLowerCase(), DataTypes.STRING, colSize[c]);
					break;
				}
			}
		}
		rs.moveRow(0); // rs의 위치를 1번째로 이동
		int rowCount = 0;
		while (rs.nextRow()) {
			rowCount++;
			appendRow(dSet, rs, colNms);
		}
		return rowCount;
	}

	/**
	 * ResultSet을 넥사크로플랫폼 데이타셋으로 변환한다.
	 */
	private static int appendDataset(DataSet dSet, ResultSet rs) {
		if (dSet == null || rs == null) {
			return 0;
		}
		try {
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				int cnt = rsmd.getColumnCount();
				String[] colNms = new String[cnt];
				int[] colSize = new int[cnt];
				int[] colType = new int[cnt];
				for (int i = 1; i <= cnt; i++) {
					//Table의 Field 가 소문자 인것은 대문자로 변경처리
					colNms[i - 1] = rsmd.getColumnName(i).toUpperCase();
					//Field 의 정보 및 Size 추가
					colSize[i - 1] = rsmd.getColumnDisplaySize(i);
					// Field 의 타입 추가
					colType[i - 1] = rsmd.getColumnType(i);
				}
				// 컬럼 레이아웃 셋팅
				for (int c = 0; c < colNms.length; c++) {
					switch (colType[c]) {
					case Types.BIGINT:
					case Types.DECIMAL:
					case Types.DOUBLE:
					case Types.FLOAT:
					case Types.INTEGER:
					case Types.NUMERIC:
					case Types.REAL:
					case Types.SMALLINT:
					case Types.TINYINT:
						dSet.addColumn(colNms[c].toLowerCase(), DataTypes.FLOAT, colSize[c]);
						break;
					default:
						dSet.addColumn(colNms[c].toLowerCase(), DataTypes.STRING, colSize[c]);
						break;
					}
				}
				int rowCount = 0;
				while (rs.next()) {
					rowCount++;
					appendRow(dSet, rs, colNms);
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
	 * 넥사크로플랫폼 데이타셋에 RecordMap 한행 추가
	 */
	private static void appendRow(DataSet dSet, RecordMap map) {
		if (dSet == null || map == null) {
			return;
		}
		int row = dSet.newRow();
		for (Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey().toLowerCase();
			Object value = entry.getValue();
			if (value == null) {
				dSet.set(row, key, "");
			} else {
				if (value instanceof Number) {
					dSet.set(row, key, map.getDouble(key));
				} else {
					dSet.set(row, key, map.getString(key));
				}
			}
		}
	}

	/**
	 * 넥사크로플랫폼 데이타셋에 RecordSet 한행 추가
	 */
	private static void appendRow(DataSet dSet, RecordSet rs, String[] colNms) {
		if (dSet == null || rs == null || colNms == null) {
			return;
		}
		int row = dSet.newRow();
		for (int c = 0; c < colNms.length; c++) {
			Object value = rs.get(colNms[c]);
			if (value == null) {
				dSet.set(row, colNms[c].toLowerCase(), "");
			} else {
				if (value instanceof Number) {
					dSet.set(row, colNms[c].toLowerCase(), rs.getDouble(colNms[c]));
				} else {
					dSet.set(row, colNms[c].toLowerCase(), rs.getString(colNms[c]));
				}
			}
		}
	}

	/**
	 * 넥사크로플랫폼 데이타셋에 ResultSet 한행 추가
	 */
	private static void appendRow(DataSet dSet, ResultSet rs, String[] colNms) {
		if (dSet == null || rs == null || colNms == null) {
			return;
		}
		try {
			int row = dSet.newRow();
			for (int c = 0; c < colNms.length; c++) {
				Object value = rs.getObject(colNms[c]);
				if (value == null) {
					dSet.set(row, colNms[c].toLowerCase(), "");
				} else {
					if (value instanceof Number) {
						dSet.set(row, colNms[c].toLowerCase(), rs.getDouble(colNms[c]));
					} else {
						dSet.set(row, colNms[c].toLowerCase(), rs.getString(colNms[c]));
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}