package rhigin;

import java.io.BufferedReader;
import java.io.File;
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
import rhigin.scripts.ScriptConstants;
import rhigin.util.Args;
import rhigin.util.EnvCache;
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
	 * @param args
	 * @return RhiginConfig
	 */
	public static final RhiginConfig initLogFactory(boolean server, String[] args) {
		// Args管理オブジェクトにセット.
		Args.set(args);
		RhiginConfig config = null;
		try {
			// 環境変数から、rhigin起動環境を取得.
			String rhiginEnv = EnvCache.get(RhiginConstants.ENV_ENV);
			
			// コンフィグ読み込みディレクトリ先を、rhigin起動環境に合わせる.
			String confDir = RhiginConstants.DIR_CONFIG;
			if(rhiginEnv != null && rhiginEnv.length() != 0) {
				confDir += rhiginEnv + "/";
				// 対象フォルダが存在しない、対象フォルダ以下のコンフィグ情報が０件の場合は
				// エラーで終了.
				File confStat = new File(confDir);
				if(!confStat.isDirectory() ||
					confStat.list() == null || confStat.list().length == 0) {
					System.out.println(
						"error: There is no configuration definition for the execution environment:" + confDir);
					System.exit(1);
					return null;
				}
				confStat = null;
			}
			
			// メインコンフィグファイルが存在するかチェック.
			config = new RhiginConfig(confDir);
			
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
		//if(Converter.isNumeric(config.get("rhigin", "threadPoolSize"))) {
		//	RhiginThreadPool.getInstance().newThreadPool(
		//		config.getInt("rhigin", "threadPoolSize"));
		//} else {
		//	RhiginThreadPool.getInstance().newThreadPool();
		//}
		
		// ExecuteScriptにRhiginConfigの要素をセット.
		ExecuteScript.addOriginals("config", config);
		
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
