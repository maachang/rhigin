package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginFunction;

/**
 * ExecuteScript.currentRhiginContextを取得.
 */
public class GlobalFunction extends RhiginFunction {
	private static final GlobalFunction THIS = new GlobalFunction();
	public static final GlobalFunction getInstance() {
		return THIS;
	}
	
	@Override
	public String getName() {
		return "global";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		return ExecuteScript.currentRhiginContext();
	}
}
