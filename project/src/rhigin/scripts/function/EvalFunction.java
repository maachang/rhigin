package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginFunction;

/**
 * eval実行.
 * 
 * rhiginでは、スクリプトの拡張を行っているので、ExecuteScriptで処理する.
 */
public class EvalFunction extends RhiginFunction {
	private static final EvalFunction THIS = new EvalFunction();

	public static final EvalFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "eval";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if(args != null && args.length > 0) {
			try {
				return ExecuteScript.execute(ExecuteScript.currentRhiginContext(), "" + args[0]);
			} catch(Exception e) {
				throw new RhiginException(e);
			}
		}
		return Undefined.instance;
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("eval", scope, EvalFunction.getInstance());
	}
}
