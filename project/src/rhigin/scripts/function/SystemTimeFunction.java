package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.scripts.RhiginFunction;

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
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		return System.currentTimeMillis();
	}
}
