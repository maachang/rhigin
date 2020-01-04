package rhigin.lib.jdbc.runner;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import rhigin.lib.jdbc.runner.JDBCConnect.Delete;
import rhigin.lib.jdbc.runner.JDBCConnect.Insert;
import rhigin.lib.jdbc.runner.JDBCConnect.Update;
import rhigin.util.OList;

/**
 * JDBC-Batch.
 */
public class JDBCBatch {
	private JDBCConnect jcon = null;
	private OList<Statement> batchList = null;
	private Map<String, PreparedStatement> psCache = null;
	private int batchLength = 0;
	
	// コンストラクタ.
	protected JDBCBatch(JDBCConnect jcon) {
		this.jcon = jcon;
	}
	
	/**
	 * バッチ定義をクリア.
	 * @return
	 */
	public JDBCBatch clear() {
		if(batchList == null) {
			return this;
		}
		Statement stmt;
		OList<Statement> list = batchList;
		batchList = null;
		psCache = null;
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			stmt = list.get(i);
			try {
				stmt.clearBatch();
			} catch(Exception e) {}
			try {
				stmt.close();
			} catch(Exception e) {}
		}
		batchLength = 0;
		return this;
	}
	
	/**
	 * バッチ実行を反映.
	 * @return
	 */
	public int[] execute() {
		jcon.check();
		if(batchList == null) {
			return new int[0];
		}
		OList<Statement> list = batchList;
		batchList = null;
		psCache = null;
		int[] res;
		final int len = list.size();
		int off = 0;
		int[] ret = new int[batchLength];
		try {
			for(int i = 0; i < len; i ++) {
				res = list.get(i).executeBatch();
				System.arraycopy(res, 0, ret, off, res.length);
				off += res.length;
			}
		} catch(Exception e) {
			if(e instanceof JDBCException) {
				throw (JDBCException)e;
			}
			throw new JDBCException(e);
		} finally {
			for(int i = 0; i < len; i ++) {
				try {
					list.get(i).close();
				} catch(Exception ee) {}
			}
			batchLength = 0;
		}
		return ret;
	}
	
	/**
	 * バッチ追加.
	 * @param sql
	 * @param args
	 * @return
	 */
	public JDBCBatch add(String sql, Object... args) {
		jcon.check();
		PreparedStatement stmt = null;
		try {
			sql = JDBCUtils.sql(jcon.kind, sql);
			if(psCache == null || (stmt = psCache.get(sql)) == null) {
				stmt = jcon.conn.prepareStatement(sql, Statement.NO_GENERATED_KEYS);
				JDBCUtils.preParams(stmt, stmt.getParameterMetaData(),
					JDBCUtils.appendSequence(jcon, args));
				stmt.addBatch();
				if(batchList == null) {
					batchList = new OList<Statement>();
				}
				if(psCache == null) {
					psCache = new HashMap<String, PreparedStatement>();
				}
				batchList.add(stmt);
				psCache.put(sql, stmt);
			} else {
				JDBCUtils.preParams(stmt, stmt.getParameterMetaData(),
					JDBCUtils.appendSequence(jcon, args));
				stmt.addBatch();
			}
			batchLength ++;
		} catch(Exception e) {
			if(stmt != null) {
				try {
					stmt.close();
				} catch(Exception ee) {}
			}
			// エラーの場合は、バッチ設定した内容を全クリア.
			clear();
			throw JDBCConnect.sqlException(e, sql, args);
		}
		return this;
	}
	
	/**
	 * 現在のバッチ登録数を取得.
	 * @return
	 */
	public int size() {
		jcon.check();
		return batchLength;
	}
	
	/**
	 * DeleteSQL処理.
	 * @param list
	 * @return
	 */
	public Delete delete(Object... list) {
		return new Delete(null, this, list);
	}
	
	/**
	 * InsertSQL処理.
	 * @param list
	 * @return
	 */
	public Insert insert(Object... list) {
		return new Insert(null, this, list);
	}
	
	/**
	 * UpdateSQL処理.
	 * @param list
	 * @return
	 */
	public Update update(Object... list) {
		return new Update(null, this, list);
	}

}
