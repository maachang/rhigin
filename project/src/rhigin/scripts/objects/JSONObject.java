package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.scripts.function.AbstractFunction;
import rhigin.util.Json;

/**
 * [js]: Jsonオブジェクト.
 */
public final class JSONObject {
	
	// json エンコード.
	private static final AbstractFunction stringify = new AbstractFunction() {
		@Override
	    public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args)
	    {
			if(args.length >= 1) {
				try {
					return Json.encode(args[0]);
				} catch(Exception e) {
					throw new RhiginException(500, e);
				}
			}
	        return Undefined.instance;
	    }
		@Override
		public final String getName() { return "stringify"; }
	};
	
	// json デコード.
	private static final AbstractFunction parse = new AbstractFunction() {
		@Override
	    public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args)
	    {
			if(args.length >= 1) {
				try {
					return Json.decode(""+args[0]);
				} catch(Exception e) {
					throw new RhiginException(500, e);
				}
			}
	        return Undefined.instance;
	    }
		@Override
		public final String getName() { return "parse"; }
	};
	
	// オブジェクトリスト.
	private static final AbstractFunction[] list = {
		stringify, parse
	};
	
	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject("JSON", list);
	public static final RhiginObject getInstance() {
		return THIS;
	}
}
