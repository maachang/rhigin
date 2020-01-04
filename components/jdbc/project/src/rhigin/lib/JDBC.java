package rhigin.lib;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginConfig;
import rhigin.RhiginException;
import rhigin.lib.jdbc.JDBCCore;
import rhigin.lib.jdbc.runner.JDBCConnect;
import rhigin.lib.jdbc.runner.JDBCConnect.Delete;
import rhigin.lib.jdbc.runner.JDBCConnect.Insert;
import rhigin.lib.jdbc.runner.JDBCConnect.Select;
import rhigin.lib.jdbc.runner.JDBCConnect.Update;
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
			new ConnectFunctions(18, c), new ConnectFunctions(19, c), new ConnectFunctions(20, c),
			new ConnectFunctions(21, c), new ConnectFunctions(22, c), new ConnectFunctions(23, c),
			new ConnectFunctions(24, c)
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
				case 17: // sequenceId.
					{
						return conn.getSequenceId();
					}
				case 18: // select.
					{
						return createSelect(conn.select(args));
					}
				case 19: // delete.
					{
						return createDelete(conn.delete(args));
					}
				case 20: // insert.
					{
						return createInsert(conn.insert(args));
					}
				case 21: // update.
					{
						return createUpdate(conn.update(args));
					}
				case 22: // deleteBatch.
					{
						return createDelete(conn.deleteBatch(args));
					}
				case 23: // insertBatch.
					{
						return createInsert(conn.insertBatch(args));
					}
				case 24: // updateBatch.
					{
						return createUpdate(conn.updateBatch(args));
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
			case 1: return "first";
			case 2: return "execUpdate";
			case 3: return "execInsert";
			case 4: return "commit";
			case 5: return "rollback";
			case 6: return "close";
			case 7: return "isClose";
			case 8: return "kind";
			case 9: return "isAutoCommit";
			case 10: return "setAutoCommit";
			case 11: return "getFetchSize";
			case 12: return "setFetchSize";
			case 13: return "clearBatch";
			case 14: return "executeBatch";
			case 15: return "addBatch";
			case 16: return "batchSize";
			case 17: return "sequenceId";
			case 18: return "select";
			case 19: return "delete";
			case 20: return "insert";
			case 21: return "update";
			case 22: return "deleteBatch";
			case 23: return "insertBatch";
			case 24: return "updateBatch";
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
	
	// Selectオブジェクトを生成.
	private static final RhiginObject createSelect(Select o) {
		return new RhiginObject("Select", new RhiginFunction[] {
			new SelectFunction(0, o), new SelectFunction(1, o), new SelectFunction(2, o), new SelectFunction(3, o),
			new SelectFunction(4, o), new SelectFunction(5, o), new SelectFunction(6, o), new SelectFunction(7, o),
			new SelectFunction(8, o), new SelectFunction(9, o), new SelectFunction(10, o)
		});
	}
	
	// Selectオブジェクトのメソッド群. 
	private static final class SelectFunction extends RhiginFunction {
		private final int type;
		private final Select object;

		SelectFunction(int t, Select o) {
			this.type = t;
			this.object = o;
		}
		
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // "name";
					{
						if(args.length > 0) {
							object.name("" + args[0]);
						} else {
							object.name(null);
						}
						break;
					}
				case 1: // "columns";
					{
						object.columns(args);
						break;
					}
				case 2: // "where";
					{
						object.where(args);
						break;
					}
				case 3: // "groupby";
					{
						object.groupby(args);
						break;
					}
				case 4: // "having";
					{
						object.having(args);
						break;
					}
				case 5: // "orderby";
					{
						object.orderby(args);
						break;
					}
				case 6: // "ather";
					{
						if(args.length > 0) {
							object.ather("" + args[0]);
						} else {
							object.ather(null);
						}
						break;
					}
				case 7: // "range";
					{
						object.range(args);
						break;
					}
				case 8: // "offset";
					{
						if(args.length > 0) {
							object.offset(args[0]);
						} else {
							object.offset(null);
						}
						break;
					}
				case 9: // "limit";
					{
						if(args.length > 0) {
							object.limit(args[0]);
						} else {
							object.limit(null);
						}
						break;
					}
				case 10: // "execute";
					{
						return createRow(object.execute(args));
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
			case 0: return "name";
			case 1: return "columns";
			case 2: return "where";
			case 3: return "groupby";
			case 4: return "having";
			case 5: return "orderby";
			case 6: return "ather";
			case 7: return "range";
			case 8: return "offset";
			case 9: return "limit";
			case 10: return "execute";
			}
			return "unknown";
		}
	};
	
	// Deleteオブジェクトを生成.
	private static final RhiginObject createDelete(Delete o) {
		return new RhiginObject("Delete", new RhiginFunction[] {
			new DeleteFunction(0, o), new DeleteFunction(1, o), new DeleteFunction(2, o)
		});
	}
	
	// Deleteオブジェクトのメソッド群. 
	private static final class DeleteFunction extends RhiginFunction {
		private final int type;
		private final Delete object;

		DeleteFunction(int t, Delete o) {
			this.type = t;
			this.object = o;
		}
		
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // "name";
					{
						if(args.length > 0) {
							object.name("" + args[0]);
						} else {
							object.name(null);
						}
						break;
					}
				case 1: // "where";
					{
						object.where(args);
						break;
					}
				case 2: // "execute";
					{
						return object.execute(args);
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
			case 0: return "name";
			case 1: return "where";
			case 2: return "execute";
			}
			return "unknown";
		}
	};
	
	// Insertオブジェクトを生成.
	private static final RhiginObject createInsert(Insert o) {
		return new RhiginObject("Insert", new RhiginFunction[] {
			new InsertFunction(0, o), new InsertFunction(1, o)
		});
	}
	
	// Insertオブジェクトのメソッド群. 
	private static final class InsertFunction extends RhiginFunction {
		private final int type;
		private final Insert object;

		InsertFunction(int t, Insert o) {
			this.type = t;
			this.object = o;
		}
		
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // "name";
					{
						if(args.length > 0) {
							object.name("" + args[0]);
						} else {
							object.name(null);
						}
						break;
					}
				case 1: // "execute";
					{
						return object.execute(args);
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
			case 0: return "name";
			case 1: return "execute";
			}
			return "unknown";
		}
	};
	
	// Updateオブジェクトを生成.
	private static final RhiginObject createUpdate(Update o) {
		return new RhiginObject("Update", new RhiginFunction[] {
			new UpdateFunction(0, o), new UpdateFunction(1, o), new UpdateFunction(2, o), new UpdateFunction(3, o)
		});
	}
	
	// Updateオブジェクトのメソッド群. 
	private static final class UpdateFunction extends RhiginFunction {
		private final int type;
		private final Update object;

		UpdateFunction(int t, Update o) {
			this.type = t;
			this.object = o;
		}
		
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // "name";
					{
						if(args.length > 0) {
							object.name("" + args[0]);
						} else {
							object.name(null);
						}
						break;
					}
				case 1: // "columns";
					{
						object.columns(args);
						break;
					}
				case 2: // "where";
					{
						object.where(args);
						break;
					}
				case 3: // "execute";
					{
						return object.execute(args);
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
			case 0: return "name";
			case 1: return "columns";
			case 2: return "where";
			case 3: return "execute";
			}
			return "unknown";
		}
	};
}
