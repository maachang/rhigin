package rhigin.scripts.function;

import java.io.StringReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginFunction;
import rhigin.util.FixedKeyValues;

/**
 * eval実行.
 * 
 * rhiginでは、スクリプトの拡張を行っているので、evalの命令はExecuteScriptで処理する.
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
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if(args != null && args.length > 0) {
			try {
				return ExecuteScript.eval(
					new StringReader("" + args[0]), null, "", "", 1);
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
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("eval", EvalFunction.getInstance());
	}

}
