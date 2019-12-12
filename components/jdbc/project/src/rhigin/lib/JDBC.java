package rhigin.lib;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginConfig;
import rhigin.RhiginException;
import rhigin.lib.jdbc.pooling.AtomicPooling;
import rhigin.lib.jdbc.pooling.AtomicPoolingManager;
import rhigin.lib.jdbc.pooling.AtomicPoolingMonitor;
import rhigin.lib.jdbc.runner.JDBCCloseable;
import rhigin.lib.jdbc.runner.JDBCConnect;
import rhigin.lib.jdbc.runner.JDBCException;
import rhigin.lib.jdbc.runner.JDBCKind;
import rhigin.lib.jdbc.runner.JDBCRow;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.JavaRequire;
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.ArrayMap;
import rhigin.util.Converter;
import rhigin.util.FixedArray;

/**
 * [js]JDBCオブジェクト.
 * 
 * js 上で、以下のようにして呼び出します.
 * 
 * var jdbc = require("@rhigin/lib/JDBC");
 */
public class JDBC implements JavaRequire {
	
	/**
	 * コンストラクタ.
	 */
	public JDBC() {
		// [JavaRequire]の場合は、public な空のコンストラクタは必須.
	}
	
	/**
	 * require呼び出しの返却処理.
	 */
	@Override
	public Scriptable load() {
		return JDBC_INSTANCE;
	}
	
	// コアオブジェクト.
	protected static final JdbcCoreObject CORE = new JdbcCoreObject();
	
	// jdbcコアオブジェクト.
	protected static final class JdbcCoreObject {
		private boolean isStartup = false;
		private final AtomicPoolingManager man = new AtomicPoolingManager();
		private final AtomicPoolingMonitor mon = new AtomicPoolingMonitor();
		private final JDBCCloseable closeable = new JDBCCloseable();
		
		/**
		 * jdbc初期化処理.
		 * この処理は[RhiginStartup]で読み込まれる[index.js]内で呼び出します.
		 * 
		 * @param conf DB接続情報を設定している、jdbc.jsonの定義条件を設定します.
		 * @return RhiginEndScriptCall スクリプト実行後のクローズ処理が返却されます.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public RhiginEndScriptCall startup(Map<String, Object> conf) {
			if(conf == null || conf.size() == 0) {
				if(conf == null) {
					// jdbcの定義が無い場合は、空のJSONを設定.
					conf = new ArrayMap();
				}
			}
			if(!isStartup) {
				Iterator<String> itr = conf.keySet().iterator();
				AtomicPooling p;
				while(itr.hasNext()) {
 					p = new AtomicPooling(JDBCKind.create((Map)conf.get(itr.next())), mon);
					man.register(p); p = null;
				}
				mon.startThread();
				isStartup = true;
			}
			return closeable;
		}
		
		/**
		 * プーリングのJDBCコネクションを取得.
		 * @param name 登録されているプーリング名を設定します.
		 * @return
		 */
		public JDBCConnect connect(String name) {
			final AtomicPooling p = man.get(name);
			if(p == null) {
				throw new JDBCException(
					"Connection information with the specified name does not exist:" + name);
			}
			return JDBCConnect.create(closeable, p.getConnection());
		}
		
		/**
		 * 非プーリングのJDBCコネクションを取得.
		 * @param args [0] drivre, [1] url, [2] user, [3] password を入れます.
		 */
		public JDBCConnect connect(Object[] args) {
			try {
				final String driver = args.length > 0 ? "" + args[0] : null;
				final String url = args.length > 1 ? "" + args[1] : null;
				final String user = args.length > 2 ? "" + args[2] : null;
				final String password = args.length > 3 ? "" + args[3] : null;
				final Connection c = AtomicPooling.getConnetion(driver, url, user, password);
				return JDBCConnect.create(closeable, c);
			} catch(JDBCException je) {
				throw je;
			} catch(Exception e) {
				throw new JDBCException(e);
			}
		}
		
		public JDBCKind getKind(String name) {
			final JDBCKind k = man.getKind(name);
			if(k == null) {
				throw new JDBCException(
					"Connection information with the specified name does not exist:" + name);
			}
			return k;
		}
		
		/**
		 * 指定名が登録されているかチェック.
		 * @param name
		 * @return
		 */
		public boolean isRegister(String name) {
			return man.contains(name);
		}
		
		/**
		 * 登録接続条件を取得.
		 * @return
		 */
		public int size() {
			return man.size();
		}
		
