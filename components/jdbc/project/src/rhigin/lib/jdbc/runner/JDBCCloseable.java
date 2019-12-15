package rhigin.lib.jdbc.runner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import rhigin.scripts.RhiginContext;
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.scripts.compile.CompileCache;
import rhigin.util.OList;

/**
 * JDBCクローズ処理.
 */
public class JDBCCloseable implements RhiginEndScriptCall {
	
	// スレッドが管理するJDBCコネクション関連のオブジェクト.
	private final ThreadLocal<OList<JDBCBatch>> jdbcBatchs = new ThreadLocal<OList<JDBCBatch>>();
	private final ThreadLocal<OList<Connection>> connections = new ThreadLocal<OList<Connection>>();
	private final ThreadLocal<OList<Statement>> statements = new ThreadLocal<OList<Statement>>();
	private final ThreadLocal<OList<ResultSet>> resultSets = new ThreadLocal<OList<ResultSet>>();
	
	/**
	 * コンストラクタ.
	 */
	public JDBCCloseable() {}
	
	/**
	 * JDBCBatchを登録.
	 * @param batch
	 * @return
	 */
	public final JDBCCloseable reg(JDBCBatch batch) {
		OList<JDBCBatch> list = jdbcBatchs.get();
		if(list == null) {
			list = new OList<JDBCBatch>();
			jdbcBatchs.set(list);
		}
		list.add(batch);
		return this;
	}
	
	/**
	 * コネクションを登録.
	 * @param conn
	 * @return
	 */
	public final JDBCCloseable reg(Connection conn) {
		OList<Connection> list = connections.get();
		if(list == null) {
			list = new OList<Connection>();
			connections.set(list);
		}
		list.add(conn);
		return this;
	}
	
	/**
	 * ステートメントを登録.
	 * @param conn
	 * @return
	 */
	public final JDBCCloseable reg(Statement stmt) {
		OList<Statement> list = statements.get();
		if(list == null) {
			list = new OList<Statement>();
			statements.set(list);
		}
		list.add(stmt);
		return this;
	}
	
	/**
	 * リザルトセットを登録.
	 * @param conn
	 * @return
	 */
	public final JDBCCloseable reg(ResultSet rset) {
		OList<ResultSet> list = resultSets.get();
		if(list == null) {
			list = new OList<ResultSet>();
			resultSets.set(list);
		}
		list.add(rset);
		return this;
	}
	
	/**
	 * バッチクリア.
	 * @return
	 */
	public final JDBCCloseable clearBatch() {
		int len;
		final OList<JDBCBatch> list = jdbcBatchs.get();
		if(list != null && (len = list.size()) > 0) {
			for(int i = 0; i < len; i ++) {
				try {
					list.get(i).clear();
				} catch(Exception e) {}
			}
			list.clear();
		}
		return this;
	}
	
	/**
	 * リザルトセットクリア.
	 * @return
	 */
	public final JDBCCloseable clearResultSet() {
		int len;
		final OList<ResultSet> list = resultSets.get();
		if(list != null && (len = list.size()) > 0) {
			for(int i = 0; i < len; i ++) {
				try {
					list.get(i).close();
				} catch(Exception e) {}
			}
			list.clear();
		}
		return this;
	}
	
	/**
	 * ステートメントクリア.
	 * @return
	 */
	public final JDBCCloseable clearStatement() {
		int len;
		final OList<Statement> list = statements.get();
		if(list != null && (len = list.size()) > 0) {
			for(int i = 0; i < len; i ++) {
				try {
					list.get(i).close();
				} catch(Exception e) {}
			}
			list.clear();
		}
		return this;
	}
	
	/**
	 * コネクションクリア.
	 * @return
	 */
	public final JDBCCloseable clearConnection() {
		int len;
		final OList<Connection> list = connections.get();
		if(list != null && (len = list.size()) > 0) {
			Connection c;
			boolean cf;
			for(int i = 0; i < len; i ++) {
				c = list.get(i);
				try {
					cf = c.isClosed();
				} catch(Exception e) {
					cf = false;
				}
				if(!cf) {
					try {
						c.rollback();
					} catch(Exception e) {}
					try {
						c.close();
					} catch(Exception e) {}
				}
			}
			list.clear();
		}
		return this;
	}

	/**
	 * 今回のスクリプト実行で利用したJDBCオブジェクト関連のクローズ処理.
	 * @params context
	 * @params cache
	 */
	@Override
	public final void call(RhiginContext context, CompileCache cache) {
		clearBatch();
		clearResultSet();
		clearStatement();
		clearConnection();
	}
	

}
