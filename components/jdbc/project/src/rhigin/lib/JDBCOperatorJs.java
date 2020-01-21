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
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginInstanceObject;
import rhigin.scripts.RhiginInstanceObject.ObjectFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.Converter;
import rhigin.util.FixedArray;
import rhigin.util.FixedSearchArray;

/**
 * [js]jdbc 操作系.
 */
class JDBCOperatorJs {
	
	// コアオブジェクト.
	protected static final JDBCCore CORE = new JDBCCore();
	
	// JDBCオブジェクトインスタンス.
	protected static final RhiginObject JDBC_INSTANCE = new RhiginObject("JDBC", new RhiginFunction[] {
		new JDBCFunctions(0), new JDBCFunctions(1), new JDBCFunctions(2), new JDBCFunctions(3),
		new JDBCFunctions(4), new JDBCFunctions(5), new JDBCFunctions(6), new JDBCFunctions(7),
		new JDBCFunctions(8), new JDBCFunctions(9)
	});
	
	/**
	 * jdbcオブジェクトのメソッド群. 
	 */
	private static final class JDBCFunctions extends RhiginFunction {
		private final int type;

		JDBCFunctions(int t) {
			this.type = t;
		}
		
		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // version.
					{
						return JDBC.VERSION;
					}
				case 1: // name.
					{
						return JDBC.NAME;
					}
				case 2: // startup.
					{
						// スタートアップ登録されていない場合のみ実行.
						if(!CORE.isStartup()) {
							RhiginEndScriptCall[] es = null;
							final RhiginConfig conf = ExecuteScript.getConfig();
							if(args.length > 0) {
								es = CORE.startup(conf, "" + args[0]);
							} else {
								es = CORE.startup(conf, null);
							}
							ExecuteScript.addEndScripts(es[0]);
							ExecuteScript.addExitSystemScripts(es[1]);
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
								return createConnect(CORE.getNewConnect("" + args[0]));
							} else {
								// プーリングコネクションを利用せずにコネクションを取得.
								return createConnect(CORE.getNoPoolingConnect(args));
							}
						} else if(CORE.size() > 0) {
							// 一番最初に定義されている定義情報のコネクションを取得.
							return createConnect(CORE.getNewConnect(CORE.getName(0)));
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
	
	// JDBCコネクションメソッド名群.
	private static final String[] JDBC_CONNECT_NAMES = new String[] {
		"query"
		,"first"
		,"execUpdate"
		,"execInsert"
		,"commit"
		,"rollback"
		,"close"
		,"isClose"
		,"kind"
		,"isAutoCommit"
		,"setAutoCommit"
		,"getFetchSize"
		,"setFetchSize"
		,"clearBatch"
		,"executeBatch"
		,"addBatch"
		,"batchSize"
		,JDBCCore.TIME12
		,"select"
		,"delete"
		,"insert"
		,"update"
		,"deleteBatch"
		,"insertBatch"
		,"updateBatch"
	};
	
	// JDBCコネクションメソッド生成処理.
	private static final ObjectFunction JDBC_CONNECT_FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(JDBC_CONNECT_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new ConnectFunctions(no, (JDBCConnect)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};
	
	/**
	 * JDBCコネクションオブジェクトを生成.
	 * @param c
	 * @return
	 */
	public static final RhiginInstanceObject createConnect(JDBCConnect c) {
		return new RhiginInstanceObject("JDBCConnect", JDBC_CONNECT_FUNCTIONS, c);
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
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // query.
					{
						if(args.length > 0) {
							return createRow(conn.query("" + args[0], getParams(1, args)));
						}
						argsException("JDBCConnect");
					}
				case 1: // first.
					{
						if(args.length > 0) {
							return createRow(conn.first("" + args[0], getParams(1, args)));
						}
						argsException("JDBCConnect");
					}
				case 2: // execUpdate.
					{
						if(args.length > 0) {
							return conn.execUpdate("" + args[0], getParams(1, args));
						}
						argsException("JDBCConnect");
					}
				case 3: // execInsert.
					{
						if(args.length > 0) {
							return createRow(conn.execInsert("" + args[0], getParams(1, args)));
						}
						argsException("JDBCConnect");
					}
				case 4: // commit.
					{
						conn.commit();
					}
					break;
				case 5: // rollback.
					{
						conn.rollback();
					}
					break;
				case 6: // close.
					{
						conn.close();
					}
					break;
				case 7: // isClose.
					{
						return conn.isClose();
					}
				case 8: // kind.
					{
						return conn.getKind();
					}
				case 9: // isAutoCommit.
					{
						return conn.isAutoCommit();
					}
				case 10: // setAutoCommit.
					{
						if(args.length > 0) {
							final boolean ret = conn.isAutoCommit();
							conn.setAutoCommit(Converter.convertBool(args[0]));
							return ret;
						}
						argsException("JDBCConnect");
					}
				case 11: // getFetchSize.
					{
						return conn.getFetchSize();
					}
				case 12: // setFetchSize.
					{
						if(args.length > 0 && Converter.isNumeric(args[0])) {
							final int ret = conn.getFetchSize();
							conn.setFetchSize(Converter.convertInt(args[0]));
							return ret;
						}
						argsException("JDBCConnect");
					}
				case 13: // clearBatch.
					{
						conn.clearBatch();
					}
					break;
				case 14: // executeBatch.
					{
						return new FixedArray<Integer>(conn.executeBatch());
					}
				case 15: // addBatch.
					{
						if(args.length > 0) {
							conn.addBatch("" + args[0], getParams(1, args));
							break;
						}
						argsException("JDBCConnect");
					}
				case 16: // batchSize.
					{
						return conn.batchSize();
					}
				case 17: // TIME12.
					{
						return conn.TIME12();
					}
				case 18: // select.
					{
						return JDBCQueryJs.createSelect(conn.select(args));
					}
				case 19: // delete.
					{
						return JDBCQueryJs.createDelete(conn.delete(args));
					}
				case 20: // insert.
					{
						return JDBCQueryJs.createInsert(conn.insert(args));
					}
				case 21: // update.
					{
						return JDBCQueryJs.createUpdate(conn.update(args));
					}
				case 22: // deleteBatch.
					{
						return JDBCQueryJs.createDelete(conn.deleteBatch(args));
					}
				case 23: // insertBatch.
					{
						return JDBCQueryJs.createInsert(conn.insertBatch(args));
					}
				case 24: // updateBatch.
					{
						return JDBCQueryJs.createUpdate(conn.updateBatch(args));
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
			return JDBC_CONNECT_NAMES[type];
		}
		
		// 連続パラメータの分離.
		private static final Object[] getParams(int n, Object[] src) {
			int len = src.length - n;
			Object[] ret = new Object[len];
			System.arraycopy(src, n, ret, 0, len);
			return ret;
		}
		
	};
	
	// JDBC行情報メソッド名群.
	private static final String[] JDBC_ROW_NAMES = new String[] {
		"close"
		,"isClose"
		,"hasNext"
		,"next"
		,"rows"
		,"toString"
	};
	
	// JDBC行情報メソッド生成処理.
	private static final ObjectFunction JDBC_ROW_FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(JDBC_ROW_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new RowFunctions(no, (JDBCRow)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};
	
	/**
	 * JDBC行情報を生成.
	 * @param r
	 * @return
	 */
	public static final RhiginInstanceObject createRow(JDBCRow r) {
		return new RhiginInstanceObject("JDBCRow", JDBC_ROW_FUNCTIONS, r);
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
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
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
			return JDBC_ROW_NAMES[type];
		}
	};
}
