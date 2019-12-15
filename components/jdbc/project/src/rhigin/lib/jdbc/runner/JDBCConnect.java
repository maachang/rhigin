package rhigin.lib.jdbc.runner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;

import rhigin.lib.jdbc.pooling.AtomicPoolConnection;
import rhigin.util.ArrayMap;

/**
 * JDBC-Connect.
 */	
public class JDBCConnect {
	protected boolean closeFlag = false;
	protected JDBCCloseable closeable = null;
	protected Connection conn = null;
	private JDBCBatch batch = null;
	
	// 今回のコネクション専用のフェッチサイズ.
	private int fetchSize = -1;
	
	/**
	 * コンストラクタ.
	 */
	protected JDBCConnect() {}
	
	/**
	 * 生成処理.
	 * @param c
	 * @param ac
	 * @return
	 */
	public static final JDBCConnect create(JDBCCloseable c, Connection ac) {
		JDBCConnect ret = new JDBCConnect();
		ret.batch = new JDBCBatch(ret);
		ret.closeable = c;
		ret.conn = ac;
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
		return _query(sql, 0, args);
	}
	
	/**
	 * クエリ実行をして先頭１行だけ取得.
	 * @param sql
	 * @param args
	 * @return
	 */
	public JDBCRow first(String sql, Object...args) {
		return _query(sql, 1, args);
	}
	
	/**
	 * クエリ実行をして指定リミット数を取得.
	 * @param sql
	 * @param args
	 * @return
	 */
	public JDBCRow limit(String sql, int limit, Object...args) {
		return _query(sql, limit, args);
	}
	
	// クエリ実行.
	private JDBCRow _query(String sql, int limit, Object[] args) {
		check();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			stmt.setMaxRows(limit > 0 ? limit : 0);
			int fsize = fetchSize > 0 ? fetchSize : stmt.getFetchSize();
			if(fsize > 0 && limit > 0 && fsize > limit) {
				fsize = limit;
			}
			if(fsize > 0) {
				stmt.setFetchSize(fsize);
			}
			JDBCUtils.preParams(stmt, stmt.getParameterMetaData(), args);
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
	public int update(String sql, Object... args) {
		check();
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql, Statement.NO_GENERATED_KEYS);
			JDBCUtils.preParams(stmt, stmt.getParameterMetaData(), args);
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
	public JDBCRow insert(String sql, Object... args) {
		check();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setMaxRows(1);
			JDBCUtils.preParams(stmt, stmt.getParameterMetaData(), args);
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
	public Map<String, Object> getKind() {
		check();
		if(conn instanceof AtomicPoolConnection) {
			return ((AtomicPoolConnection)conn).getKind().getMap();
		}
		return new ArrayMap();
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

}

