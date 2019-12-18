package rhigin.lib.jdbc.runner;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import rhigin.util.ByteArrayIO;
import rhigin.util.Converter;

/**
 * JDBCユーティリティ.
 */
public final class JDBCUtils {
	private JDBCUtils() {
	}

	/**
	 * PreparedStatementパラメータをセット.
	 * 
	 * @param pre    対象のステートメントを設定します.
	 * @param meta   パラメータメタデータを設定します.
	 * @param params 対象のパラメータを設定します.
	 */
	public static final void preParams(final PreparedStatement pre, ParameterMetaData meta, Object[] params)
			throws Exception {
		int len = params.length;
		for (int i = 0; i < len; i++) {
			putParam(i + 1, pre, meta, params[i]);
		}
	}

	/**
	 * 1つのパラメータセット.
	 */
	public static final void putParam(final int no, final PreparedStatement pre, ParameterMetaData meta, Object v)
			throws Exception {

		// ParameterMetaDataがサポートしていない場合.
		if (meta == null) {

			// nullの場合、タイプが不明なので、無作法だがsetObjectにNULLをセット.
			if (v == null) {
				pre.setObject(no, null);
			} else if (v instanceof Boolean) {
				boolean b = ((Boolean) v).booleanValue();
				pre.setBoolean(no, b);
			} else if (v instanceof String) {
				pre.setString(no, (String) v);
			} else if (v instanceof Integer) {
				pre.setInt(no, (Integer) v);
			} else if (v instanceof Long) {
				pre.setLong(no, (Long) v);
			} else if (v instanceof Float) {
				pre.setFloat(no, (Float) v);
			} else if (v instanceof Double) {
				pre.setDouble(no, (Double) v);
			} else if (v instanceof BigDecimal) {
				pre.setBigDecimal(no, (BigDecimal) v);
			} else if (v instanceof java.util.Date) {
				if (v instanceof java.sql.Timestamp) {
					pre.setTimestamp(no, (java.sql.Timestamp) v);
				} else if (v instanceof java.sql.Time) {
					pre.setTime(no, (java.sql.Time) v);
				} else if (v instanceof java.sql.Date) {
					pre.setDate(no, (java.sql.Date) v);
				} else {
					pre.setTimestamp(no, new java.sql.Timestamp(((java.util.Date) v).getTime()));
				}
			} else if (v instanceof byte[]) {
				pre.setBytes(no, (byte[]) v);
			} else {
				pre.setObject(no, v);
			}

			return;
		}

		// ParameterMetaDataがサポートされている場合.
		int type = meta.getParameterType(no);

		// 情報がnullの場合はこちらのほうが行儀がよいのでこのように処理する.
		if (v == null) {
			pre.setNull(no, type);
			return;
		}

		// タイプ別で処理をセット.
		switch (type) {
		case Types.BOOLEAN:
			if (v instanceof Boolean) {
				pre.setBoolean(no, (Boolean) v);
			} else {
				pre.setBoolean(no, Converter.convertBool(v));
			}
			break;
		case Types.BIT:
		case Types.TINYINT:
		case Types.SMALLINT:
			if (v instanceof Boolean) {
				pre.setInt(no, (((Boolean) v).booleanValue()) ? 1 : 0);
			} else {
				pre.setInt(no, Converter.convertInt(v));
			}
			break;
		case Types.INTEGER:
		case Types.BIGINT:
			if (v instanceof Boolean) {
				pre.setLong(no, (((Boolean) v).booleanValue()) ? 1 : 0);
			} else if (v instanceof java.util.Date) {
				pre.setLong(no, ((java.util.Date) v).getTime());
			} else {
				pre.setLong(no, Converter.convertLong(v));
			}
			break;
		case Types.FLOAT:
		case Types.REAL:
			if (v instanceof Float) {
				pre.setFloat(no, (Float) v);
			} else {
				pre.setFloat(no, Converter.convertFloat(v));
			}
			break;
		case Types.DOUBLE:
			if (v instanceof Double) {
				pre.setDouble(no, (Double) v);
			} else {
				pre.setDouble(no, Converter.convertDouble(v));
			}
			break;
		case Types.NUMERIC:
		case Types.DECIMAL:
			if (v instanceof BigDecimal) {
				pre.setBigDecimal(no, (BigDecimal) v);
			} else {
				pre.setBigDecimal(no, new BigDecimal(Converter.convertDouble(v).toString()));
			}
			break;
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.DATALINK:
			if (v instanceof String) {
				pre.setString(no, (String) v);
			} else {
				pre.setString(no, Converter.convertString(v));
			}
			break;
		case Types.DATE:
			if (v instanceof java.sql.Date) {
				pre.setDate(no, (java.sql.Date) v);
			} else {
				pre.setDate(no, Converter.convertSqlDate(v));
			}
			break;
		case Types.TIME:
			if (v instanceof java.sql.Time) {
				pre.setTime(no, (java.sql.Time) v);
			} else {
				pre.setTime(no, Converter.convertSqlTime(v));
			}
			break;
		case Types.TIMESTAMP:
			if (v instanceof java.sql.Timestamp) {
				pre.setTimestamp(no, (java.sql.Timestamp) v);
			} else {
				pre.setTimestamp(no, Converter.convertSqlTimestamp(v));
			}
			break;
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
		case Types.BLOB:
			if (v instanceof byte[]) {
				pre.setBytes(no, (byte[]) v);
				break;
			} else if (v instanceof String) {
				pre.setBytes(no, ((String) v).getBytes("UTF8"));
				break;
			}
			break;
		case Types.JAVA_OBJECT:
			pre.setObject(no, v);
			break;
		default:
			pre.setObject(no, v);
			break;
		}

	}

