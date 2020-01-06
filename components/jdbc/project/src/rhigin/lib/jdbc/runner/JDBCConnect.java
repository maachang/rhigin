package rhigin.lib.jdbc.runner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.mozilla.javascript.Undefined;

import rhigin.lib.jdbc.pooling.AtomicPoolConnection;
import rhigin.scripts.JsMap;
import rhigin.util.BlankScriptable;
import rhigin.util.Converter;
import rhigin.util.Time12SequenceId;

/**
 * JDBC-Connect.
 */	
public class JDBCConnect {
	protected boolean closeFlag = false;
	protected JDBCCloseable closeable = null;
	protected Connection conn = null;
	protected JDBCKind kind = null;
	protected JDBCBatch batch = null;
	protected Time12SequenceId sequence = null;
	
	// 今回のコネクション専用のフェッチサイズ.
	protected int fetchSize = -1;
	
	/** 無効なシーケンスID. **/
	public static final String NON_SEQUENCE = "f///////////////";
	
	/**
	 * Time12SequenceID発行オブジェクト.
	 * 
	 * preparedStatementの引数にこのオブジェクトをセットすると、
	 * Time12SequenceIdのシーケンスIDを採番してくれます。
	 */
	public static final class Time12 implements BlankScriptable {
		public Time12() {}
		public boolean equals(Object o) {
			if(o == null) {
				return false;
			}
			return Time12.class.equals(o.getClass());
		}
		public Object getDefaultValue(Class<?> clazz) {
			return (clazz == null || String.class.equals(clazz)) ? toString() : Undefined.instance;
		}
		public String getClassName() {
			return "TIME12";
		}
		public String toString() {
			return "TIME12";
		}
	}
	
	/**
	 * コンストラクタ.
	 */
	protected JDBCConnect() {}
	
	/**
	 * 生成処理.
	 * @param c
	 * @param ac
	 * @param seq
	 * @return
	 */
	public static final JDBCConnect create(JDBCCloseable c, Time12SequenceId seq, Connection ac) {
		JDBCConnect ret = new JDBCConnect();
		ret.kind = (c instanceof AtomicPoolConnection) ? ((AtomicPoolConnection)c).getKind() : null;
		ret.batch = new JDBCBatch(ret);
		ret.closeable = c;
		ret.conn = ac;
		ret.sequence = seq;
		c.reg(ac);
		c.reg(ret.batch);
		return ret;
	}
	
	/**
	 * クローズ処理.
	 */
	public void close() {
		if(!closeFlag) {
			closeFlag = true;
			fetchSize = -1;
			if(batch != null) {
				batch.clear();
			}
			try {
				conn.rollback();
			} catch(Exception e) {}
			try {
				conn.close();
			} catch(Exception e) {}
		}
	}
	
	/**
	 * クローズ済みかチェック.
	 * @return boolean
	 */
	public boolean isClose() {
		// クローズ処理を直接呼び出している場合.
		if(closeFlag) {
			return true;
		}
		// コネクションにクローズ済みか問い合わせる.
		try {
			if(conn.isClosed()) {
				// クローズの場合は、このオブジェクトのクローズを呼び出す.
				close();
				return true;
			}
		} catch(Exception e) {
			// 例外が出てもクローズを呼び出す.
			close();
			return true;
		}
		return false;
	}
	
	// チェック処理.
	protected void check() {
		if(isClose()) {
			throw new JDBCException("Connection is already closed.");
		}
	}
	
	/**
	 * コミット.
	 * @return
	 */
	public JDBCConnect commit() {
		check();
		try {
			batch.execute();
			conn.commit();
		} catch(Exception e) {
			batch.clear();
			try {
				conn.rollback();
			} catch(Exception ee) {}
			if(e instanceof JDBCException) {
				throw (JDBCException)e;
			}
			throw new JDBCException(e);
		}
		return this;
	}
	
	/**
	 * ロールバック.
	 * @return
	 */
	public JDBCConnect rollback() {
		check();
		try {
			batch.clear();
			conn.rollback();
		} catch(JDBCException je) {
			throw je;
		} catch(Exception e) {
			throw new JDBCException(e);
		}
		return this;
	}
	
