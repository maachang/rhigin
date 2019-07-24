package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;

/**
 * ガベージコレクション呼び出し.
 */
public class GcFunction extends RhiginFunction {
	private static final GcFunction THIS = new GcFunction();
	public static final GcFunction getInstance() {
		return THIS;
	}
	
	@Override
	public String getName() {
		return "gc";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		System.gc();
		return Undefined.instance;
	}
}
