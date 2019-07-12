package rhigin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.http.HttpInfo;
import rhigin.logs.LogFactory;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginThreadPool;
import rhigin.scripts.ScriptConstants;
import rhigin.util.Converter;
import rhigin.util.FileUtil;

/**
 * Rhigin初期処理.
 */
public class RhiginStartup {
	protected RhiginStartup() {}
	
	/** サーバ起動時のスクリプトファイル名. **/
	public static final String STARTUP_JS = "./index.js";
	
	/** スタートアップオブジェクト一時格納先. **/
	private static final String STARTUP_OBJECT = "_originals";
	
	/**
	 * ログファクトリの初期化.
	 * @param server
	 * @return RhiginConfig
	 */
	public static final RhiginConfig initLogFactory(boolean server) {
		RhiginConfig config = null;
		try {
			// メインコンフィグファイルが存在するかチェック.
			config = new RhiginConfig();
			
			// ログファクトリの初期化.
			if(config.has("logger")) {
				LogFactory.setting(config.get("logger"));
			}
		} catch(Exception e) {
			// エラーが出る場合は、処理終了.
			e.printStackTrace();
			System.exit(1);
		}
		return config;
	}
	
	/**
	 * スタートアップ処理.
	 * @param server
	 * @param config
	 * @return HttpInfo
	 * @exception Exception
	 */
	public static final HttpInfo startup(boolean server, RhiginConfig config)
		throws Exception {
		
		// スレッドプーリングの初期化.
		if(Converter.isNumeric(config.get("rhigin", "threadPoolSize"))) {
			RhiginThreadPool.getInstance().newThreadPool(
				config.getInt("rhigin", "threadPoolSize"));
		} else {
			RhiginThreadPool.getInstance().newThreadPool();
		}
		
		// ExecuteScriptにRhiginConfigの要素をセット.
		ExecuteScript.addOriginals("conf", config);
		
		// 初期設定用のスクリプト実行.
		if(FileUtil.isFile(STARTUP_JS)) {
			// スクリプト実行用のコンテキスト.
			// スタートアップ専用のオブジェクトは、ここに設定する.
			RhiginContext context = new RhiginContext();
			
			// スタートアップで、ExecuteScript実行時に利用可能にしたいオブジェクトを設定.
			Map<String,Object> originals = new HashMap<String,Object>();
			context.setAttribute(STARTUP_OBJECT, originals);
			context.setAttribute(addOrigin.getName(), addOrigin);
			context.setAttribute(removeOrigin.getName(), removeOrigin);
			
			Reader r = null;
			try {
				r = new BufferedReader(new InputStreamReader(new FileInputStream(STARTUP_JS), "UTF8"));
				ExecuteScript.execute(context, r, STARTUP_JS, ScriptConstants.HEADER, ScriptConstants.FOOTER, 0);
				r.close(); r = null;
				
				// ExecuteScript実行時に利用可能にしたいオブジェクトをExecuteScript.addOriginalsで追加.
				Entry<String,Object> et;
				Iterator<Entry<String,Object>> it = originals.entrySet().iterator();
				while(it.hasNext()) {
					et = it.next();
					ExecuteScript.addOriginals(et.getKey(), et.getValue());
				}
			} finally {
				if(r != null) {
					try {r.close();} catch(Exception e) {}
				}
			}
		}
		
		// HttpInfoを生成して返却.
		HttpInfo httpInfo = new HttpInfo();
		HttpInfo.load(httpInfo, config.get("http"));
		return httpInfo;
	}
	
	// scopeからスタートアップオブジェクト追加先を取得.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Map<String,Object> getOriginal(Scriptable scope) {
		return (Map)scope.get(STARTUP_OBJECT, null);
	}
	
	// スタートアップオブジェクト追加.
	private static final RhiginFunction addOrigin= new RhiginFunction() {
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args)
		{
			if(args.length >= 2) {
				try {
					getOriginal(scope).put(""+args[0], args[1]);
				} catch(Exception e) {
					throw new RhiginException(500, e);
				}
			}
			return Undefined.instance;
		}
		@Override
		public final String getName() { return "addOriginals"; }
	};
	
	// スタートアップオブジェクト削除.
	private static final RhiginFunction removeOrigin = new RhiginFunction() {
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args)
		{
			if(args.length >= 2) {
				try {
					getOriginal(scope).remove(""+args[0], args[1]);
				} catch(Exception e) {
					throw new RhiginException(500, e);
				}
			}
			return Undefined.instance;
		}
		@Override
		public final String getName() { return "removeOriginals"; }
	};
	
}
