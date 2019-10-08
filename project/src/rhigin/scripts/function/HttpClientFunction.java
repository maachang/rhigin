package rhigin.scripts.function;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.RhiginException;
import rhigin.http.client.HttpClient;
import rhigin.scripts.RhiginFunction;

/**
 * [js]HttpClient.
 * 
 * httpClient(method, url, option)
 *   method: Httpメソッド [GET, POST, DELETE, PUT, PATCH, OPTION]
 *   url: 接続先URL (http://yahoo.co.jp).
 *   option: Mapで設定.
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
	@SuppressWarnings("rawtypes")
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (args.length >= 2) {
			String method = "" + args[0];
			String url = "" + args[1];
			Map option = (args.length >= 3 && args[2] instanceof Map) ? (Map) args[2] : null;
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