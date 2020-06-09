package rhigin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import rhigin.http.Http;
import rhigin.http.HttpConstants;
import rhigin.http.HttpInfo;
import rhigin.http.MimeType;
import rhigin.logs.LogFactory;
import rhigin.net.IpPermission;
import rhigin.scripts.ExecuteJsByEndScriptCall;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.JavaScriptable;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObjectWrapper;
import rhigin.scripts.ScriptConstants;
import rhigin.scripts.objects.LockObjects;
import rhigin.util.Args;
import rhigin.util.ArrayMap;
import rhigin.util.EnvCache;
import rhigin.util.FileUtil;
import rhigin.util.IsOs;
import rhigin.util.ObjectList;
import rhigin.util.WatchPath;

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
	 * Rhiginの初期化.
	 * 
	 * @param server
	 * @param console
	 * @param args
	 * @return RhiginConfig
	 */
	public static final RhiginConfig init(boolean server, boolean console, String[] args) {
		return init(server, console, false, args);
	}
	
	/**
	 * Rhiginの初期化.
	 * 
	 * @param server
	 * @param console
	 * @return RhiginConfig
	 */
	public static final RhiginConfig init(boolean server, boolean console) {
		return init(server, console, false);
	}

	
	/**
	 * Rhiginの初期化.
	 * 
	 * @param server
	 * @param console
	 * @param noScript
	 * @param args
	 * @return RhiginConfig
	 */
	public static final RhiginConfig init(boolean server, boolean console, boolean noScript, String[] args) {
		// Args管理オブジェクトにセット.
		Args.set(args);
		return init(server, console, noScript);
	}
	
	/**
	 * ログファクトリの初期化.
	 * 
	 * @param server
	 * @param console
	 * @param noScript
	 * @return RhiginConfig
	 */
	public static final RhiginConfig init(boolean server, boolean console, boolean noScript) {
		return init(null, server, console, noScript);
	}
	
	/**
	 * ログファクトリの初期化.
	 * 
	 * @parma confDir
	 * @param server
	 * @param console
	 * @param noScript
	 * @return RhiginConfig
	 */
	public static final RhiginConfig init(String confDir, boolean server, boolean console, boolean noScript) {
		RhiginConfig config = null;
		try {
			// モードをセット.
			Http.setMode(server, console);

			// function, objectの初期化.
			if(!noScript) {
				initRhiginScriptFunctionObject();
			}
			// 基本コンフィグフォルダをチェック.
			// 存在しないか、指定フォルダが存在しない場合は、デフォルトのコンフィグフォルダ.
			if(confDir == null || !FileUtil.isDir(confDir)) {
				confDir = RhiginConstants.DIR_CONFIG;
			} else {
				confDir = FileUtil.getFullPath(confDir);
			}
			
			// サーバモードの場合は、コンフィグフォルダは必須.
			if(!FileUtil.isDir(confDir) && server) {
				throw new RhiginException("Config folder does not exist: " + confDir);
			}
			
			// コンフィグ情報をロード.
			config = new RhiginConfig(getRhiginEnv(), confDir);
			
			// ログファクトリの初期化.
			if (config.has("logger")) {
				LogFactory.setting(config.get("logger"));
			}
			// コンフィグ情報をセット.
			RhiginConfig.setMainConfig(config);
			
			// ipPermissionをロード.
			IpPermission ip = new IpPermission();
			ip.load();
			IpPermission.setMainIpPermission(ip);
			
			// Rhiginで利用するパスの管理.
			WatchPath wp = null;
			if(!FileUtil.isDir(RhiginConstants.DIR_LIB)) {
				wp = new WatchPath(HttpConstants.ACCESS_PATH);
			} else {
				wp = new WatchPath(HttpConstants.ACCESS_PATH, RhiginConstants.DIR_LIB);
			}
			WatchPath.setStaticWatchPath(wp);
			
		} catch (Exception e) {
			// エラーが出る場合は、処理終了.
			e.printStackTrace();
			System.exit(1);
			return null;
		}
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final HttpInfo startup(RhiginConfig config) throws Exception {
		boolean server = Http.isWebServerMode();
		boolean console = Http.isConsoleMode();
		
		// スレッドプーリングの初期化.
		// ThreadFunction.init(config);

		// コンフィグ情報をセット.
		if(RhiginConfig.getMainConfig() != config) {
			RhiginConfig.setMainConfig(config);

			// ipPermissionをロード.
			IpPermission ip = new IpPermission();
			ip.load();
			IpPermission.setMainIpPermission(ip);
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
			final List<RhiginEndScriptCall> endScriptCallList = new JavaScriptable.GetList(new ObjectList<RhiginEndScriptCall>());
			final List<RhiginEndScriptCall> exitScriptCallList = new JavaScriptable.GetList(new ObjectList<RhiginEndScriptCall>());
			
			// スクリプト実行用のコンテキスト.
			// スタートアップ専用のオブジェクトは、ここに設定する.
			final RhiginContext context = new RhiginContext();

			// スタートアップで、ExecuteScript実行時に利用可能にしたいオブジェクトを設定.
			final Map<String, Object> originals = new JavaScriptable.GetMap(new ArrayMap<String, Object>());
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
				ExecuteScript.clearCurrentRhiginContext();
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
				ArrayMap<String, Object> ori = (ArrayMap)((JavaScriptable.GetMap)originals).rawData();
				for(int i = 0; i < len; i ++) {
					ExecuteScript.addOriginals(ori.getKey(i), ori.getValue(i));
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
					if(o instanceof Wrapper) {
						o = ((Wrapper)o).unwrap();
					} else if (o instanceof RhiginObjectWrapper) {
						o = ((RhiginObjectWrapper) o).unwrap();
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
					if(o instanceof Wrapper) {
						o = ((Wrapper)o).unwrap();
					} else if (o instanceof RhiginObjectWrapper) {
						o = ((RhiginObjectWrapper) o).unwrap();
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
