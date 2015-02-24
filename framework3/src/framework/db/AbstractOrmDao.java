package framework.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 테이블을 CRUD 하는 DAO를 작성할때 상속받는 클래스
 */
public abstract class AbstractOrmDao {
	protected static final Log logger = LogFactory.getLog(framework.db.AbstractOrmDao.class);
	protected DB db = null;

	public AbstractOrmDao(DB db) {
		super();
		this.db = db;
	}

	protected RecordSet executeQuery(String query) {
		return executeQuery(query, null);
	}

	protected RecordSet executeQuery(String query, Object[] where) {
		RecordSet rs = null;
		if (this.db == null) {
			logger.error("executeQuery : Can't open DB Connection!");
			return null;
		}
		SQLPreparedStatement pstmt = null;
		try {
			pstmt = this.db.createPrepareStatement(query);
			if (where != null) {
				pstmt.set(where);
			}
			rs = pstmt.executeQuery();
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return rs;
	}

	protected int execute(String query, Object[] values) {
		int result = 0;
		if (this.db == null) {
			logger.error("executeQuery : Can't open DB Connection!");
			return 0;
		}
		SQLPreparedStatement pstmt = null;
		try {
			pstmt = this.db.createPrepareStatement(query);
			pstmt.set(values);
			result = pstmt.executeUpdate();
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return result;
	}

	public int[] save(ValueObjectArray voArray) {
		int result[] = null;
		if (this.db == null) {
			logger.error("executeQuery : Can't open DB Connection!");
			return null;
		}
		if (voArray.size() == 0) {
			return new int[] { 0 };
		}
		result = new int[voArray.size()];
		int cnt = 0;
		cnt += _executeArray(voArray, ValueObjectArray.INSERT, result, cnt);
		cnt += _executeArray(voArray, ValueObjectArray.UPDATE, result, cnt);
		cnt += _executeArray(voArray, ValueObjectArray.DELETE, result, cnt);
		cnt += _executeArray(voArray, ValueObjectArray.UPDATE_ONLY, result, cnt);
		cnt += _executeArray(voArray, ValueObjectArray.USER_DELETE, result, cnt);
		cnt += _executeArray(voArray, ValueObjectArray.USER_UPDATE, result, cnt);
		return result;
	}

	private int _executeArray(ValueObjectArray vo, String type, int[] result, int cnt) {
		ValueObject[] values = null;
		values = vo.get(type);
		if (values == null || values.length == 0)
			return 0;
		SQLPreparedStatement pstmt = null;
		try {
			pstmt = this.db.createPrepareStatement(_getSaveSql(type, vo.getUserKeys(), vo.getUserFields()));
			for (int i = 0; i < values.length; i++) {
				pstmt.set(_getSaveValue(values[i], type, vo.getUserKeys(), vo.getUserFields()));
				result[cnt++] = pstmt.executeUpdate();
			}
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return values.length;
	}

	private String _getSaveSql(String type, String[] keys, String[] fields) {
		if (type.equals(ValueObjectArray.INSERT))
			return getInsertSql();
		else if (type.equals(ValueObjectArray.UPDATE))
			return getUpdateSql();
		else if (type.equals(ValueObjectArray.DELETE))
			return getDeleteSql();
		else if (type.equals(ValueObjectArray.UPDATE_ONLY))
			return getUpdateOnlySql(fields);
		else if (type.equals(ValueObjectArray.USER_UPDATE))
			return getUserUpdateOnlySql(fields, keys);
		else if (type.equals(ValueObjectArray.USER_DELETE))
			return getUserDeleteSql(keys);
		return null;
	}

	private Object[] _getSaveValue(ValueObject vo, String type, String[] keys, String[] fields) {
		if (type.equals(ValueObjectArray.INSERT))
			return vo.getInsertValue();
		else if (type.equals(ValueObjectArray.UPDATE))
			return vo.getUpdateValue();
		else if (type.equals(ValueObjectArray.DELETE))
			return vo.getPrimaryKeysValue();
		else if (type.equals(ValueObjectArray.UPDATE_ONLY))
			return vo.getUpdateOnlyValue(fields);
		else if (type.equals(ValueObjectArray.USER_UPDATE))
			return vo.getUserUpdateOnlyValue(fields, keys);
		else if (type.equals(ValueObjectArray.USER_DELETE))
			return vo.getUserDeleteValue(keys);
		return null;
	}

	public int insert(ValueObject vo) {
		return execute(getInsertSql(), vo.getInsertValue());
	}

	public int update(ValueObject vo) {
		return execute(getUpdateSql(), vo.getUpdateValue());
	}

	public int updateOnlyFields(ValueObject vo, String[] updateFieldName) {
		return execute(getUpdateOnlySql(updateFieldName), vo.getUpdateOnlyValue(updateFieldName));
	}

	public int userUpdate(ValueObject vo, String[] fields, String[] keyNames) {
		return execute(getUserUpdateOnlySql(fields, keyNames), vo.getUserUpdateOnlyValue(fields, keyNames));
	}

	public int delete(ValueObject vo) {
		return execute(getDeleteSql(), vo.getPrimaryKeysValue());
	}

	public int userDelete(ValueObject vo, String[] keyNames) {
		return execute(getUserDeleteSql(keyNames), vo.getUserDeleteValue(keyNames));
	}

	public abstract String getInsertSql();

	public abstract String getUpdateSql();

	public abstract String getUpdateOnlySql(String[] updateFieldNames);

	public abstract String getUserUpdateOnlySql(String[] updateFieldNames, String[] updateKeyNames);

	public abstract String getDeleteSql();

	public abstract String getUserDeleteSql(String[] deleteKeyNames);

	public abstract RecordSet select(ValueObject vo);
}