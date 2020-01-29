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
import rhigin.util.IsOs;
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

	/** システム終了時の処理を一時格納先. **/
	private static final String STARTUP_EXIT_SYSTEM_CALL_SCRIPT = "_$exitSystemCallScripts";
	
	// コンフィグ情報.
	private static RhiginConfig config = null;
	
	// OS名.
	private static final String osName;
	
	// OSビット.
	private static final int osBit;
	
	static {
		// OS情報を取得.
		Object[] v = createOs();
		osName = (String)v[0];
		osBit = (Integer)v[1];
	}
	
	// OSの情報を生成.
	private static final Object[] createOs() {
		IsOs io = IsOs.getInstance();
		int os = io.getOS();
		String name = "etc";
		if(os == IsOs.OS_WIN9X || os == IsOs.OS_WINNT) {
			name = "Windows";
		} else if(os == IsOs.OS_MACINTOSH || os == IsOs.OS_MAC_OS_X) {
			name = "Mac";
		} else if(os == IsOs.OS_UNIX) {
			name = "Linux";
		}
		return new Object[] {name, io.getBit()};
	}

	
	/**
	 * ログファクトリの初期化.
	 * 
	 * @param server
	 * @param console
	 * @param args
	 * @return RhiginConfig
	 */
	public static final RhiginConfig initLogFactory(boolean server, boolean console, String[] args) {
		return initLogFactory(server, console, false, args);
	}
	
	/**
	 * ログファクトリの初期化.
	 * 
	 * @param server
	 * @param console
	 * @return RhiginConfig
	 */
	public static final RhiginConfig initLogFactory(boolean server, boolean console) {
		return initLogFactory(server, console, false);
	}

	
	/**
	 * ログファクトリの初期化.
	 * 
	 * @param server
	 * @param console
	 * @param noScript
	 * @param args
	 * @return RhiginConfig
	 */
	public static final RhiginConfig initLogFactory(boolean server, boolean console, boolean noScript, String[] args) {
		// Args管理オブジェクトにセット.
		Args.set(args);
		return initLogFactory(server, console, noScript);
	}
	
	/**
	 * ログファクトリの初期化.
	 * 
	 * @param server
	 * @param console
	 * @param noScript
	 * @return RhiginConfig
	 */
	public static final RhiginConfig initLogFactory(boolean server, boolean console, boolean noScript) {
		RhiginConfig config = null;
		try {
			// モードをセット.
			Http.setMode(server, console);

			// function, objectの初期化.
			if(!noScript) {
				initRhiginScriptFunctionObject();
			}
			
			// rhiginEnvを取得.
			String rhiginEnv = getRhiginEnv();

			// コンフィグ読み込みディレクトリ先を、rhigin起動環境に合わせる.
			String confDir = RhiginConstants.DIR_CONFIG;
			if (rhiginEnv != null) {
				confDir += rhiginEnv + "/";
				// 対象フォルダが存在しない、対象フォルダ以下のコンフィグ情報が０件の場合は
				// confフォルダ配下を読み込む.
				File confStat = new File(confDir);
				if (!confStat.isDirectory() || confStat.list() == null || confStat.list().length == 0) {
					confDir = RhiginConstants.DIR_CONFIG;
				}
				confStat = null;
			}
			
			// サーバモードの場合は、コンフィグフォルダは必須.
			if(!FileUtil.isDir(confDir) && server) {
				throw new RhiginException("Config folder does not exist: " + confDir);
			}
			
			// コンフィグ情報をロード.
			config = new RhiginConfig(confDir);

			// ログファクトリの初期化.
			if (config.has("logger")) {
				LogFactory.setting(config.get("logger"));
			}
			// コンフィグ情報をセット.
			setConfig(config);
		} catch (Exception e) {
			// エラーが出る場合は、処理終了.
			e.printStackTrace();
			System.exit(1);
		}
		return config;
	}
	
	/**
	 * RhiginConfigを設定.
	 * 
	 * @param conf
	 */
	public static final void setConfig(RhiginConfig conf) {
		config = conf;
	}
	
	/**
	 * ロード済みのRhiginConfigを取得.
	 * 
	 * @return RhiginConfig
	 */
	public static final RhiginConfig getConfig() {
		return config;
	}
	
	/**
	 * OS名を取得.
	 * 
	 * @return Windows, Mac, Linux, etc.
	 */
	public static final String getOsName() {
		return osName;
	}
	
	/**
	 * Osビットを取得.
	 * 
	 * @return int 32 or 64.
	 */
	public static final int getOsBit() {
		return osBit;
	}

	/**
	 * RhiginEnvを取得.
	 * 
	 * @return String RhiginEnvが返却されます.
	 */
	public static final String getRhiginEnv() {
		// 環境変数から、rhigin起動環境を取得.
		String ret = EnvCache.get(RhiginConstants.ENV_ENV);
		
		// プログラム引数からrhigin起動環境が設定されている場合.
		// こちらの情報を優先的に利用する.
		Args params = Args.getInstance();
		if(params.isValue("-e", "--env")) {
			String n = params.get("-e", "--env");
			if(n != null && !n.isEmpty()) {
				ret = n;
			}
		}
		return ret == null || ret.isEmpty() ? null : ret;
	}

	/**
	 * スタートアップ処理.
	 * 
	 * @param config
	 * @return HttpInfo
	 * @exception Exception
	 */
	public static final HttpInfo startup(RhiginConfig config) throws Exception {
		boolean server = Http.isWebServerMode();
		boolean console = Http.isConsoleMode();
		
		// スレッドプーリングの初期化.
		// ThreadFunction.init(config);

		// コンフィグ情報をセット.
		if(getConfig() != config) {
			setConfig(config);
		}
		
		// サーバモード及び、コンソールモードの場合.
		if(server || console) {
			// ExecuteScriptにMimeTypeの要素をセット.
			MimeType mime = MimeType.createMime(config.get("mime"));
			ExecuteScript.addOriginals("mime", mime);

			// サーバIDを取得、設定.
			RhiginServerId.getInstance().getId();
		}

		// 初期設定用のスクリプト実行.
		if (FileUtil.isFile(STARTUP_JS)) {
			final List<RhiginEndScriptCall> endScriptCallList = new ObjectList<RhiginEndScriptCall>();
			final List<RhiginEndScriptCall> exitScriptCallList = new ObjectList<RhiginEndScriptCall>();
			
			// スクリプト実行用のコンテキスト.
			// スタートアップ専用のオブジェクトは、ここに設定する.
			final RhiginContext context = new RhiginContext();

			// スタートアップで、ExecuteScript実行時に利用可能にしたいオブジェクトを設定.
			final ArrayMap<String, Object> originals = new ArrayMap<String, Object>();
			context.setAttribute(STARTUP_OBJECT, originals);
			context.setAttribute(addOrigin.getName(), addOrigin);
			
			// スクリプト終了時のスクリプト実行.
			context.setAttribute(STARTUP_END_CALL_SCRIPT, endScriptCallList);
			context.setAttribute(addEndCall.getName(), addEndCall);
			
			// システム終了時のスクリプト実行.
			context.setAttribute(STARTUP_EXIT_SYSTEM_CALL_SCRIPT, exitScriptCallList);
			context.setAttribute(addExitSystemCall.getName(), addExitSystemCall);
			
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
				int len = originals.size();
				for(int i = 0; i < len; i ++) {
					ExecuteScript.addOriginals(originals.getKey(i), originals.getValue(i));
				}
			}
			// ExecuteScriptの終了時にスタートアップで登録した終了処理系のスクリプトを追加.
			{
				List<RhiginEndScriptCall> list = endScriptCallList;
				int len = list.size();
				for(int i = 0; i < len; i ++) {
					ExecuteScript.addEndScripts(list.get(i));
				}
				
				list = exitScriptCallList;
				len = list.size();
				for(int i = 0; i < len; i ++) {
					ExecuteScript.addExitSystemScripts(list.get(i));
				}
			}
		}

		// HttpInfoを生成して返却.
		HttpInfo httpInfo = new HttpInfo();
		if(server || console) {
			HttpInfo.load(httpInfo, config.get("http"));
		}
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final List<RhiginEndScriptCall> getExitSystemScriptCall(Scriptable scope) {
		return (List) scope.get(STARTUP_EXIT_SYSTEM_CALL_SCRIPT, null);
	}
	

	// スタートアップオブジェクト追加.
	private static final RhiginFunction addOrigin = new RhiginFunction() {
		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
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
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
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
	
	// システム終了時の処理を追加.
	private static final RhiginFunction addExitSystemCall = new RhiginFunction() {
		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				try {
					Object o = args[0];
					if(o instanceof NativeJavaObject) {
						o = ((NativeJavaObject)o).unwrap();
					}
					if(o instanceof RhiginEndScriptCall) {
						getExitSystemScriptCall(scope).add((RhiginEndScriptCall)o);
					} else {
						getExitSystemScriptCall(scope).add(new ExecuteJsByEndScriptCall("" + o));
					}
				} catch (Exception e) {
					throw new RhiginException(500, e);
				}
			}
			return Undefined.instance;
		}

		@Override
		public final String getName() {
			return "exitSystemCall";
		}
	};


}
