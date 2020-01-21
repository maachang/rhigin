package rhigin.scripts.objects;

import java.io.StringReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginFunction;

/**
 * Functionオブジェクト.
 * 
 * rhiginでは、スクリプトの拡張を行っているので、Functionの命令はExecuteScriptで処理する.
 */
public final class FunctionObject {
	
	// インスタンス生成用オブジェクト.
	private static final class Instance extends RhiginFunction {
		@Override
		public Scriptable jconstruct(Context ctx, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0 || !(args[args.length - 1] instanceof String)) {
				argsException("Function");
				return null;
			}
			try {
				StringBuilder buf = new StringBuilder("function(");
				int len = args.length - 1;
				for(int i = 0; i < len; i ++) {
					if(i != 0) {
						buf.append(", ");
					}
					buf.append(args[i]);
				}
				buf.append(") {\n").append(args[len]).append("\n}");
				return new Execute(ExecuteScript.execute(
						ExecuteScript.currentRhiginContext(),
						new StringReader("" + buf.toString()), null, "", "", 1));
				
			} catch(Exception e) {
				throw new RhiginException(e);
			}
		}

		@Override
		public final String getName() {
			return "Function";
		}

		@Override
		public Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return Undefined.instance;
		}
	};
	
	// Function実行用.
	private static final class Execute extends RhiginFunction {
		private Function sc = null;
		Execute(Object o) {
			sc = (Function)o;
		}
		
		@Override
		public String getName() {
			return "Function";
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return sc.call(ctx, scope, thisObj, args);
		}

	}
	
	private static final Instance THIS = new Instance();
	public static final RhiginFunction getInstance() {
		return THIS;
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("Function", scope, FunctionObject.getInstance());
	}
}
