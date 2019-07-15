package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.util.Base64;

/**
 * [js]base64エンコード.
 */
public class BtoaFunction extends RhiginFunction {
	private static final BtoaFunction THIS = new BtoaFunction();
	public static final BtoaFunction getInstance() {
		return THIS;
	}
	
	@Override
	public String getName() {
		return "btoa";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if(args.length >= 1) {
			if(args[0] instanceof byte[]) {
				return Base64.encode((byte[])args[0]);
			}
			return Base64.btoa(""+args[0]);
		}
		return Undefined.instance;
	}

}
