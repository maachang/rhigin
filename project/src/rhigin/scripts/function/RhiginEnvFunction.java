package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.RhiginStartup;
import rhigin.scripts.RhiginFunction;
import rhigin.util.FixedKeyValues;

/**
 * [js]RhiginEnvを取得.
 */
public class RhiginEnvFunction extends RhiginFunction {
	private static final RhiginEnvFunction THIS = new RhiginEnvFunction();

	public static final RhiginEnvFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "rhiginEnv";
	}

	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		final String ret = RhiginStartup.getRhiginEnv();
		return ret == null ? "" : ret;
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("rhiginEnv", scope, RhiginEnvFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("rhiginEnv", RhiginEnvFunction.getInstance());
	}

}