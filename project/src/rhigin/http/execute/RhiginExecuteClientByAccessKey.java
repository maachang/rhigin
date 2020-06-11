package rhigin.http.execute;

import java.util.Map;

import rhigin.RhiginException;
import rhigin.http.client.HttpClient;
import rhigin.http.client.HttpResult;
import rhigin.keys.RhiginAccessKeyClient;
import rhigin.keys.RhiginAccessKeyUtil;
import rhigin.scripts.Json;
import rhigin.util.ArrayMap;
import rhigin.util.Converter;

/**
 * RhiginAccessKeyのClient操作.
 * 
 * 指定URLを指定してサーバに対してアクセスキーの生成・削除・確認等が行え、
 * Homeファイルやconf/accessKey.conf などに「情報の登録・削除」等が行えます.
 */
public class RhiginExecuteClientByAccessKey extends RhiginExecuteClient {
	private static final RhiginExecuteClientByAccessKey SNGL = new RhiginExecuteClientByAccessKey();
	
	/**
	 * シングルトンオブジェクトを取得.
	 * @return
	 */
	public static final RhiginExecuteClientByAccessKey getInstance() {
		return SNGL;
	}
	
	/**
	 * 指定URLに対するアクセスキーを作成.
	 * Homeファイルに生成されたアクセスキーは書き込まれます.
	 * 
	 * @param url 対象のURLを設定します.
	 * @return アクセスキーの生成に成功した場合は、アクセスキーと認証コードが返却されます.
	 *         {"accessKey": "....", "authCode": "...."};
	 */
	public Object createByHome(String url) {
		return send(url, "create", null);
	}
	
	/**
	 * 指定URLに対するアクセスキーを作成.
	 * Homeファイルに生成されたアクセスキーは書き込まれます.
	 * 
	 * @param url 対象のURLを設定します.
	 * @param option 対象のOptionを設定します.
	 * @return アクセスキーの生成に成功した場合は、アクセスキーと認証コードが返却されます.
	 *         {"accessKey": "....", "authCode": "...."};
	 */
	public Object createByHome(String url, Map<String, Object> option) {
		return send(url, "create", option);
	}
	
	/**
	 * 指定URLに対するアクセスキーを作成.
	 * conf/accessKey.conf に生成されたアクセスキーは書き込まれます.
	 * 
	 * @param url 対象のURLを設定します.
	 * @return アクセスキーの生成に成功した場合は、アクセスキーと認証コードが返却されます.
	 *         {"accessKey": "....", "authCode": "...."};
	 */
	public Object createByConf(String url) {
		return send(url, "createConf", null);
	}
	
	/**
	 * 指定URLに対するアクセスキーを作成.
	 * conf/accessKey.conf に生成されたアクセスキーは書き込まれます.
	 * 
	 * @param url 対象のURLを設定します.
	 * @param option 対象のOptionを設定します.
	 * @return アクセスキーの生成に成功した場合は、アクセスキーと認証コードが返却されます.
	 *         {"accessKey": "....", "authCode": "...."};
	 */
	public Object createByConf(String url, Map<String, Object> option) {
		return send(url, "createConf", option);
	}
	
	/**
	 * 指定URLのアクセスキーを削除.
	 * 
	 * @param url 対象のURLを設定します.
	 * @return アクセスキーの削除結果が返却されます.
	 *         {"result": true/false, "accessKey": "...."};
	 */
	public Object delete(String url) {
		return delete(url, null, null);
	}
	
	/**
	 * 指定URLのアクセスキーを削除.
	 * 
	 * @param url 対象のURLを設定します.
	 * @param option 対象のOptionを設定します.
	 * @return アクセスキーの削除結果が返却されます.
	 *         {"result": true/false, "accessKey": "...."};
	 */
	public Object delete(String url, Map<String, Object> option) {
		return delete(url, null, option);
	}
	
	/**
	 * 指定URLのアクセスキーを削除.
	 * 
	 * @param url 対象のURLを設定します.
	 * @param accessKey 削除対象のアクセスキーを設定します.
	 * @return アクセスキーの削除結果が返却されます.
	 *         {"result": true/false, "accessKey": "...."};
	 */
	public Object delete(String url, String accessKey) {
		return delete(url, accessKey, null);
	}
	
	/**
	 * 指定URLのアクセスキーを削除.
	 * 
	 * @param url 対象のURLを設定します.
	 * @param accessKey 削除対象のアクセスキーを設定します.
	 * @param option 対象のOptionを設定します.
	 * @return アクセスキーの削除結果が返却されます.
	 *         {"result": true/false, "accessKey": "...."};
	 */
	public Object delete(String url, String accessKey, Map<String, Object> option) {
		if(option == null) {
			option = new ArrayMap<String, Object>();
		}
		if(accessKey != null) {
			if(!RhiginAccessKeyUtil.isAccessKey(accessKey)) {
				throw new RhiginException("The access key is incorrect.");
			}
			option.put("accessKey", accessKey);
		}
		return send(url, "delete", option);
	}
	
	/**
	 * 指定URLのアクセスキーを確認.
	 * 
	 * @param url 対象のURLを設定します.
	 * @return アクセスキーの存在結果が返却されます.
	 *         {"result": true/false, "accessKey": "...."};
	 */
	public Object isAccessKey(String url) {
		return isAccessKey(url, null, null);
	}
	