	/**
	 * クエリ実行.
	 * @param sql
	 * @param args
	 * @return
	 */
	public JDBCRow query(String sql, Object... args) {
		return execQuery(sql, 0, args);
	}
	
	/**
	 * クエリ実行をして先頭１行だけ取得.
	 * @param sql
	 * @param args
	 * @return
	 */
	public JDBCRow first(String sql, Object...args) {
		return execQuery(sql, 1, args);
	}
	
	// クエリ実行.
	private JDBCRow execQuery(String sql, int limit, Object[] args) {
		check();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(JDBCUtils.sql(kind, sql),
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			stmt.setMaxRows(limit > 0 && limit < 1024 ? (int)limit : 0);
			int fsize = fetchSize > 0 ? fetchSize : stmt.getFetchSize();
			if(fsize > 0 && limit > 0 && fsize > limit) {
				fsize = limit;
			}
			if(fsize > 0) {
				stmt.setFetchSize(fsize);
			}
			JDBCUtils.preParams(stmt, stmt.getParameterMetaData(),
				JDBCUtils.appendSequence(this, args));
			rs = stmt.executeQuery();
			JDBCRow ret = JDBCRow.create(rs, this, stmt);
			closeable.reg(stmt).reg(rs);
			return ret;
		} catch(Exception e) {
			if(rs != null) {
				try {
					rs.close();
				} catch(Exception ee) {}
			}
			if(stmt != null) {
				try {
					stmt.close();
				} catch(Exception ee) {}
			}
			throw sqlException(e, sql, args);
		}
	}
	