	/**
	 * 結果のカラム情報を取得.
	 * 
	 * @param result 対象の結果オブジェクトを設定します.
	 * @param type   対象のSQLタイプを設定します.
	 * @param no     対象の項番を設定します. この番号は１から開始されます.
	 */
	public static final Object getResultColumn(ResultSet result, int type, int no) throws Exception {
		if (result.getObject(no) == null) {
			return null;
		}
		Object data = null;
		switch (type) {
		case Types.BOOLEAN:
			data = result.getBoolean(no);
			break;
		case Types.BIT:
		case Types.TINYINT:
			data = (int)(((Byte) result.getByte(no)).byteValue());
			break;
		case Types.SMALLINT:
			data = result.getInt(no);
			break;
		case Types.INTEGER:
			data = result.getLong(no);
			break;
		case Types.BIGINT:
			data = result.getLong(no);
			break;
		case Types.FLOAT:
		case Types.REAL:
			data = result.getFloat(no);
			break;
		case Types.DOUBLE:
			data = result.getDouble(no);
			break;
		case Types.NUMERIC:
		case Types.DECIMAL:
			data = result.getBigDecimal(no);
			break;
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			data = result.getString(no);
			break;
		case Types.DATE:
			data = result.getDate(no);
			break;
		case Types.TIME:
			data = result.getTime(no);
			break;
		case Types.TIMESTAMP:
			data = result.getTimestamp(no);
			break;
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			data = result.getBytes(no);
			break;
		case Types.BLOB:
			data = result.getBlob(no);
			break;
		case Types.DATALINK:
			data = result.getString(no);
			break;
		case Types.STRUCT:// 未サポート.
		case Types.CLOB:// 未サポート.
		case Types.NCLOB:// 未サポート.
		case Types.REF:// 未サポート.
			break;
		}
		// blob.
		if (data instanceof Blob) {
			InputStream b = new BufferedInputStream(((Blob) data).getBinaryStream());
			ByteArrayIO bo = new ByteArrayIO();
			byte[] bin = new byte[4096];
			int len;
			while (true) {
				if ((len = b.read(bin)) <= -1) {
					break;
				}
				if (len > 0) {
					bo.write(bin, 0, len);
				}
			}
			b.close();
			b = null;
			data = bo.toByteArray();
			bo.close();
			bo = null;
		}
		return data;
	}

	/** 小文字大文字差分. **/
	private static final int _Aa = (int) 'a' - (int) 'A';

	public static final String convertJavaNameByDBName(String name) {
		int cnt = 0;
		int len = name.length();
		char c = name.charAt(0);
		char[] buf = new char[len << 1];
		if (c >= 'A' && c <= 'Z') {
			buf[cnt++] = (char) (c + _Aa);
		} else {
			buf[cnt++] = c;
		}
		for (int i = 1; i < len; i++) {
			c = name.charAt(i);
			if (c >= 'A' && c <= 'Z') {
				buf[cnt] = '_';
				buf[cnt + 1] = (char) (c + _Aa);
				cnt += 2;
			} else {
				buf[cnt++] = c;
			}
		}
		return new String(buf, 0, cnt);
	}

	/**
	 * DB用データ名をJava用データ名に変換.
	 * 
	 * @param table テーブル名の変換の場合は[true]を設定します.
	 * @param name  対象の名前を設定します.
	 * @return String Java用の名前が返却されます.
	 */
	public static final String convertDBNameByJavaName(final boolean table, final String name) {
		char cp;
		char c = name.charAt(0);
		final int len = name.length();
		StringBuilder buf = new StringBuilder(len + (len >> 1));
		if (table && c >= 'a' && c <= 'z') {
			buf.append((char) (c - _Aa));
		} else {
			buf.append(c);
		}
		for (int i = 1; i < len; i++) {
			if ((c = name.charAt(i)) == '_') {
				if (i + 1 < len && ((cp = name.charAt(i + 1)) >= 'a' && cp <= 'z')) {
					buf.append((char) ('A' + (cp - 'a')));
					i++;
				}
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}
	
	/**
	 * SQLを整形.
	 * @param kind
	 * @param sql
	 * @return
	 */
	public static final String sql(final JDBCKind kind, String sql) {
		if(kind == null) {
			return sql;
		} else if(kind.isNotSemicolon()) {
			// 終端のセミコロンがあるとエラーになる場合.
			char c;
			for(int i = sql.length()-1 ; i >= 0 ; i --) {
				c = sql.charAt(i);
				if(c == ';') {
					sql = sql.substring(0, i);
					break;
				} else if(c == ' ' || c == '\t' || c == '\r' || c == '\n') {
					continue;
				}
				break;
			}
		}
		return sql;
	}
}
