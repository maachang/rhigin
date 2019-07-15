package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.util.Base64;

/**
 * [js]base64デコード.
 */
public class AtobFunction extends RhiginFunction {
	private static final AtobFunction THIS = new AtobFunction();
	public static final AtobFunction getInstance() {
		return THIS;
	}
	
	@Override
	public String getName() {
		return "atob";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if(args.length >= 1) {
			if(args.length >= 2) {
				boolean binaryFlag = (args[1] instanceof Boolean) ? (boolean)args[1] : false;
				if(binaryFlag) {
					return Base64.decode(""+args[0]);
				}
			}
			return Base64.atob(""+args[0]);
		}
		return Undefined.instance;
	}
}
