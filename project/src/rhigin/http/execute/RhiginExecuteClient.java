package rhigin.http.execute;

import java.util.Map;

import rhigin.RhiginException;
import rhigin.http.client.HttpResult;
import rhigin.keys.RhiginAccessKeyUtil;
import rhigin.scripts.Json;
import rhigin.util.ArrayMap;

/**
 * Rhigin実行命令クライアント.
 */
public abstract class RhiginExecuteClient {
	/**
	 * サーバ接続.
	 * 
	 * @param value 送信情報を設定します.
	 * @return Object 処理結果が返却されます.
	 */
	public Object send(Object value) {
		return send(null, value, null);
	}

	/**
	 * サーバ接続.
	 * 
	 * @param value 送信情報を設定します.
	 * @oaram option 対象のオプションを設定します.
	 *               params: パラメータを設定する場合は、この名前で設定します.
	 *               header: 追加のHTTPヘッダ情報を設定する場合は、この名前でMapで設定します.
	 *               bodyFile: HTTPレスポンスのデータをファイルで格納させたい場合は[true]を設定します.
	 *               minHeader: rhiginサーバに最小のヘッダで通信をする場合は true を設定します.
	 *               blowser: rhiginサーバにアクセスする場合、falseをセットすることで、HTTPレスポンスヘッダ
	 *                        の量を少し減らせます.
	 * @return Object 処理結果が返却されます.
	 */
	public Object send(Object value, Map<String, Object> option) {
		return send(null, value, option);
	}
	
	/**
	 * サーバ接続.
	 * 
	 * @param url
	 *            対象のURLを設定します.
	 * @param value 送信情報を設定します.
	 * @return Object 処理結果が返却されます.
	 */
	public Object send(String url, Object value) {
		return send(url, value, null);
	}
	
	/**
	 * サーバ接続.
	 * 
	 * @param url
	 *            対象のURLを設定します.
	 * @param value 送信情報を設定します.
	 * @oaram option 対象のオプションを設定します.
	 *               params: パラメータを設定する場合は、この名前で設定します.
	 *               header: 追加のHTTPヘッダ情報を設定する場合は、この名前でMapで設定します.
	 *               bodyFile: HTTPレスポンスのデータをファイルで格納させたい場合は[true]を設定します.
	 *               minHeader: rhiginサーバに最小のヘッダで通信をする場合は true を設定します.
	 *               blowser: rhiginサーバにアクセスする場合、falseをセットすることで、HTTPレスポンスヘッダ
	 *                        の量を少し減らせます.
	 * @return Object 処理結果が返却されます.
	 */
	@SuppressWarnings("rawtypes")
	public Object send(String url, Object value, Map<String, Object> option) {
		if(option == null) {
			option = new ArrayMap<String, Object>();
		}
		HttpResult res = _send(RhiginAccessKeyUtil.getDomain(true, url), value, option);
		// 処理結果がエラーステータスの場合はエラー返却.
		if(res.getStatus() >= 400) {
			Object json = res.responseJson();
			if(json instanceof Map && ((Map)json).containsKey("message")) {
				throw new RhiginException(res.getStatus(), "" + ((Map)json).get("message"));
			} else {
				throw new RhiginException(res.getStatus(), Json.encode(json));
			}
		}
		return res.responseJson();
	}
	
	/**
	 * サーバ接続＋実装.
	 * @param url
	 *            対象のURLを設定します.
	 * @param value 送信情報を設定します.
	 * @oaram option 対象のオプションを設定します.
	 *               params: パラメータを設定する場合は、この名前で設定します.
	 *               header: 追加のHTTPヘッダ情報を設定する場合は、この名前でMapで設定します.
	 *               bodyFile: HTTPレスポンスのデータをファイルで格納させたい場合は[true]を設定します.
	 *               minHeader: rhiginサーバに最小のヘッダで通信をする場合は true を設定します.
	 *               blowser: rhiginサーバにアクセスする場合、falseをセットすることで、HTTPレスポンスヘッダ
	 *                        の量を少し減らせます.
	 * @return HttpResult 処理結果が返却されます.
	 */
	protected abstract HttpResult _send(String url, Object value, Map<String, Object> option);
	
	/**
	 * アクセスキーを取得.
	 * @param check [true]の場合は存在しない場合例外発生します.
	 * @param option optionを設定します.
	 * @return String 文字列が返却された場合は、存在します.
	 */
	protected static final String getAccessKey(boolean check, Map<String, Object> option) {
		Object o = option.get("accessKey");
		if(o == null) {
			o = option.get("akey");
		}
		if(check) {
			RhiginAccessKeyUtil.checkAccessKey(o);
		} else {
			if(RhiginAccessKeyUtil.isAccessKey(o)) {
				return (String)o;
			}
			return null;
		}
		return (String)o;
	}
	
	/**
	 * 認証コードを取得.
	 * @param check [true]の場合は存在しない場合例外発生します.
	 * @param option optionを設定します.
	 * @return String 文字列が返却された場合は、存在します.
	 */
	protected static final String getAuthCode(boolean check, Map<String, Object> option) {
		Object o = option.get("authCode");
		if(o == null) {
			o = option.get("acode");
		}
		if(check) {
			RhiginAccessKeyUtil.checkAuthCode(o);
		} else {
			if(RhiginAccessKeyUtil.isAuthCode(o)) {
				return (String)o;
			}
			return null;
		}
		return (String)o;
	}
	
	/**
	 * optionからHttpHeaderを取得.
	 * @param option オプションを設定します.
	 * @return Map HttpHeaderを設定します.
	 */
	@SuppressWarnings("rawtypes")
	protected static final Map getHeader(Map<String, Object> option) {
		// ヘッダにアクセスキーをセット.
		Map header = (Map)option.get("header");
		if(header == null) {
			header = (Map)option.get("headers");
			if(header == null) {
				header = new ArrayMap();
				option.put("header", header);
			}
		}
		return header;
	}
}
