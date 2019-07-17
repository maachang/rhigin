package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.util.Converter;

/**
 * [js]sleep処理.
 */
public class SleepFunction extends RhiginFunction {
	private static final SleepFunction THIS = new SleepFunction();
	public static final SleepFunction getInstance() {
		return THIS;
	}
	
	@Override
	public String getName() {
		return "sleep";
	}

	@Override
    public final Object call(Context ctx, Scriptable scope, Scriptable thisObj,
                       Object[] args)
    {
		if(args.length >= 1 && Converter.isFloat(args[0])) {
			try {
				Thread.sleep(Converter.convertInt(args[0]));
			} catch(Exception e) {}
		}
        return Undefined.instance;
    }
}
