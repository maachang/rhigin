package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.scripts.RhiginFunction;

public class GetClassFunction extends RhiginFunction {
	private static final GetClassFunction THIS = new GetClassFunction();

	public static final GetClassFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "getClass";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (args.length >= 1) {
			return args[0] == null ? "null" : args[0].getClass().getName();
		}
		return argsException();
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("getClass", scope, GetClassFunction.getInstance());
	}
}