package rhigin.lib.jdbc.runner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import rhigin.scripts.RhiginContext;
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.scripts.compile.CompileCache;
import rhigin.util.AndroidMap;
import rhigin.util.OList;

/**
 * JDBCクローズ処理.
 */
public class JDBCCloseable implements RhiginEndScriptCall {
	
	// ローカル実態オブジェクト.
	private static final class Entity {
		public OList<JDBCBatch> batchs;
		public OList<Connection> connections;
		public OList<Statement> statements;
		public OList<ResultSet> resultSets;
		public Map<String, JDBCConnect> useConnects;
	}
	
	// スレッドが管理するJDBCコネクション関連のオブジェクト.
	private final ThreadLocal<Entity> lo = new ThreadLocal<Entity>();
	
	/**
	 * コンストラクタ.
	 */
	public JDBCCloseable() {}
	
	// ローカルオブジェクトを取得.
	private final Entity lo() {
		Entity ret = lo.get();
		if(ret == null) {
			ret = new Entity();
			lo.set(ret);
		}
		return ret;
	}
	
	// ローカルオブジェクトをクリア.
	private final void clearLo() {
		lo.set(null);
	}
	
	/**
	 * コネクション管理Mapを取得.
	 * @return
	 */
	public final Map<String, JDBCConnect> useConnect() {
		Entity et = lo();
		Map<String, JDBCConnect> ret = et.useConnects;
		if (ret == null) {
			ret = new AndroidMap<String, JDBCConnect>();
			et.useConnects = ret;
		}
		return ret;
	}
	
	/**
	 * JDBCBatchを登録.
	 * @param batch
	 * @return
	 */
	public final JDBCCloseable reg(JDBCBatch batch) {
		Entity et = lo();
		OList<JDBCBatch> list = et.batchs;
		if(list == null) {
			list = new OList<JDBCBatch>();
			et.batchs = list;
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
		Entity et = lo();
		OList<Connection> list = et.connections;
		if(list == null) {
			list = new OList<Connection>();
			et.connections = list;
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
		Entity et = lo();
		OList<Statement> list = et.statements;
		if(list == null) {
			list = new OList<Statement>();
			et.statements = list;
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
		Entity et = lo();
		OList<ResultSet> list = et.resultSets;
		if(list == null) {
			list = new OList<ResultSet>();
			et.resultSets = list;
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
		Entity et;
		if((et = lo.get()) != null) {
			final OList<JDBCBatch> list = et.batchs;
			if(list != null && (len = list.size()) > 0) {
				for(int i = 0; i < len; i ++) {
					try {
						list.get(i).clear();
					} catch(Exception e) {}
				}
				list.clear();
			}
		}
		return this;
	}
	
	/**
	 * リザルトセットクリア.
	 * @return
	 */
	public final JDBCCloseable clearResultSet() {
		int len;
		Entity et;
		if((et = lo.get()) != null) {
			final OList<ResultSet> list = et.resultSets;
			if(list != null && (len = list.size()) > 0) {
				for(int i = 0; i < len; i ++) {
					try {
						list.get(i).close();
					} catch(Exception e) {}
				}
				list.clear();
			}
		}
		return this;
	}
	
	/**
	 * ステートメントクリア.
	 * @return
	 */
	public final JDBCCloseable clearStatement() {
		int len;
		Entity et;
		if((et = lo.get()) != null) {
			final OList<Statement> list = et.statements;
			if(list != null && (len = list.size()) > 0) {
				for(int i = 0; i < len; i ++) {
					try {
						list.get(i).close();
					} catch(Exception e) {}
				}
				list.clear();
			}
		}
		return this;
	}
	
	/**
	 * コネクションクリア.
	 * @return
	 */
	public final JDBCCloseable clearConnection() {
		int len;
		Entity et;
		if((et = lo.get()) != null) {
			final OList<Connection> list = et.connections;
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
		
		clearLo();
	}
	

}