	/**
	 * 更新処理.
	 * @param sql
	 * @param args
	 * @return
	 */
	public int execUpdate(String sql, Object... args) {
		check();
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(JDBCUtils.sql(kind, sql), Statement.NO_GENERATED_KEYS);
			JDBCUtils.preParams(stmt, stmt.getParameterMetaData(),
				JDBCUtils.appendSequence(this, args));
			int ret = stmt.executeUpdate();
			stmt.close(); stmt = null;
			return ret;
		} catch(Exception e) {
			throw sqlException(e, sql, args);
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch(Exception e) {}
			}
		}
	}
	
	/**
	 * 行挿入処理.
	 * @param sql
	 * @param args
	 * @return
	 */
	public JDBCRow execInsert(String sql, Object... args) {
		check();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(JDBCUtils.sql(kind, sql), Statement.RETURN_GENERATED_KEYS);
			stmt.setMaxRows(1);
			JDBCUtils.preParams(stmt, stmt.getParameterMetaData(),
				JDBCUtils.appendSequence(this, args));
			stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			JDBCRow ret = JDBCRow.create(rs, this, stmt);
			closeable.reg(stmt).reg(rs);
			return ret;
		} catch(Exception e) {
			if(rs != null) {
				try {
					rs.close();
				} catch(Exception ee) {}
			}
			if(stmt != null) {
				try {
					stmt.close();
				} catch(Exception ee) {}
			}
			throw sqlException(e, sql, args);
		}
	}
	
	// SQL例外を出力.
	protected static final JDBCException sqlException(Exception err, String sql, Object... args) {
		final String src = err.getMessage();
		return new JDBCException(new StringBuffer(src == null ? "" : src)
			.append(src == null ? "" : " ")
			.append("sql: ")
			.append(sql)
			.append(" params: ")
			.append(args == null || args.length == 0 ? "[]": Arrays.deepToString(args))
			.toString(), err);
	}
	
	/**
	 * kind設定を取得.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<Object, Object> getKind() {
		check();
		if(conn instanceof AtomicPoolConnection) {
			return ((AtomicPoolConnection)conn).getKind().getMap();
		}
		return new JsMap();
	}
	
	/**
	 * AutoCommitかチェック.
	 * @return
	 */
	public boolean isAutoCommit() {
		check();
		try {
			return conn.getAutoCommit();
		} catch(Exception e) {
			if(e instanceof JDBCException) {
				throw (JDBCException)e;
			}
			throw new JDBCException(e);
		}
	}
	
	/**
	 * AutoCommitをセット.
	 * @param mode
	 * @return
	 */
	public JDBCConnect setAutoCommit(boolean mode) {
		check();
		try {
			conn.setAutoCommit(mode);
		} catch(Exception e) {
			if(e instanceof JDBCException) {
				throw (JDBCException)e;
			}
			throw new JDBCException(e);
		}
		return this;
	}
	
	/**
	 * フェッチサイズを取得.
	 * @return
	 */
	public int getFetchSize() {
		check();
		return fetchSize;
	}
	
	/**
	 * フェッチサイズを設定.
	 * @param size
	 */
	public JDBCConnect setFetchSize(int size) {
		check();
		fetchSize = size <= 0 ? 0 : size;
		return this;
	}
	
	/**
	 * 登録バッチ内容をクリア.
	 * @return
	 */
	public JDBCConnect clearBatch() {
		batch.clear();
		return this;
	}
	
	/**
	 * 登録バッチを実行.
	 * @return
	 */
	public int[] executeBatch() {
		return batch.execute();
	}
	
	/**
	 * バッチ実行内容を登録.
	 * @param sql
	 * @param args
	 * @return
	 */
	public JDBCConnect addBatch(String sql, Object... args) {
		batch.add(sql, args);
		return this;
	}
	
	/**
	 * バッチ登録数を取得.
	 * @return
	 */
	public int batchSize() {
		return batch.size();
	}
	
	/**
	 * バッチオブジェクトを取得.
	 * @return
	 */
	public JDBCBatch getBatch() {
		check();
		return batch;
	}
	
	/**
	 * １２バイト、１６文字（Base64）のシーケンスIDが利用可能かチェック.
	 * @return
	 */
	public boolean isTIME12() {
		return sequence != null;
	}
	
	/**
	 * １２バイト、１６文字（Base64）のシーケンスIDを取得.
	 * @return
	 */
	public String TIME12() {
		check();
		// シーケンスIDが利用出来ない場合は、最大値を返却.
		return sequence == null ? NON_SEQUENCE :
			Time12SequenceId.toString(sequence.next());
	}
	
	/**
	 * SelectSQL処理.
	 * @param list
	 * @return
	 */
	public Select select(Object... list) {
		return new Select(this, list);
	}
	
	/**
	 * DeleteSQL処理.
	 * @param list
	 * @return
	 */
	public Delete delete(Object... list) {
		return new Delete(this, null, list);
	}
	
	/**
	 * InsertSQL処理.
	 * @param list
	 * @return
	 */
	public Insert insert(Object... list) {
		return new Insert(this, null, list);
	}
	
	/**
	 * UpdateSQL処理.
	 * @param list
	 * @return
	 */
	public Update update(Object... list) {
		return new Update(this, null, list);
	}
	
	/**
	 * DeleteSQLバッチ処理.
	 * @param list
	 * @return
	 */
	public Delete deleteBatch(Object... list) {
		return batch.delete(list);
	}
	
	/**
	 * InsertSQLバッチ処理.
	 * @param list
	 * @return
	 */
	public Insert insertBatch(Object... list) {
		return batch.insert(list);
	}
	
	/**
	 * UpdateSQLバッチ処理.
	 * @param list
	 * @return
	 */
	public Update updateBatch(Object... list) {
		return batch.update(list);
	}
	
	/**
	 * Select用SQLオブジェクト.
	 */
	public static final class Select {
		private JDBCConnect conns;
		private String name;
		private Object[] columns;
		private String where;
		private String groupby;
		private String having;
		private String orderby;
		private String ather;
		private Integer offset;
		private Integer limit;
		
		/**
		 * コンストラクタ.
		 * @param c
		 * @param list
		 */
		protected Select(JDBCConnect c, Object[] list) {
			if(list != null) {
				int len = list.length;
				if(len >= 1) {
					this.name = "" + list[0];
					if(len > 1) {
						this.columns = new String[len-1];
						for(int i = 1, j = 0; i < len; i ++, j ++) {
							this.columns[j] = list[i];
						}
					}
				}
			}
			this.conns = c;
		}
		
		/**
		 * テーブル名を設定.
		 * @param table
		 * @return
		 */
		public Select name(String table) {
			this.name = table;
			return this;
		}
		
		/**
		 * 取得カラムを設定.
		 * @param col
		 * @return
		 */
		public Select columns(Object... col) {
			if(col != null && col.length > 0) {
				columns = col;
			} else {
				columns = null;
			}
			return this;
		}
		
		/**
		 * 取得項目条件を設定.
		 * @param where
		 * @return
		 */
		public Select where(Object... where) {
			if(where == null || where.length == 0) {
				this.where = null;
				return this;
			}
			if(where.length == 1) {
				String wstr = "" + where[0];
				if(wstr != null && !wstr.isEmpty()) {
					if(!wstr.trim().toLowerCase().startsWith("where")) {
						wstr = "WHERE " + wstr;
					}
				}
				this.where = wstr;
			} else {
				int len = where.length;
				StringBuilder buf = new StringBuilder("WHERE ");
				for(int i = 0; i < len; i ++) {
					if(i != 0) {
						buf.append(" AND ");
					}
					buf.append(where[i]);
				}
				this.where = buf.toString();
			}
			return this;
		}
		
		/**
		 * group byの設定.
		 * @param columns
		 * @return
		 */
		public Select groupby(Object... columns) {
			if(columns == null || columns.length <= 0) {
				this.groupby = null;
				return this;
			} else if(columns.length == 1) {
				String sql = "" + columns[0];
				if(!sql.trim().toLowerCase().startsWith("group by")) {
					sql = "GROUP BY " + sql;
				}
				this.groupby = sql;
				return this;
			}
			int len = columns.length;
			StringBuilder buf = new StringBuilder("GROUP BY ");
			for(int i = 0; i < len; i ++) {
				if(i != 0) {
					buf.append(", ");
				}
				buf.append(columns[i]);
			}
			this.groupby = buf.toString();
			return this;
		}
		
		/**
		 * group byの条件をセット.
		 * @param where
		 * @return
		 */
		public Select having(Object... having) {
			if(having == null || having.length == 0) {
				this.having = null;
				return this;
			}
			if(having.length == 1) {
				String hstr = "" + having[0];
				if(hstr != null && !hstr.isEmpty()) {
					if(!hstr.trim().toLowerCase().startsWith("having")) {
						hstr = "HAVING " + hstr;
					}
				}
				this.having = hstr;
			} else {
				int len = having.length;
				StringBuilder buf = new StringBuilder("HAVING ");
				for(int i = 0; i < len; i ++) {
					if(i != 0) {
						buf.append(" AND ");
					}
					buf.append(having[i]);
				}
				this.having = buf.toString();
			}
			return this;
		}
		
		/**
		 * order byの設定.
		 * @param columns [boolean] true=asc,false=desc [String] columns.
		 * @return
		 */
		public Select orderby(Object... columns) {
			if(columns == null || columns.length <= 0) {
				this.orderby = null;
				return this;
			} else if(columns.length == 1) {
				String sql = "" + columns[0];
				if(!sql.trim().toLowerCase().startsWith("order by")) {
					sql = "ORDER BY " + sql;
				}
				this.orderby = sql;
				return this;
			}
			boolean first = true;
			String before = null;
			StringBuilder buf = new StringBuilder("ORDER BY ");
			int len = columns.length;
			for(int i = 0; i < len; i++) {
				if(columns[i] instanceof Boolean) {
					if(before != null) {
						if(!first) {
							buf.append(", ");
						}
						if((Boolean)columns[i]) {
							buf.append(before).append(" ASC");
						} else {
							buf.append(before).append(" DESC");
						}
						first = false;
					}
					before = null;
				} else {
					before = "" + columns[i];
				}
			}
			if(before != null) {
				if(!first) {
					buf.append(", ");
				}
				buf.append(before).append(" ASC");
			}
			this.orderby = buf.toString();
			return this;
		}
		
		/**
		 * order by 以降のSQL設定.
		 * @param ather
		 * @return
		 */
		public Select ather(String ather) {
			this.ather = ather;
			return this;
		}
		
		/**
		 * offset limit を設定.
		 * @param args
		 * @return
		 */
		public Select range(Object... args) {
			if(args.length >= 1) {
				this.offset = Converter.convertInt(args[0]);
			}
			if(args.length >= 2) {
				this.limit = Converter.convertInt(args[1]);
			}
			return this;
		}
		
		/**
		 * オフセットを設定.
		 * @param offset
		 * @return
		 */
		public Select offset(Object offset) {
			this.offset = Converter.convertInt(offset);
			return this;
		}
		
		/**
		 * リミットを設定.
		 * @param limit
		 * @return
		 */
		public Select limit(Object limit) {
			this.limit = Converter.convertInt(limit);
			return this;
		}
		
		// SQLを作成.
		private String sql() {
			if(name == null || name.isEmpty()) {
				throw new JDBCException("Table name is not set.");
			}
			StringBuilder buf = new StringBuilder("SELECT ");
			if(columns != null && columns.length > 0) {
				int len = columns.length;
				for(int i = 0; i < len; i ++) {
					if(i != 0) {
						buf.append(", ");
					}
					buf.append(columns[i]);
				}
			} else {
				buf.append("*");
			}
			buf.append(" FROM ").append(name);
			if(where != null && !where.isEmpty()) {
				buf.append(" ").append(where);
			}
			if(groupby != null && !groupby.isEmpty()) {
				buf.append(" ").append(groupby);
				if(having != null && !having.isEmpty()) {
					buf.append(" ").append(having);
				}
			}
			if(orderby != null && !orderby.isEmpty()) {
				buf.append(" ").append(orderby);
			}
			if(ather != null && !ather.isEmpty()) {
				buf.append(" ").append(ather);
			}
			if(limit != null && limit >= 0) {
				buf.append(" LIMIT ").append(limit);
			}
			if(offset != null && offset >= 0) {
				buf.append(" OFFSET ").append(offset);
			}
			buf.append(";");
			return buf.toString();
		}
		
		/**
		 * 文字列を出力.
		 * @retrun
		 */
		public String toString() {
			try {
				return sql();
			} catch(Exception e) {
				return "error";
			}
		}
		
		/**
		 * 実行処理.
		 * @param args パラメータを設定します.
		 * @return
		 */
		public JDBCRow execute(Object... args) {
			return conns.execQuery(sql(), limit == null || limit < 0 ? 0 : limit, args);
		}
	}
	
	/**
	 * 削除用SQLオブジェクト.
	 */
	public static final class Delete {
		private JDBCConnect conns;
		private JDBCBatch batch;
		private String name;
		private String where;
		
		public Delete(JDBCConnect c, JDBCBatch b, Object... list) {
			this.conns = c;
			this.batch = b;
			this.name = list == null || list.length == 0 ?
				null : "" + list[0];
		}
		
		/**
		 * テーブル名を設定.
		 * @param table
		 * @return
		 */
		public Delete name(String table) {
			this.name = table;
			return this;
		}
		
		/**
		 * 取得項目条件を設定.
		 * @param where
		 * @return
		 */
		public Delete where(Object... where) {
			if(where == null || where.length == 0) {
				this.where = null;
				return this;
			} else if(where.length == 1) {
				String wstr = "" + where[0];
				if(wstr != null && !wstr.isEmpty()) {
					if(!wstr.trim().toLowerCase().startsWith("where")) {
						wstr = "WHERE " + wstr;
					}
				}
				this.where = wstr;
			} else {
				int len = where.length;
				StringBuilder buf = new StringBuilder("WHERE ");
				for(int i = 0; i < len; i ++) {
					if(i != 0) {
						buf.append(" AND ");
					}
					buf.append(where[i]);
				}
				this.where = buf.toString();
			}
			return this;
		}
		
		// SQLを作成.
		private String sql() {
			if(name == null || name.isEmpty()) {
				throw new JDBCException("Table name is not set.");
			}
			StringBuilder buf = new StringBuilder("DELETE ")
				.append(" FROM ").append(name);
			if(where != null && !where.isEmpty()) {
				buf.append(" ").append(where);
			}
			buf.append(";");
			return buf.toString();
		}
		
		/**
		 * 文字列を出力.
		 * @retrun
		 */
		public String toString() {
			try {
				return sql();
			} catch(Exception e) {
				return "error";
			}
		}
		
		/**
		 * 実行処理.
		 * @param args パラメータを設定します.
		 * @return
		 */
		public int execute(Object... args) {
			if(conns != null) {
				return conns.execUpdate(sql(), args);
			} else if(batch != null) {
				batch.add(sql(), args);
				return 0;
			} else {
				throw new JDBCException("Execution object is not set.");
			}
		}
	}
	
	/**
	 * Insert用SQLオブジェクト.
	 */
	public static final class Insert {
		private JDBCConnect conns;
		private JDBCBatch batch;
		private String name;
		
		public Insert(JDBCConnect c, JDBCBatch b, Object... list) {
			this.conns = c;
			this.batch = b;
			this.name = list == null || list.length == 0 ?
				null : "" + list[0];
		}
		
		/**
		 * テーブル名を設定.
		 * @param table
		 * @return
		 */
		public Insert name(String table) {
			this.name = table;
			return this;
		}
		
		// SQLを作成.
		private String sql(String[] columns) {
			if(name == null || name.isEmpty()) {
				throw new JDBCException("Table name is not set.");
			}
			StringBuilder buf = new StringBuilder("INSERT INTO ")
				.append(name).append("(");
			int len = columns.length;
			for(int i = 0; i < len; i ++) {
				if(i != 0) {
					buf.append(", ");
				}
				buf.append(columns[i]);
			}
			buf.append(") VALUES (");
			for(int i = 0; i < len; i ++) {
				if(i != 0) {
					buf.append(", ");
				}
				buf.append("?");
			}
			return buf.append(");").toString();
		}
		
		/**
		 * 文字列を出力.
		 * @retrun
		 */
		public String toString() {
			return "" + name;
		}
		
		/**
		 * 実行処理.
		 * @param args パラメータを設定します.
		 * @return
		 */
		@SuppressWarnings("rawtypes")
		public JDBCRow execute(Object... args) {
			if(args == null || args.length == 0) {
				throw new JDBCException("The parameter to be inserted does not exist.");
			}
			int cnt = 0;
			String[] cols = null;
			Object[] params = null;
			int len = args.length;
			if(len == 1 && args[0] instanceof Map) {
				Object key;
				Map m = (Map)args[0];
				len = m.size();
				cols = new String[len];
				params = new Object[len];
				Iterator it = m.keySet().iterator();
				while(it.hasNext()) {
					key = it.next();
					cols[cnt] = Converter.convertString(key);
					params[cnt++] = m.get(key);
				}
			} else {
				int colLen = len >> 1;
				cols = new String[colLen];
				params = new Object[colLen];
				for(int i = 0; i < len; i += 2) {
					cols[cnt] = Converter.convertString(args[i]);
					params[cnt++] = args[i+1];
				}
			}
			if(conns != null) {
				return conns.execInsert(sql(cols), params);
			} else if(batch != null) {
				batch.add(sql(cols), args);
				return null;
			} else {
				throw new JDBCException("Execution object is not set.");
			}
		}
	}
	
	/**
	 * 削除用SQLオブジェクト.
	 */
	public static final class Update {
		private JDBCConnect conns;
		private JDBCBatch batch;
		private String name;
		private String where;
		private String[] columns;
		private Object[] params;

		/**
		 * コンストラクタ.
		 * @param c
		 * @param list
		 */
		protected Update(JDBCConnect c, JDBCBatch b, Object... list) {
			if(list != null) {
				if(list.length >= 1) {
					name = "" + list[0];
					setColumns(1, list);
				}
			}
			this.conns = c;
			this.batch = b;
		}
		
		/**
		 * テーブル名を設定.
		 * @param table
		 * @return
		 */
		public Update name(String table) {
			this.name = table;
			return this;
		}
		
		/**
		 * 更新カラムと要素を追加.
		 * @param args
		 * @return
		 */
		public Update set(Object... args) {
			setColumns(0, args);
			return this;
		}
		
		// カラムをセット.
		@SuppressWarnings("rawtypes")
		private void setColumns(int off, Object... args) {
			if(args == null || args.length <= off) {
				this.columns = null;
				this.params = null;
				return;
			}
			int cnt = 0;
			String[] cols = null;
			Object[] params = null;
			int len = args.length;
			if(len == off + 1) {
				// map形式で設定されている場合.
				if(args[off] instanceof Map) {
					Object key;
					Map m = (Map)args[off];
					len = m.size();
					cols = new String[len];
					params = new Object[len];
					Iterator it = m.keySet().iterator();
					while(it.hasNext()) {
						key = it.next();
						cols[cnt] = Converter.convertString(key);
						params[cnt++] = m.get(key);
					}
				// 文字列で設定されている場合.
				} else {
					cols = new String[] {"" + args[off]};
					params = null;
				}
			// columns , value のように設定されている.
			} else {
				int colLen = (len - off) >> 1;
				cols = new String[colLen];
				params = new Object[colLen];
				for(int i = off; i < len; i += 2) {
					cols[cnt] = Converter.convertString(args[i]);
					params[cnt++] = args[i+1];
				}
			}
			this.columns = cols;
			this.params = params;
		}
		
		/**
		 * 取得項目条件を設定.
		 * @param where
		 * @return
		 */
		public Update where(Object... where) {
			if(where == null || where.length == 0) {
				this.where = null;
				return this;
			}
			if(where.length == 1) {
				String wstr = "" + where[0];
				if(wstr != null && !wstr.isEmpty()) {
					if(!wstr.trim().toLowerCase().startsWith("where")) {
						wstr = "WHERE " + wstr;
					}
				}
				this.where = wstr;
			} else {
				int len = where.length;
				StringBuilder buf = new StringBuilder("WHERE ");
				for(int i = 0; i < len; i ++) {
					if(i != 0) {
						buf.append(" AND ");
					}
					buf.append(where[i]);
				}
				this.where = buf.toString();
			}
			return this;
		}
		
		// SQLを作成.
		private String sql() {
			if(name == null || name.isEmpty()) {
				throw new JDBCException("Table name is not set.");
			} else if(columns == null || params == null) {
				throw new JDBCException("Update column information is not set.");
			}
			StringBuilder buf = new StringBuilder("UPDATE ")
				.append(name).append(" SET ");
			int len = columns.length;
			for(int i = 0; i < len; i ++) {
				if(i != 0) {
					buf.append(", ");
				}
				buf.append(columns[i]).append("=?");
			}
			if(where != null && !where.isEmpty()) {
				buf.append(" ").append(where);
			}
			buf.append(";");
			return buf.toString();
		}
		
		/**
		 * 文字列を出力.
		 * @retrun
		 */
		public String toString() {
			try {
				return sql();
			} catch(Exception e) {
				return "error";
			}
		}
		
		/**
		 * 実行処理.
		 * @param args パラメータを設定します.
		 * @return
		 */
		public int execute(Object... args) {
			Object[] pms = null;
			if(args.length != 0) {
				if(params == null || params.length == 0) {
					pms = args;
				} else {
					int all = args.length + params.length;
					Object[] p = new Object[all];
					System.arraycopy(params, 0, p, 0, params.length);
					System.arraycopy(args, 0, p, params.length, args.length);
					pms = p;
				}
			} else if(params == null || params.length == 0) {
				pms = new Object[0];
			} else {
				pms = params;
			}
			if(conns != null) {
				return conns.execUpdate(sql(), pms);
			} else if(batch != null) {
				batch.add(sql(), pms);
				return 0;
			} else {
				throw new JDBCException("Execution object is not set.");
			}
		}
	}
}