	/**
	 * 指定URLのアクセスキーを確認.
	 * 
	 * @param url 対象のURLを設定します.
	 * @param option 対象のOptionを設定します.
	 * @return アクセスキーの存在結果が返却されます.
	 *         {"result": true/false, "accessKey": "...."};
	 */
	public Object isAccessKey(String url, Map<String, Object> option) {
		return isAccessKey(url, null, option);
	}
	
	/**
	 * 指定URLのアクセスキーを確認.
	 * 
	 * @param url 対象のURLを設定します.
	 * @param accessKey 対象のアクセスキーを設定します.
	 * @return アクセスキーの存在結果が返却されます.
	 *         {"result": true/false, "accessKey": "...."};
	 */
	public Object isAccessKey(String url, String accessKey) {
		return isAccessKey(url, accessKey, null);
	}
	
	/**
	 * 指定URLのアクセスキーを確認.
	 * 
	 * @param url 対象のURLを設定します.
	 * @param accessKey 対象のアクセスキーを設定します.
	 * @param option 対象のOptionを設定します.
	 * @return アクセスキーの存在結果が返却されます.
	 *         {"result": true/false, "accessKey": "...."};
	 */
	public Object isAccessKey(String url, String accessKey, Map<String, Object> option) {
		if(option == null) {
			option = new ArrayMap<String, Object>();
		}
		if(accessKey != null && !accessKey.isEmpty()) {
			option.put("accessKey", accessKey);
		}
		return send(url, "use", option);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected HttpResult _send(String url, Object value, Map<String, Object> option) {
		boolean toHome = true;
		String accessKey = null;
		RhiginAccessKeyClient ac = RhiginAccessKeyClient.getInstance();
		// valueの存在チェック.
		if(value == null || !(value instanceof String) || ((String)value).isEmpty()) {
			throw new RhiginException("Access Key processing conditions are not set.");
		} else if("create".equals(value = ((String)(value)).trim().toLowerCase()) ||
				"createhome".equals(value) || "createfile".equals(value) ||
				"createconf".equals(value) || "createconfig".equals(value) ||
				"delete".equals(value) || "use".equals(value)) {
			// create系の実行処理.
			if("createhome".equals(value) || "createfile".equals(value) ||
				"createconf".equals(value) || "createconfig".equals(value)) {
				// create / createFile以外の場合はコンフィグ定義に生成条件をセットする.
				if(!("createhome".equals(value) || "createfile".equals(value))) {
					// conf/accessKey.jsonに保存.
					toHome = false;
				}
				value = "create";
			}
			if("delete".equals(value) || "use".equals(value)) {
				String[] keys = null;
				// optionにaccessKeyが存在しない場合は、
				// URL先のサーバに登録されているAccessKeyを取得する.
				if(getAccessKey(false, option) == null) {
					keys = ac.get(url);
				// optionにaccessKeyが存在する場合は、そちらから取得.
				} else {
					keys = new String[] { getAccessKey(true, option) };
					option.remove("akey");
					option.remove("accessKey");
				}
				// 存在しない場合はエラー返却.
				if(keys == null) {
					throw new RhiginException("Access Key is not set.");
				}
				accessKey = keys[0];
			}
			if("create".equals(value)) {
				// 生成系の処理の場合.
				// 指定URLに対して、情報が存在するかチェックして、存在する場合は、エラーを返却.
				if((toHome && ac.getByHome(url) != null) ||
					(!toHome && ac.getByConfig(url) != null)) {
					throw new RhiginException("The access key already exists.");
				}
			}
		} else {
			throw new RhiginException("Unknown execution instruction: " + value);
		}
		// URLに実行命令をセット.
		url += RhiginExecuteConstants.RHIGIN_URL_EXECUTE_HEAD + RhiginExecuteByAccessKey.NAME + "/" + value;
		// アクセスキーが取得できた場合.
		if(accessKey != null) {
			// アクセスキーを追加.
			url += "/" + accessKey;
		}
		try {
			// 最小ヘッダで処理.
			option.put("minHeader", true);
			
			// getで送信.
			HttpResult ret = HttpClient.get(url, option);
			// 処理結果に応じてRhiginAccessClientの情報を操作.
			if(ret.getStatus() < 300) {
				Object json = ret.responseJson();
				// 生成操作.
				if("create".equals(value)) {
					// 生成処理結果をClient管理ファイルに反映.
					if(json instanceof Map) {
						Map<String, String> res = (Map)json;
						if(toHome) {
							// Homeファイルに追加.
							ac.setByHome(url, res.get("accessKey"), res.get("authCode"));
						} else {
							// conf/accessKey.jsonに追加.
							ac.setByConfig(url, res.get("accessKey"), res.get("authCode"));
						}
						return ret;
					}
					throw new RhiginException(
						"Unknown return value for create operation: " + Json.encode(json));
				} else if("delete".equals(value)) {
					// 削除処理結果をClient管理ファイルに反映.
					if(json instanceof Map) {
						Map<String, Object> res = (Map)json;
						// 戻り値とセットしたAccessKeyが一致する場合は削除処理.
						if(Converter.convertBool(res.get("result")) &&
							accessKey != null && accessKey.equals(res.get("accessKey"))) {
							// 削除処理の場合はそれぞれを削除する.
							String[] keys = ac.getByHome(url);
							if(keys != null && accessKey.equals(keys[0])) {
								ac.removeByHome(url);
							}
							keys = ac.getByConfig(url);
							if(keys != null && accessKey.equals(keys[0])) {
								ac.removeByConfig(url);
							}
							return ret;
						}
					}
					throw new RhiginException("The delete process has failed: " + Json.encode(json));
				}
			}
			return ret;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		}
	}
}
