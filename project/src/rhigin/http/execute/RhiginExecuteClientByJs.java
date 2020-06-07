package rhigin.http.execute;

import java.util.Map;

import rhigin.RhiginException;
import rhigin.http.client.HttpClient;
import rhigin.http.client.HttpResult;
import rhigin.keys.RhiginAccessKeyUtil;
import rhigin.util.ArrayMap;

/**
 * Rhigin実行命令クライアント.
 */
public class RhiginExecuteClientByJs extends RhiginExecuteClient {
	private static final RhiginExecuteClientByJs SNGL = new RhiginExecuteClientByJs();
	
	/**
	 * シングルトンオブジェクトを取得.
	 * @return
	 */
	public static final RhiginExecuteClientByJs getInstance() {
		return SNGL;
	}
	
	@Override
	protected HttpResult _send(String url, Object value, Map<String, Object> option) {
		// RhiginAccessKeyClientで管理しているAccessKeyをセット.
		RhiginAccessKeyUtil.settingAccessKeyAndAuthCode(option, url, option);
		// valueの存在チェック.
		if(value == null || !(value instanceof String) || ((String)value).isEmpty()) {
			throw new RhiginException("The javascript information to execute does not exist.");
		}
		// URLに実行命令をセット.
		url += RhiginExecuteConstants.RHIGIN_URL_EXECUTE_HEAD + RhiginExecuteByJs.NAME;
		if(option == null) {
			option = new ArrayMap<String, Object>();
		}
		try {
			// 最小ヘッダで処理.
			option.put("minHeader", true);
			
			// 送信対象のパラメータをセット.
			option.put("params", value);
			
			// postで送信.
			return HttpClient.post(url, option);
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		}
	}
}
