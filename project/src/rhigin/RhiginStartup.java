package rhigin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.http.Http;
import rhigin.http.HttpInfo;
import rhigin.http.MimeType;
import rhigin.logs.LogFactory;
import rhigin.scripts.ExecuteJsByEndScriptCall;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.ScriptConstants;
import rhigin.scripts.objects.LockObjects;
import rhigin.util.Args;
import rhigin.util.ArrayMap;
import rhigin.util.EnvCache;
import rhigin.util.FileUtil;
import rhigin.util.OList;
import rhigin.util.ObjectList;

/**
 * Rhigin初期処理.
 */
public class RhiginStartup {
	protected RhiginStartup() {
	}

	/** サーバ起動時のスクリプトファイル名. **/
	public static final String STARTUP_JS = "./index.js";

	/** スタートアップオブジェクト一時格納先. **/
	private static final String STARTUP_OBJECT = "_$originals";
	
	/** スクリプト実行後の終了処理を一時格納先. **/
	private static final String STARTUP_END_CALL_SCRIPT = "_$endCallScripts";

	/**
	 * ログファクトリの初期化.
	 * 
	 * @param server
	 * @param args
	 * @return RhiginConfig
	 */
	public static final RhiginConfig initLogFactory(boolean server, String[] args) {
		return initLogFactory(server, false, args);
	}
	
	/**
	 * ログファクトリの初期化.
	 * 
	 * @param server
	 * @param noScript
	 * @param args
	 * @return RhiginConfig
	 */
	public static final RhiginConfig initLogFactory(boolean server, boolean noScript, String[] args) {
		// Args管理オブジェクトにセット.
		Args.set(args);
		RhiginConfig config = null;
		try {
			// webServerモードをセット.
			Http.setWebServerMode(server);

			// function, objectの初期化.
			if(!noScript) {
				initRhiginScriptFunctionObject();
			}

			// 環境変数から、rhigin起動環境を取得.
			String rhiginEnv = EnvCache.get(RhiginConstants.ENV_ENV);

			// コンフィグ読み込みディレクトリ先を、rhigin起動環境に合わせる.
			String confDir = RhiginConstants.DIR_CONFIG;
			if (rhiginEnv != null && rhiginEnv.length() != 0) {
				confDir += rhiginEnv + "/";
				// 対象フォルダが存在しない、対象フォルダ以下のコンフィグ情報が０件の場合は
				// confフォルダ配下を読み込む.
				File confStat = new File(confDir);
				if (!confStat.isDirectory() || confStat.list() == null || confStat.list().length == 0) {
					confDir = RhiginConstants.DIR_CONFIG;
				}
				confStat = null;
			}

			// メインコンフィグファイルが存在するかチェック.
			config = new RhiginConfig(confDir);

			// ログファクトリの初期化.
			if (config.has("logger")) {
				LogFactory.setting(config.get("logger"));
			}
		} catch (Exception e) {
			// エラーが出る場合は、処理終了.
			e.printStackTrace();
			System.exit(1);
		}
		return config;
	}

	/**
	 * スタートアップ処理.
	 * 
	 * @param server
	 * @param config
	 * @return HttpInfo
	 * @exception Exception
	 */
	public static final HttpInfo startup(RhiginConfig config) throws Exception {

		// スレッドプーリングの初期化.
		// ThreadFunction.init(config);

		// ExecuteScriptにRhiginConfigの要素をセット.
		ExecuteScript.addOriginals("config", config);

		// ExecuteScriptにMimeTypeの要素をセット.
		MimeType mime = MimeType.createMime(config.get("mime"));
		ExecuteScript.addOriginals("mime", mime);

		// サーバIDを取得、設定.
		RhiginServerId.getInstance().getId();

		// 初期設定用のスクリプト実行.
		if (FileUtil.isFile(STARTUP_JS)) {
			// スクリプト実行用のコンテキスト.
			// スタートアップ専用のオブジェクトは、ここに設定する.
			RhiginContext context = new RhiginContext();

			// スタートアップで、ExecuteScript実行時に利用可能にしたいオブジェクトを設定.
			ArrayMap originals = new ArrayMap();
			context.setAttribute(STARTUP_OBJECT, originals);
			List<RhiginEndScriptCall> endScriptCallList = new ObjectList<RhiginEndScriptCall>();
			context.setAttribute(STARTUP_END_CALL_SCRIPT, endScriptCallList);
			context.setAttribute(addOrigin.getName(), addOrigin);
			context.setAttribute(addEndCall.getName(), addEndCall);
			
			Reader r = null;
			try {
				r = new BufferedReader(new InputStreamReader(new FileInputStream(STARTUP_JS), "UTF8"));
				ExecuteScript.execute(context, r, STARTUP_JS, ScriptConstants.HEADER, ScriptConstants.FOOTER, 0);
				r.close();
				r = null;
			} finally {
				if (r != null) {
					try {
						r.close();
					} catch (Exception e) {
					}
				}
			}
			
			// ExecuteScript実行時に利用可能にしたいオブジェクトをExecuteScript.addOriginalsで追加.
			{
				OList<Object[]> list = originals.getListMap().rawData();
				int len = list.size();
				Object[] n;
				for(int i = 0; i < len; i ++) {
					n = list.get(i);
					ExecuteScript.addOriginals((String)n[0], n[1]);
				}
			}
			// ExecuteScriptの終了時にスタートアップで登録した終了処理スクリプトを追加.
			{
				List<RhiginEndScriptCall> list = endScriptCallList;
				int len = list.size();
				for(int i = 0; i < len; i ++) {
					ExecuteScript.addEndScripts(list.get(i));
				}
			}
		}

		// HttpInfoを生成して返却.
		HttpInfo httpInfo = new HttpInfo();
		HttpInfo.load(httpInfo, config.get("http"));
		return httpInfo;
	}

	// rhiginスクリプトでのfunction, Objectの初期化処理.
	private static final void initRhiginScriptFunctionObject() {
		LockObjects.init();
	}

	// scopeからスタートアップオブジェクト追加先を取得.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Map<String, Object> getOriginal(Scriptable scope) {
		return (Map) scope.get(STARTUP_OBJECT, null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final List<RhiginEndScriptCall> getEndScriptCall(Scriptable scope) {
		return (List) scope.get(STARTUP_END_CALL_SCRIPT, null);
	}
	

	// スタートアップオブジェクト追加.
	private static final RhiginFunction addOrigin = new RhiginFunction() {
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 2) {
				try {
					getOriginal(scope).put("" + args[0], args[1]);
				} catch (Exception e) {
					throw new RhiginException(500, e);
				}
			}
			return Undefined.instance;
		}

		@Override
		public final String getName() {
			return "originals";
		}
	};
	
	// スクリプト実行後の終了処理を追加.
	private static final RhiginFunction addEndCall = new RhiginFunction() {
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				try {
					Object o = args[0];
					if(o instanceof NativeJavaObject) {
						o = ((NativeJavaObject)o).unwrap();
					}
					if(o instanceof RhiginEndScriptCall) {
						getEndScriptCall(scope).add((RhiginEndScriptCall)o);
					} else {
						getEndScriptCall(scope).add(new ExecuteJsByEndScriptCall("" + o));
					}
				} catch (Exception e) {
					throw new RhiginException(500, e);
				}
			}
			return Undefined.instance;
		}

		@Override
		public final String getName() {
			return "endCall";
		}
	};

}
