package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.util.EnvCache;

/**
 * [js]環境変数を読み込む.
 * 
 * getEnv("RHIGIN_HOME");
 */
public class GetEnvFunction extends RhiginFunction {
	private static final GetEnvFunction THIS = new GetEnvFunction();
	public static final GetEnvFunction getInstance() {
		return THIS;
	}
	
	@Override
	public String getName() {
		return "getEnv";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if(args.length >= 1) {
			return EnvCache.get(""+args[0]);
		}
		return Undefined.instance;
	}
}