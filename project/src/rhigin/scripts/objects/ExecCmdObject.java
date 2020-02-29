package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.scripts.JavaScriptable;
import rhigin.scripts.JsonOut;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginInstanceObject;
import rhigin.scripts.RhiginInstanceObject.ObjectFunction;
import rhigin.util.ArrayMap;
import rhigin.util.Converter;
import rhigin.util.ExecCmd;
import rhigin.util.ExecCmd.ResultCmd;
import rhigin.util.FixedKeyValues;
import rhigin.util.FixedSearchArray;

/**
 * [js]外部コマンド実行オブジェクト.
 */
public class ExecCmdObject {
	public static final String OBJECT_NAME = "ExecCmd";
	
	// ExecCmdメソッド名群.
	private static final String[] EXEC_CMD_NAMES = new String[] {
		"path"
		,"env"
		,"timeout"
		,"exec"
	};
	
	// ExecCmdメソッド生成処理.
	private static final ObjectFunction EXEC_CMD_FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(EXEC_CMD_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new ExecCmdFunctions(no, (ExecCmd)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};
	
	/**
	 * ExecCmdを生成.
	 * @param c
	 * @return
	 */
	public static final RhiginInstanceObject create(ExecCmd c) {
		return new RhiginInstanceObject(OBJECT_NAME, EXEC_CMD_FUNCTIONS, c);
	}
	
	// ExecCmdメソッド群. 
	private static final class ExecCmdFunctions extends RhiginFunction {
		private final int type;
		private final ExecCmd cmd;

		ExecCmdFunctions(int t, ExecCmd o) {
			this.type = t;
			this.cmd = o;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // path.
				{
					if(args.length > 0) {
						cmd.setPath("" + args[0]);
					} else {
						return cmd.getPath();
					}
				}
				return PARENT;
				case 1: // env.
				{
					if(args.length > 0) {
						cmd.setEnv(args);
					} else {
						return new JavaScriptable.ReadArray(cmd.getEnv());
					}
				}
				return PARENT;
				case 2: // timeout.
				{
					if(args.length > 0 && Converter.isNumeric(args[0])) {
						cmd.setTimeout(Converter.convertLong(args[0]));
					} else {
						return cmd.getTimeout();
					}
				}
				return PARENT;
				case 3: // exec.
				{
					if(args.length > 0) {
						int len = args.length;
						String[] cmdList = new String[len];
						for(int i = 0; i < len; i ++) {
							cmdList[i] = "" + args[i];
						}
						return create(cmd.exec(cmdList));
					} else {
						this.argsException(OBJECT_NAME);
					}
				}
				break;
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
			return EXEC_CMD_NAMES[type];
		}
	};
	
	// インスタンス生成用オブジェクト.
	private static final class Instance extends RhiginFunction {
		@Override
		public Scriptable jconstruct(Context ctx, Scriptable thisObj, Object[] args) {
			ExecCmd cmd;
			if(args.length > 0 && args[0] instanceof String) {
				cmd = new ExecCmd((String)args[0]);
			} else {
				cmd = new ExecCmd();
			}
			return ExecCmdObject.create(cmd);
		}

		@Override
		public final String getName() {
			return OBJECT_NAME;
		}

		@Override
		public Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return Undefined.instance;
		}
	};

	// ExecCmdObject.
	private static final Instance THIS = new Instance();
	public static final RhiginFunction getInstance() {
		return THIS;
	}
	
	// ResultCmd.
	public static final String RESULT_CMD_NAME = "ResultCmd";
	
	// ResultCmdメソッド名群.
	private static final String[] RESULT_CMD_NAMES = new String[] {
		"result"
		,"out"
		,"isOut"
		,"toString"
	};
	
	// ResultCmdメソッド生成処理.
	private static final ObjectFunction RESULT_CMD_FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(RESULT_CMD_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new ResultCmdFunctions(no, (ResultCmd)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};
	
	/**
	 * ResultCmdを生成.
	 * @param c
	 * @return
	 */
	public static final RhiginInstanceObject create(ResultCmd c) {
		return new RhiginInstanceObject(RESULT_CMD_NAME, RESULT_CMD_FUNCTIONS, c);
	}
	
	// ResultCmdメソッド群. 
	private static final class ResultCmdFunctions extends RhiginFunction {
		private final int type;
		private final ResultCmd cmd;

		ResultCmdFunctions(int t, ResultCmd o) {
			this.type = t;
			this.cmd = o;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // result.
				{
					return cmd.result();
				}
				case 1: // out.
				{
					return cmd.getOut();
				}
				case 2: // isOut.
				{
					return cmd.isOut();
				}
				case 3: // toString.
				{
					return JsonOut.toString(new ArrayMap<String, Object>(
						"result", cmd.result(),
						"out", cmd.getOut()
					));
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
			return RESULT_CMD_NAMES[type];
		}
	};
	

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put(OBJECT_NAME, scope, ExecCmdObject.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(OBJECT_NAME, ExecCmdObject.getInstance());
	}
}
