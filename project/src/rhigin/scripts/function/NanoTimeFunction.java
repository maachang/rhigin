package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.scripts.RhiginFunction;

/**
 * [js]ナノタイムを取得します.
 *
 * nanoTime();
 */
public class NanoTimeFunction extends RhiginFunction {
	private static final NanoTimeFunction THIS = new NanoTimeFunction();

	public static final NanoTimeFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "nanoTime";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		return System.nanoTime();
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("nanoTime", scope, NanoTimeFunction.getInstance());
	}
}
