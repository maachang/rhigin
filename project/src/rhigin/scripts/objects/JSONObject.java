package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.scripts.Json;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;

/**
 * [js]: Jsonオブジェクト.
 */
public final class JSONObject {
	
	// json エンコード.
	private static final RhiginFunction stringify = new RhiginFunction() {
		@Override
	    public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args)
	    {
			if(args.length >= 1) {
				return Json.encode(args[0]);
			}
	        return Undefined.instance;
	    }
		@Override
		public final String getName() { return "stringify"; }
	};
	
	// json デコード.
	private static final RhiginFunction parse = new RhiginFunction() {
		@Override
	    public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args)
	    {
			if(args.length >= 1) {
				return Json.decode(""+args[0]);
			}
	        return Undefined.instance;
	    }
		@Override
		public final String getName() { return "parse"; }
	};
	
	// オブジェクトリスト.
	private static final RhiginFunction[] list = {
		stringify, parse
	};
	
	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject("JSON", list);
	public static final RhiginObject getInstance() {
		return THIS;
	}
}
