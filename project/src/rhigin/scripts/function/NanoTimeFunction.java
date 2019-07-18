package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.scripts.RhiginFunction;

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
}
