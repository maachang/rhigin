package rhigin.lib;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginConfig;
import rhigin.RhiginException;
import rhigin.lib.jdbc.JDBCCore;
import rhigin.lib.jdbc.runner.JDBCConnect;
import rhigin.lib.jdbc.runner.JDBCRow;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.JavaRequire;
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.Converter;
import rhigin.util.FixedArray;

/**
 * [js]JDBCコンポーネント.
 * 
 * js 上で、以下のようにして呼び出します.
 * 
 * var jdbc = require("@rhigin/lib/JDBC");
 */
public class JDBC implements JavaRequire {
	
	/** コンポーネント名. **/
	public static final String NAME = "JDBC";
	
	/** コンポーネントバージョン. **/
	public static final String VERSION = "0.0.1";
	
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
	protected static final JDBCCore CORE = new JDBCCore();
	
	// JDBCオブジェクトインスタンス.
	private static final RhiginObject JDBC_INSTANCE = new RhiginObject("JDBC", new RhiginFunction[] {
		new JDBCFunctions(0), new JDBCFunctions(1), new JDBCFunctions(2), new JDBCFunctions(3),
		new JDBCFunctions(4), new JDBCFunctions(5), new JDBCFunctions(6), new JDBCFunctions(7),
		new JDBCFunctions(8), new JDBCFunctions(9)
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
				case 0: // version.
					{
						return VERSION;
					}
				case 1: // name.
					{
						return NAME;
					}
				case 2: // startup.
					{
						// スタートアップ登録されていない場合のみ実行.
						if(!CORE.isStartup()) {
							RhiginEndScriptCall es = null;
							final RhiginConfig conf = ExecuteScript.getConfig();
							if(args.length > 0) {
								es = CORE.startup(conf, "" + args[0]);
							} else {
								es = CORE.startup(conf, null);
							}
							ExecuteScript.addEndScripts(es);
							return true;
						}
						return false;
					}
				case 3: // isStartup.
					{
						return CORE.isStartup();
					}
				case 4: // abort.
					{
						CORE.close();
					}
					break;
				case 5: // connect.
					{
						if(args.length > 0) {
							if(args.length == 1) {
								// プーリングコネクションから取得.
								return JDBC.createConnect(CORE.getNewConnect("" + args[0]));
							} else {
								// プーリングコネクションを利用せずにコネクションを取得.
								return JDBC.createConnect(CORE.getNoPoolingConnect(args));
							}
						} else if(CORE.size() > 0) {
							// 一番最初に定義されている定義情報のコネクションを取得.
							return JDBC.createConnect(CORE.getNewConnect(CORE.getName(0)));
						}
						argsException("JDBC");
					}
				case 6: // kind.
					{
						if(args.length > 0) {
							return CORE.getKind("" + args[0]).getMap();
						} else if(CORE.size() > 0) {
							// 一番最初に定義されている定義情報を取得.
							return CORE.getKind(CORE.getName(0)).getMap();
						}
						argsException("JDBC");
					}
				case 7: // isRegister.
					{
						if(args.length > 0) {
							return CORE.isRegister("" + args[0]);
						}
						argsException("JDBC");
					}
				case 8: // length.
					{
						return CORE.size();
					}
				case 9: // names.
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
			case 0: return "version";
			case 1: return "name";
			case 2: return "startup";
			case 3: return "isStartup";
			case 4: return "abort";
			case 5: return "connect";
			case 6: return "kind";
			case 7: return "isRegister";
			case 8: return "length";
			case 9: return "names";
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
			new ConnectFunctions(18, c)
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
				case 18: // sequenceId.
					{
						return conn.getSequenceId();
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
			case 11: return "setAutoCommit";
			case 12: return "getFetchSize";
			case 13: return "setFetchSize";
			case 14: return "clearBatch";
			case 15: return "executeBatch";
			case 16: return "addBatch";
			case 17: return "batchSize";
			case 18: return "sequenceId";
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
			new RowFunctions(4, r), new RowFunctions(5, r)
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
				case 5: // toString.
					{
						return row.toString();
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
			case 5: return "toString";
			}
			return "unknown";
		}
	};
}
