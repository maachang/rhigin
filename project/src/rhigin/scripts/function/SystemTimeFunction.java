package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.scripts.RhiginFunction;
import rhigin.util.FixedKeyValues;

/**
 * [js]Unix時間を取得します.
 * 
 * systemTime();
 */
public class SystemTimeFunction extends RhiginFunction {
	private static final SystemTimeFunction THIS = new SystemTimeFunction();

	public static final SystemTimeFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "systemTime";
	}

	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		return System.currentTimeMillis();
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("systemTime", scope, SystemTimeFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("systemTime", SystemTimeFunction.getInstance());
	}
}
