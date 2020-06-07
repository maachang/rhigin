package rhigin.scripts.function;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.http.execute.RhiginExecuteClientByJs;
import rhigin.keys.RhiginAccessKeyUtil;
import rhigin.scripts.RhiginFunction;
import rhigin.util.ArrayMap;
import rhigin.util.FileUtil;
import rhigin.util.FixedKeyValues;

/**
 * 指定したサーバにJavaScriptを送信・実行して処理結果を返却します.
 */
public class SendJsFunction extends RhiginFunction {
	private static final String NAME = "sendJs";
	private static final SendJsFunction THIS = new SendJsFunction();

	public static final SendJsFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return NAME;
	}

	// 指定したサーバにJavaScriptを送信・実行して処理結果を返却します.
	// sendjs({ ... });
	//   url : 対象のURLを設定します.
	//         url は [http or https]://[domain or ipAddr]:[port] で設定し
	//         パス設定はしないようにします.
	//   accessKey or akey: アクセスキーを設定します.
	//   authCode or acode: 認証コードを設定します.
	//   script or js: 実行スクリプトを文字列で指定出来ます.
	//   file: 実行スクリプトのファイル名を設定します.
	//
	// accessKey and authCode が設定されない場合は、RhiginAccessKeyClient で管理されたキーを設定します.
	// url を指定しない場合は、デフォルトのURLで接続します.
	// script o file どちらかの設定が必要です.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if(args != null && args.length > 0 && args[0] instanceof Map) {
			Map map = (Map)args[0];
			try {
				String url = getString(map.get("url"));
				String script = getString(map.get("script"));
				if(script == null) {
					script = getString(map.get("js"));
				}
				if(script == null || script.isEmpty()) {
					String file = getString(map.get("file"));
					if(file != null && !file.isEmpty() && FileUtil.isFile(file)) {
						script = FileUtil.getFileString(file, "UTF8");
					}
				}
				// スクリプト実行の条件が存在する場合.
				if(script != null && !script.isEmpty()) {
					// 設定されているアクセスキー取得.
					Map<String, Object> option = new ArrayMap<String, Object>();
					RhiginAccessKeyUtil.settingAccessKeyAndAuthCode(option, url, map);
					// スクリプトの送信.
					return RhiginExecuteClientByJs.getInstance().send(url, script, option);
				} else {
					throw new RhiginException("The script to execute remotely does not exist.");
				}
			} catch(RhiginException re) {
				throw re;
			} catch(Exception e) {
				throw new RhiginException(e);
			}
		}
		this.argsException();
		return Undefined.instance;
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put(NAME, scope, SendJsFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(NAME, SendJsFunction.getInstance());
	}


}
