package rhigin.keys;

import java.net.URL;
import java.util.Map;

import rhigin.RhiginException;
import rhigin.http.HttpConstants;
import rhigin.scripts.UniqueIdManager;
import rhigin.util.UniqueId;

/**
 * RhiginAccessKeyのユーティリティ.
 */
public class RhiginAccessKeyUtil {
	/**
	 * RhiginAccessKeyを生成.
	 * @return
	 */
	public static final String[] create() {
		UniqueId uid = UniqueIdManager.get();
		return new String[] {
			uid.get64(RhiginAccessKeyConstants.ACCESS_KEY_LENGTH),
			uid.get64(RhiginAccessKeyConstants.AUTH_CODE_LENGTH)
		};
	}
	
	/**
	 * 正しいアクセスキーの形式かチェック.
	 * @param accessKey
	 * @return
	 */
	public static final boolean isAccessKey(Object accessKey) {
		if(accessKey == null || !(accessKey instanceof String)
			|| ((String)accessKey).length() != RhiginAccessKeyConstants.ACCESS_KEY_LENGTH_64) {
			return false;
		}
		return true;
	}
	
	/**
	 * 正しい認証コードの形式かチェック.
	 * @param authCode
	 * @return
	 */
	public static final boolean isAuthCode(Object authCode) {
		if(authCode == null || !(authCode instanceof String)
				|| ((String)authCode).length() != RhiginAccessKeyConstants.AUTH_CODE_LENGTH_64) {
				return false;
			}
			return true;
	}
	
	/**
	 * 正しくないアクセスキーの場合はエラー.
	 * @param accessKey
	 */
	public static final void checkAccessKey(Object accessKey) {
		if(!isAccessKey(accessKey)) {
			throw new RhiginException("Invalid access key set.");
		}
	}
	
	/**
	 * 正しくない認証コードの場合はエラー.
	 * @param authCode
	 */
	public static final void checkAuthCode(Object authCode) {
		if(!isAuthCode(authCode)) {
			throw new RhiginException("Invalid auth code set.");
		}
	}
	
	/**
	 * 指定パラメータ内のAccessKeyとAuthCodeを取得.
	 * @param out HttpClientのオプション定義が返却されます.
	 * @param map 対象のパラメータを設定します.
	 * @param Map 対象のオプションを設定します.
	 */
	public static final void settingAccessKeyAndAuthCode(Map<String, Object> out, String url) {
		settingAccessKeyAndAuthCode(out, url);
	}
	
	/**
	 * 指定パラメータ内のAccessKeyとAuthCodeを取得.
	 * @param out HttpClientのオプション定義が返却されます.
	 * @param map 対象のパラメータを設定します.
	 * @param Map 対象のオプションを設定します.
	 */
	public static final void settingAccessKeyAndAuthCode(Map<String, Object> out, String url, Map<String, Object> map) {
		String akey = null, acode = null;
		if(map != null) {
			Object o;
			// 設定されているアクセスキーと認証コードを取得.
			o = map.get("accessKey");
			if(o == null) {
				o = map.get("akey");
			}
			if(o != null && o instanceof String) {
				akey = (String)o;
			}
			o = map.get("authCode");
			if(o == null) {
				o = map.get("acode");
			}
			if(o != null && o instanceof String) {
				acode = (String)o;
			}
		}
		// optionの定義.
		// AccessKeyの条件が設定されていない場合はRhiginAccessKeyClientからURL指定で条件を取得.
		if(!RhiginAccessKeyUtil.isAccessKey(akey) || !RhiginAccessKeyUtil.isAuthCode(acode)) {
			String[] keys = RhiginAccessKeyClient.getInstance().get(url);
			if(keys == null) {
				throw new RhiginException("AccessKey definition for the specified URL does not exist.");
			}
			akey = keys[0];
			acode = keys[1];
		}
		out.put("accessKey", akey);
		out.put("authCode", acode);
	}
	
	/**
	 * URLからドメイン名+ポート番号を取得.
	 * @param protocol trueの場合はプロトコル付きのURLを取得します.
	 * @param url URLを設定します.
	 * @return String 処理結果が返却されます.
	 */
	public static final String getDomain(boolean protocol, String url) {
		if(url == null || url.isEmpty()) {
			url = "http://127.0.0.1:" + HttpConstants.BIND_PORT;
		}
		if(!url.startsWith("http://") && url.startsWith("https://")) {
			url = "http://" + url;
		}
		try {
			URL u = new URL(url);
			if(u.getPort() == -1 || u.getPort() == 80 || u.getPort() == 443) {
				url = u.getHost();
			} else {
				url = u.getHost() + ":" + u.getPort();
			}
			if(protocol) {
				return u.getProtocol() + "://" + url;
			}
			return url;
		} catch(Exception e) {
			throw new RhiginException(e);
		}
	}
}
