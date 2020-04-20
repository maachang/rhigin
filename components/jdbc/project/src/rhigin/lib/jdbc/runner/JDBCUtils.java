package rhigin.lib.jdbc.runner;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;

import rhigin.lib.jdbc.runner.JDBCConnect.Time12;
import rhigin.scripts.RhiginWrapUtil;
import rhigin.util.ByteArrayIO;
import rhigin.util.Converter;
import rhigin.util.ObjectList;
import rhigin.util.Time12SequenceId;

/**
 * JDBCユーティリティ.
 */
public final class JDBCUtils {
	private JDBCUtils() {
	}

	/**
	 * Time12のシーケンスIDを付与.
	 * 
	 * @param conns
	 * @param args
	 * @return
	 */
	public static final Object[] appendSequenceTime12(JDBCConnect conns, Object[] args) {
		if(conns.sequence == null) {
			return args;
		}
		final int len = args == null ? 0 : args.length;
		for(int i = 0; i < len; i ++) {
			if(args[i] instanceof Time12) {
				args[i] = Time12SequenceId.toString(conns.sequence.next());
			}
		}
		return args;
	}

	/**
	 * PreparedStatementパラメータをセット.
	 * 
	 * @param pre    対象のステートメントを設定します.
	 * @param meta   パラメータメタデータを設定します.
	 * @param params 対象のパラメータを設定します.
	 */
	public static final void preParams(final PreparedStatement pre, final ParameterMetaData meta, final Object[] params)
			throws Exception {
		int len = params.length;
		for (int i = 0; i < len; i++) {
			putParam(i + 1, pre, meta, params[i]);
		}
	}

	/**
	 * 1つのパラメータセット.
	 */
	public static final void putParam(final int no, final PreparedStatement pre, final ParameterMetaData meta, Object v)
			throws Exception {
		// rhigin向けのアンラップ処理.
		v = RhiginWrapUtil.unwrap(v);

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
			} else if(Converter.isNumeric(v)) {
				pre.setBoolean(no, Converter.convertInt(v) == 1);
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
			if (v instanceof Boolean) {
				pre.setFloat(no, (((Boolean) v).booleanValue()) ? 1.0f : 0.0f);
			} else if (v instanceof Float) {
				pre.setFloat(no, (Float) v);
			} else {
				pre.setFloat(no, Converter.convertFloat(v));
			}
			break;
		case Types.DOUBLE:
			if (v instanceof Boolean) {
				pre.setDouble(no, (((Boolean) v).booleanValue()) ? 1.0d : 0.0d);
			} else if (v instanceof Double) {
				pre.setDouble(no, (Double) v);
			} else {
				pre.setDouble(no, Converter.convertDouble(v));
			}
			break;
		case Types.NUMERIC:
		case Types.DECIMAL:
			if (v instanceof Boolean) {
				pre.setBigDecimal(no, new BigDecimal((((Boolean) v).booleanValue()) ? "1.0" : "0.0"));
			} if (v instanceof BigDecimal) {
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
			} else if(Converter.isNumeric(v)) {
				// javascriptの場合、1 と設定しても 1.0 となるので、その場合は整数でセット.
				final Long n = Converter.convertLong(v);
				if(Converter.convertDouble(n).equals(Converter.convertDouble(v))) {
					pre.setString(no, Converter.convertString(n));
				} else {
					pre.setString(no, Converter.convertString(v));
				}
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
			} else if (v instanceof String) {
				pre.setBytes(no, ((String) v).getBytes("UTF8"));
			} else {
				pre.setBytes(no, ("" + v).getBytes("UTF8"));
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
	public static final Object getResultColumn(final ResultSet result, final int type, final int no) throws Exception {
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
		// rhigin向けのラップ処理.
		return RhiginWrapUtil.wrapJavaObject(data);
	}

	/** 小文字大文字差分. **/
	private static final int _Aa = (int) 'a' - (int) 'A';

	public static final String convertJavaNameByDBName(final String name) {
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
	
	/**
	 * SQLの終端が存在する場合.
	 * @param sql
	 * @return
	 */
	public static final boolean endSqlExists(final String sql) {
		return Converter.indexOfNoCote(sql, ";", 0) != -1;
	}
	
	/**
	 * 複数命令のSQL文を１つのSQLに変換してリスト化.
	 * @param sql
	 * @return
	 */
	public static final List<String> sqlList(final String sql) {
		
		// たとえば
		// select * from hoge; select * frim moge;
		// ような場合は、
		// (1) select * from hoge;
		// (2) select * from moge;
		// と言う感じで分ける.
		
		int p;
		int b = 0;
		String s;
		List<String> ret = new ObjectList<String>();
		while((p = Converter.indexOfNoCote(sql, ";", b)) != -1) {
			if(p != b) {
				s = sql.substring(b, p+1).trim();
				if(!s.isEmpty()) {
					ret.add(s);
				}
			}
			b = p + 1;
		}
		if(b != sql.length() && !(s = sql.substring(b).trim()).isEmpty()) {
			ret.add(s);
		}
		return ret;
	}
	
	/** 不明なSQL、多分実行出来ないSQL文. **/
	public static final int SQL_UNKNOWN = 0;
	
	/** SELECT文. **/
	public static final int SQL_SELECT = 1;
	
	/** INSERT文. **/
	public static final int SQL_INSERT = 2;
	
	/** 他SQL文. **/
	public static final int SQL_SQL = 3;
	
	// SELECT コマンド.
	private static final String[] SELECT_CMD = new String[] {"select", "SELECT"};
	private static final int SELECT_CMD_LENGTH = 6;
	
	// INSERT コマンド.
	private static final String[] INSERT_CMD = new String[] {"insert", "INSERT"};
	private static final int INSERT_CMD_LENGTH = 6;
	
	/**
	 * SQLタイプを取得.
	 * @param sql
	 * @return int [0] 不明, [1] SELECT文 [2] INSERT文 [3] その他SQL文.
	 */
	public static final int sqlType(final String sql) {
		char c;
		int cnt = 0;
		int type = SQL_UNKNOWN;
		final int len = sql.length();
		for(int i = 0; i < len; i ++) {
			c = sql.charAt(i);
			if(type == SQL_UNKNOWN) {
				if(c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == ';') {
					continue;
				} else if(c == 's' || c == 'S') {
					type = SQL_SELECT; // selectの可能性.
				} else if(c == 'i' || c == 'I') {
					type = SQL_INSERT; // insertの可能性.
				} else {
					// その他SQLの可能性.
					return SQL_SQL;
				}
				cnt = 1;
			// SELECTの可能性.
			} else if(type == SQL_SELECT) {
				if(SELECT_CMD[0].charAt(cnt) != c && SELECT_CMD[1].charAt(cnt) != c) {
					return SQL_SQL;
				} else if(SELECT_CMD_LENGTH == cnt + 1) {
					return SQL_SELECT;
				}
				cnt ++;
			// INSERTの可能性.
			} else if(type == SQL_INSERT) {
				if(INSERT_CMD[0].charAt(cnt) != c && INSERT_CMD[1].charAt(cnt) != c) {
					return SQL_SQL;
				} else if(INSERT_CMD_LENGTH == cnt + 1) {
					return SQL_INSERT;
				}
				cnt ++;
			}
		}
		return type != SQL_UNKNOWN ? SQL_SQL : SQL_UNKNOWN;
	}
}
