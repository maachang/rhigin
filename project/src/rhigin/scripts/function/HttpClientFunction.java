package rhigin.scripts.function;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.RhiginException;
import rhigin.http.client.HttpClient;
import rhigin.scripts.RhiginFunction;
import rhigin.util.ArrayMap;

/**
 * [js]HttpClient.
 * 
 * httpClient(method, url, option)
 *   method: Httpメソッド [GET, POST, DELETE, PUT, PATCH, OPTION]
 *   url: 接続先URL (http://yahoo.co.jp).
 *   option: Map or [key, value... ] で設定.
 *     params: パラメータを設定する場合は、この名前で設定します.
 *     header: 追加のHTTPヘッダ情報を設定する場合は、この名前でMapで設定します.
 *     bodyFile: HTTPレスポンスのデータをファイルで格納させたい場合は[true]を設定します.
 */
public class HttpClientFunction extends RhiginFunction {
	private static final HttpClientFunction THIS = new HttpClientFunction();

	public static final HttpClientFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "httpClient";
	}

	/**
	 * HttpClient接続.
	 * 
	 * @param ctx
	 * @param scope
	 * @param thisObj
	 * @param args
	 *            args[0]: [String] method 対象のMethodを設定します.
	 *            args[1]: [String] url 対象のURLを設定します.
	 *            args[2]: [Map] option 対象のオプションを設定します.
	 *              params: パラメータを設定する場合は、この名前で設定します.
	 *              header: 追加のHTTPヘッダ情報を設定する場合は、この名前でMapで設定します.
	 *              bodyFile: HTTPレスポンスのデータをファイルで格納させたい場合は[true]を設定します.
	 * @return HttpResult 返却データが返されます.
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (args.length >= 2) {
			String method = "" + args[0];
			String url = "" + args[1];
			Map option = null;
			int len = args.length;
			// オプションが設定されている場合.
			if(len >= 3) {
				// mapの場合.
				if(args[2] instanceof Map) {
					option = (Map)args[2];
				// key, value ... の場合.
				} else if(len >= 4) {
					option = new ArrayMap();
					for(int i = 2; i < len; i += 2) {
						option.put("" + args[i], args[i+1]);
					}
				}
			}
			try {
				return HttpClient.connect(method, url, option);
			} catch (Exception e) {
				throw new RhiginException(500, e);
			}
		}
		return argsException();
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("httpClient", scope, HttpClientFunction.getInstance());
	}
}