		/**
		 * 登録名一覧を取得.
		 * @return List<String>
		 */
		public List<String> names() {
			return new FixedArray<String>(man.getNames());
		}
	};
	
	// JDBCオブジェクトインスタンス.
	private static final RhiginObject JDBC_INSTANCE = new RhiginObject("JDBC", new RhiginFunction[] {
		new JDBCFunctions(0), new JDBCFunctions(1), new JDBCFunctions(2), new JDBCFunctions(3),
		new JDBCFunctions(4), new JDBCFunctions(5)
	});
	
	// jdbcオブジェクトのメソッド群. 
	private static final class JDBCFunctions extends RhiginFunction {
		private final int type;

		JDBCFunctions(int t) {
			this.type = t;
		}
		
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // startup.
					{
						// スタートアップ登録されていない場合のみ実行.
						if(!CORE.isStartup) {
							final RhiginConfig conf = ExecuteScript.getConfig();
							String name = "jdbc";
							if(args.length > 0) {
								// JDBC読み込み対象のコンフィグ情報名が設定されている場合.
								name = "" + args[0];
								Object o = conf.get(name);
								if(o == null || !(o instanceof Map)) {
									argsException("JDBC");
								}
							}
							// スタートアップ実行をして、スクリプト実行後の終了処理時にJDBC関連のクローズ処理実行を登録.
							final RhiginEndScriptCall e = CORE.startup(conf.get(name));
							ExecuteScript.addEndScripts(e);
							return true;
						}
						return false;
					}
				case 1: // connect.
					{
						if(args.length > 0) {
							if(args.length == 1) {
								// プーリングコネクションから取得.
								return JDBC.createConnect(CORE.connect("" + args[0]));
							} else {
								// プーリングコネクションを利用せずにコネクションを取得.
								return JDBC.createConnect(CORE.connect(args));
							}
						}
						argsException("JDBC");
					}
				case 2: // kind.
					{
						if(args.length > 0) {
							return CORE.getKind("" + args[0]).getMap();
						}
						argsException("JDBC");
					}
				case 3: // isRegister.
					{
						if(args.length > 0) {
							return CORE.isRegister("" + args[0]);
						}
						argsException("JDBC");
					}
				case 4: // size.
					{
						return CORE.size();
					}
				case 5: // names.
					{
						return CORE.names();
					}
				}
				
			} catch (RhiginException re) {
				throw re;
			} catch (Exception e) {
				throw new RhiginException(500, e);
			}
			return Undefined.instance;
		}
		
		@Override
		public final String getName() {
			switch (type) {
			case 0: return "startup";
			case 1: return "connect";
			case 2: return "kind";
			case 3: return "isRegister";
			case 4: return "length";
			case 5: return "names";
			}
			return "unknown";
		}
		
	};
	
	// JDBCコネクションオブジェクトを生成.
	private static final RhiginObject createConnect(JDBCConnect c) {
		return new RhiginObject("JDBCConnect", new RhiginFunction[] {
			new ConnectFunctions(0, c), new ConnectFunctions(1, c), new ConnectFunctions(2, c),
			new ConnectFunctions(3, c), new ConnectFunctions(4, c), new ConnectFunctions(5, c),
			new ConnectFunctions(6, c), new ConnectFunctions(7, c), new ConnectFunctions(8, c),
			new ConnectFunctions(9, c), new ConnectFunctions(10, c), new ConnectFunctions(11, c),
			new ConnectFunctions(12, c), new ConnectFunctions(13, c), new ConnectFunctions(14, c),
			new ConnectFunctions(15, c), new ConnectFunctions(16, c), new ConnectFunctions(17, c),
		});
	}
	
	// Connectオブジェクトのメソッド群. 
	private static final class ConnectFunctions extends RhiginFunction {
		private final int type;
		private final JDBCConnect conn;

		ConnectFunctions(int t, JDBCConnect c) {
			this.type = t;
			this.conn = c;
		}
		
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // query.
					{
						if(args.length > 0) {
							return createRow(conn.query("" + args[0], getParams(1, args)));
						}
						argsException("JDBCConnect");
					}
				case 1: // fquery.
					{
						if(args.length > 0) {
							return createRow(conn.first("" + args[0], getParams(1, args)));
						}
						argsException("JDBCConnect");
					}
				case 2: // lquery.
					{
						if(args.length > 1 && Converter.isNumeric(args[1])) {
							return createRow(conn.limit("" + args[0],Converter.convertInt(args[1]), getParams(2, args)));
						}
						argsException("JDBCConnect");
					}
				case 3: // update.
					{
						if(args.length > 0) {
							return conn.update("" + args[0], getParams(1, args));
						}
						argsException("JDBCConnect");
					}
				case 4: // insert.
					{
						if(args.length > 0) {
							return createRow(conn.insert("" + args[0], getParams(1, args)));
						}
						argsException("JDBCConnect");
					}
				case 5: // commit.
					{
						conn.commit();
					}
					break;
				case 6: // rollback.
					{
						conn.rollback();
					}
					break;
				case 7: // close.
					{
						conn.close();
					}
					break;
				case 8: // isClose.
					{
						return conn.isClose();
					}
				case 9: // kind.
					{
						return conn.getKind();
					}
				case 10: // isAutoCommit.
					{
						return conn.isAutoCommit();
					}
				case 11: // setAutoCommit.
					{
						if(args.length > 0) {
							final boolean ret = conn.isAutoCommit();
							conn.setAutoCommit(Converter.convertBool(args[0]));
							return ret;
						}
						argsException("JDBCConnect");
					}
				case 12: // getFetchSize.
					{
						return conn.getFetchSize();
					}
				case 13: // setFetchSize.
					{
						if(args.length > 0 && Converter.isNumeric(args[0])) {
							final int ret = conn.getFetchSize();
							conn.setFetchSize(Converter.convertInt(args[0]));
							return ret;
						}
						argsException("JDBCConnect");
					}
				case 14: // clearBatch.
					{
						conn.clearBatch();
					}
					break;
				case 15: // executeBatch.
					{
						return new FixedArray<Integer>(conn.executeBatch());
					}
				case 16: // addBatch.
					{
						if(args.length > 0) {
							conn.addBatch("" + args[0], getParams(1, args));
							break;
						}
						argsException("JDBCConnect");
					}
				case 17: // batchSize.
					{
						return conn.batchSize();
					}
				}
			} catch (RhiginException re) {
				throw re;
			} catch (Exception e) {
				throw new RhiginException(500, e);
			}
			return Undefined.instance;
		}
		
		@Override
		public final String getName() {
			switch (type) {
			case 0: return "query";
			case 1: return "fquery";
			case 2: return "lquery";
			case 3: return "update";
			case 4: return "insert";
			case 5: return "commit";
			case 6: return "rollback";
			case 7: return "close";
			case 8: return "isClose";
			case 9: return "kind";
			case 10: return "isAutoCommit";
			case 11: return "getAutoCommit";
			case 12: return "getFetchSize";
			case 13: return "setFetchSize";
			case 14: return "clearBatch";
			case 15: return "executeBatch";
			case 16: return "addBatch";
			case 17: return "batchSize";
			}
			return "unknown";
		}
		
		// 連続パラメータの分離.
		private static final Object[] getParams(int n, Object[] src) {
			int len = src.length - n;
			Object[] ret = new Object[len];
			System.arraycopy(src, n, ret, 0, len);
			return ret;
		}
	};
	
	
	// JDBC行情報を生成.
	private static final RhiginObject createRow(JDBCRow r) {
		return new RhiginObject("JDBCRow", new RhiginFunction[] {
			new RowFunctions(0, r), new RowFunctions(1, r), new RowFunctions(2, r), new RowFunctions(3, r),
			new RowFunctions(4, r)
		});
	}
	
	// Rowオブジェクトのメソッド群. 
	private static final class RowFunctions extends RhiginFunction {
		private final int type;
		private final JDBCRow row;

		RowFunctions(int t, JDBCRow r) {
			this.type = t;
			this.row = r;
		}
		
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // close.
					{
						row.close();
					}
					break;
				case 1: // isClose.
					{
						return row.isClose();
					}
				case 2: // hasNext.
					{
						return row.hasNext();
					}
				case 3: // next.
					{
						return row.next();
					}
				case 4: // rows.
					{
						if(args.length == 0) {
							return row.getRows();
						} else {
							return row.getRows(Converter.convertInt(args[0]));
						}
					}
				}
			} catch (RhiginException re) {
				throw re;
			} catch (Exception e) {
				throw new RhiginException(500, e);
			}
			return Undefined.instance;
		}
		
		@Override
		public final String getName() {
			switch (type) {
			case 0: return "close";
			case 1: return "isClose";
			case 2: return "hasNext";
			case 3: return "next";
			case 4: return "rows";
			}
			return "unknown";
		}
	};
}
