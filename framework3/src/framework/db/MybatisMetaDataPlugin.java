package framework.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

/**
 * Mybatis 에서 ResultMetaData 정보를 얻어오기 위한 플러그인
 */
@Intercepts({ @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = { Statement.class }) })
public class MybatisMetaDataPlugin implements Interceptor {

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		List<Object> returnObj = new ArrayList<Object>();
		Object[] args = invocation.getArgs();
		Statement statement = (Statement) args[0];
		ResultSet rs = statement.getResultSet();
		while (rs == null) {
			if (statement.getMoreResults()) {
				rs = statement.getResultSet();
			} else {
				if (statement.getUpdateCount() == -1) {
					break;
				}
			}
		}
		if (rs != null) {
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
			RecordMap metaMap = new RecordMap();
			metaMap.setMetaData(true);
			metaMap.put("colNms", colNms);
			metaMap.put("colSize", colSize);
			metaMap.put("colType", colType);
			returnObj.add(metaMap);
		}
		Object results = invocation.proceed();
		if (results instanceof List) {
			returnObj.addAll((List<?>) results);
		} else {
			returnObj.add(results);
		}
		return returnObj;
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}
}