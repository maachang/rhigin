package rhigin.lib;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.RhiginException;
import rhigin.lib.jdbc.runner.JDBCConnect.Delete;
import rhigin.lib.jdbc.runner.JDBCConnect.Insert;
import rhigin.lib.jdbc.runner.JDBCConnect.Select;
import rhigin.lib.jdbc.runner.JDBCConnect.Update;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginInstanceObject;
import rhigin.scripts.RhiginInstanceObject.ObjectFunction;
import rhigin.util.FixedSearchArray;

/**
 * [js]jdbc クエリ系.
 */
class JDBCQueryJs {
	
	// JDBCSelectメソッド名群.
	private static final String[] JDBC_SELECT_NAMES = new String[] {
		"name"
		,"columns"
		,"where"
		,"groupby"
		,"having"
		,"orderby"
		,"ather"
		,"range"
		,"offset"
		,"limit"
		,"execute"
		,"toString"
	};
	
	// JDBCSelectメソッド生成処理.
	private static final ObjectFunction JDBC_SELECT_FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(JDBC_SELECT_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new SelectFunction(no, (Select)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};
	
	/**
	 * Selectオブジェクトを生成.
	 * @param o
	 * @return
	 */
	public static final RhiginInstanceObject createSelect(Select o) {
		return new RhiginInstanceObject("Select", JDBC_SELECT_FUNCTIONS, o);
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
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
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
						return JDBCOperatorJs.createRow(object.execute(args));
					}
				case 11: // "toString";
					{
						return object.toString();
					}
				}
			} catch (RhiginException re) {
				throw re;
			} catch (Exception e) {
				throw new RhiginException(500, e);
			}
			return PARENT;
		}
		
		@Override
		public final String getName() {
			return JDBC_SELECT_NAMES[type];
		}
	};
	
	// JDBCDeleteメソッド名群.
	private static final String[] JDBC_DELETE_NAMES = new String[] {
		"name"
		,"where"
		,"execute"
	};
	
	// JDBCDeleteメソッド生成処理.
	private static final ObjectFunction JDBC_DELETE_FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(JDBC_DELETE_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new DeleteFunction(no, (Delete)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};
	
	/**
	 * Deleteオブジェクトを生成.
	 * @param o
	 * @return
	 */
	public static final RhiginInstanceObject createDelete(Delete o) {
		return new RhiginInstanceObject("Delete", JDBC_DELETE_FUNCTIONS, o);
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
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
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
			return PARENT;
		}
		
		@Override
		public final String getName() {
			return JDBC_DELETE_NAMES[type];
		}
	};
	
	// JDBCInsertメソッド名群.
	private static final String[] JDBC_INSERT_NAMES = new String[] {
		"name"
		,"execute"
	};
	
	// JDBCDeleteメソッド生成処理.
	private static final ObjectFunction JDBC_INSERT_FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(JDBC_INSERT_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new InsertFunction(no, (Insert)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};
	
	/**
	 * Insertオブジェクトを生成.
	 * @param o
	 * @return
	 */
	public static final RhiginInstanceObject createInsert(Insert o) {
		return new RhiginInstanceObject("Insert", JDBC_INSERT_FUNCTIONS, o);
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
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
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
			return PARENT;
		}
		
		@Override
		public final String getName() {
			return JDBC_INSERT_NAMES[type];
		}
	};
	
	// JDBCUpdateメソッド名群.
	private static final String[] JDBC_UPDATE_NAMES = new String[] {
		"name"
		,"set"
		,"where"
		,"execute"
	};
	
	// JDBCUpdateメソッド生成処理.
	private static final ObjectFunction JDBC_UPDATE_FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(JDBC_UPDATE_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new UpdateFunction(no, (Update)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};

	
	/**
	 * Updateオブジェクトを生成.
	 * @param o
	 * @return
	 */
	public static final RhiginInstanceObject createUpdate(Update o) {
		return new RhiginInstanceObject("Update", JDBC_UPDATE_FUNCTIONS, o);
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
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
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
				case 1: // "set";
					{
						object.set(args);
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
			return PARENT;
		}
		
		@Override
		public final String getName() {
			return JDBC_UPDATE_NAMES[type];
		}
	};
}
