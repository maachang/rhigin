package rhigin.scripts.function;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.http.client.HttpClient;
import rhigin.scripts.RhiginFunction;

public class HttpClientFunction extends RhiginFunction {
	private static final HttpClientFunction THIS = new HttpClientFunction();
	public static final HttpClientFunction getInstance() {
		return THIS;
	}
	
	@Override
	public String getName() {
		return "httpClient";
	}

	@Override
	@SuppressWarnings("rawtypes")
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if(args.length >= 2) {
			String method = "" + args[0];
			String url = "" + args[1];
			Object params = null;
			Map headers = null;
			
			if(args.length >= 3) {
				params = args[2];
				if(args.length >= 4 && args[3] instanceof Map) {
					headers = (Map)args[3];
				}
			}
			try {
				return HttpClient.connect(method, url, params, headers);
			} catch(Exception e) {
				throw new RhiginException(500, e);
			}
		}
		return Undefined.instance;
	}
}