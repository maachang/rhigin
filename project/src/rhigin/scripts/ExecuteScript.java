package rhigin.scripts;

import java.io.Reader;
import java.io.StringReader;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import rhigin.RhiginConfig;
import rhigin.RhiginConstants;
import rhigin.RhiginException;
import rhigin.RhiginStartup;
import rhigin.logs.Log;
import rhigin.logs.LogFactory;
import rhigin.scripts.function.ArgsFunctions;
import rhigin.scripts.function.AuthAccessKeyFunction;
import rhigin.scripts.function.Base64Functions;
import rhigin.scripts.function.BinaryFunction;
import rhigin.scripts.function.CheckFunctions;
import rhigin.scripts.function.ColorOutFunction;
import rhigin.scripts.function.EntityFunctions;
import rhigin.scripts.function.EvalFunction;
import rhigin.scripts.function.GcFunction;
import rhigin.scripts.function.GetClassFunction;
import rhigin.scripts.function.GetEnvFunction;
import rhigin.scripts.function.GlobalFunction;
import rhigin.scripts.function.HttpClientFunction;
import rhigin.scripts.function.IpPermissionFunction;
import rhigin.scripts.function.LogFactoryFunction;
import rhigin.scripts.function.NanoTimeFunction;
import rhigin.scripts.function.ParseInt32Function;
import rhigin.scripts.function.ParseIntFunction;
import rhigin.scripts.function.RandomFunction;
import rhigin.scripts.function.RequireFunction;
import rhigin.scripts.function.RhiginEnvFunction;
import rhigin.scripts.function.SendJsFunction;
import rhigin.scripts.function.ServerIdFunction;
import rhigin.scripts.function.SleepFunction;
import rhigin.scripts.function.SystemTimeFunction;
import rhigin.scripts.function.ValidateFunction;
import rhigin.scripts.objects.AccessKeyObject;
import rhigin.scripts.objects.ColorOutObject;
import rhigin.scripts.objects.ConsoleObject;
import rhigin.scripts.objects.ExecCmdObject;
import rhigin.scripts.objects.FCipherObject;
import rhigin.scripts.objects.FCompObject;
import rhigin.scripts.objects.FileObject;
import rhigin.scripts.objects.FunctionObject;
import rhigin.scripts.objects.IpPermissionObject;
import rhigin.scripts.objects.JArrayObject;
import rhigin.scripts.objects.JDateObject;
import rhigin.scripts.objects.JMapObject;
import rhigin.scripts.objects.JSONObject;
import rhigin.scripts.objects.JavaObject;
import rhigin.scripts.objects.JwtObject;
import rhigin.scripts.objects.LockObjects;
import rhigin.scripts.objects.UniqueIdObject;
import rhigin.scripts.objects.Xor128Object;
import rhigin.util.FixedKeyValues;
import rhigin.util.ListMap;
import rhigin.util.OList;

/**
 * javscriptを実行.
 */
public class ExecuteScript {
	
	/** Rhino js packages. **/
	public static final String RHINO_JS_PACKAGE_NAME = "org.mozilla.javascript";

	/** 名無しスクリプト名. **/
	protected static final String NO_SCRIPT_NAME = "<script>";

	/** Javascriot最適化レベル: コンパイル時は最適化. **/
	private static final int SCRIPT_COMPILE_OPTIMIZE_LEVEL = 1;

	/** Javascriot最適化レベル: 非コンパイル時は最適化無効. **/
	private static final int SCRIPT_NOT_COMPILE_OPTIMIZE_LEVEL = -1;

	/** java LanguageVersion. **/
	// private static final int SCRIPT_LANGUAGE_VERSION = Context.VERSION_1_5;
	private static final int SCRIPT_LANGUAGE_VERSION = Context.VERSION_1_8;
	// private static final int SCRIPT_LANGUAGE_VERSION = Context.VERSION_ES6;
	
	// 基本Functions.
	private static FixedKeyValues<String, Object> baseFunctions = null;

	/** originalFunctionAndObject. **/
	private static final ListMap<String, Object> originalFunctionAndObjectList = new ListMap<String, Object>();
	
	/** スクリプト終了処理管理. **/
	private static final OList<RhiginEndScriptCall> endScriptCallList = new OList<RhiginEndScriptCall>();
	
	/** システム終了処理管理. **/
	private static final OList<RhiginEndScriptCall> exitSystemScriptCallList = new OList<RhiginEndScriptCall>();

	static {
		// Context初期化.
		ContextFactory.initGlobal(new ContextFactory() {
			@Override
			protected Context makeContext() {
				final Context ctx = super.makeContext();
				ctx.setLanguageVersion(SCRIPT_LANGUAGE_VERSION);
				ctx.setOptimizationLevel(SCRIPT_COMPILE_OPTIMIZE_LEVEL);
				ctx.setClassShutter(RhiginClassShutter.getInstance());
				ctx.setWrapFactory(RhiginWrapFactory.getInstance());
				return ctx;
			}

			@Override
			protected Object doTopCall(final Callable callable, final Context cx, final Scriptable scope,
					final Scriptable thisObj, final Object[] args) {
				AccessControlContext accCtxt = null;
				final Scriptable global = ScriptableObject.getTopLevelScope(scope);
				final Scriptable globalProto = global.getPrototype();
				if (globalProto instanceof RhiginTopLevel) {
					accCtxt = ((RhiginTopLevel) globalProto).getAccessContext();
				}
				if (accCtxt != null) {
					return AccessController.doPrivileged(new PrivilegedAction<Object>() {
						public Object run() {
							return superDoTopCall(callable, cx, scope, thisObj, args);
						}
					}, accCtxt);
				} else {
					return superDoTopCall(callable, cx, scope, thisObj, args);
				}
			}

			private Object superDoTopCall(Callable callable, Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				return super.doTopCall(callable, cx, scope, thisObj, args);
			}
		});
		
		// baseFunctionsを初期化.
		initBaseFunctions();
	}
	
	/**
	 * baseFunctionsを初期化.
	 */
	private static final FixedKeyValues<String, Object> initBaseFunctions() {
		// baseFunctionsを登録.
		FixedKeyValues<String, Object> scope = new FixedKeyValues<String, Object>();
		scope.startIndex();
		
		// rhiginバージョンをセット.
		scope.put("VERSION", RhiginConstants.VERSION);
		
		// RHIGIN_HOMEをセット.
		scope.put("RHIGIN_HOME", RhiginConstants.RHIGIN_HOME);
		
		// OS情報をセット.
		scope.put("OS_NAME", RhiginStartup.getOsName());
		scope.put("OS_BIT", RhiginStartup.getOsBit());
		
		// コンフィグ情報をセット.
		scope.put("config", RhiginConfig.getMainConfig());

		// オブジェクトの登録.
		ConsoleObject.regFunctions(scope);
		FunctionObject.regFunctions(scope);
		Xor128Object.regFunctions(scope);
		JSONObject.regFunctions(scope);
		JwtObject.regFunctions(scope);
		FileObject.regFunctions(scope);
		JDateObject.regFunctions(scope);
		LockObjects.regFunctions(scope);
		UniqueIdObject.regFunctions(scope);
		FCipherObject.regFunctions(scope);
		FCompObject.regFunctions(scope);
		ColorOutObject.regFunctions(scope);
		ExecCmdObject.regFunctions(scope);
		JavaObject.regFunctions(scope);
		JArrayObject.regFunctions(scope);
		JMapObject.regFunctions(scope);
		IpPermissionFunction.regFunctions(scope);
		AuthAccessKeyFunction.regFunctions(scope);
		SendJsFunction.regFunctions(scope);

		// rhigin用の基本オブジェクトを設定.
		RequireFunction.regFunctions(scope);
		ParseIntFunction.regFunctions(scope);
		EvalFunction.regFunctions(scope);
		GcFunction.regFunctions(scope);
		GlobalFunction.regFunctions(scope);
		LogFactoryFunction.regFunctions(scope);
		BinaryFunction.regFunctions(scope);
		GetClassFunction.regFunctions(scope);
		SleepFunction.regFunctions(scope);
		GetEnvFunction.regFunctions(scope);
		ArgsFunctions.regFunctions(scope);
		RandomFunction.regFunctions(scope);
		Base64Functions.regFunctions(scope);
		CheckFunctions.regFunctions(scope);
		HttpClientFunction.regFunctions(scope);
		NanoTimeFunction.regFunctions(scope);
		SystemTimeFunction.regFunctions(scope);
		ServerIdFunction.regFunctions(scope);
		ValidateFunction.regFunctions(scope);
		EntityFunctions.regFunctions(scope);
		RhiginEnvFunction.regFunctions(scope);
		ColorOutFunction.regFunctions(scope);
		ParseInt32Function.regFunctions(scope);
		AccessKeyObject.regFunctions(scope);
		IpPermissionObject.regFunctions(scope);
		
		// インデックスの作成.
		scope.endIndex();
		baseFunctions = scope;
		return scope;
	}
	
	/**
	 * RhiginContextにBaseFunctionを登録.
	 * @param context
	 */
	public static final void setBaseFunctions(RhiginContext context) {
		context.setBaseFunctions(baseFunctions);
	}

	// [ThreadLocal]: topLevelオブジェクトを生成・取得.
	private static final ThreadLocal<RhiginTopLevel> topLevels = new ThreadLocal<RhiginTopLevel>();

	private static final RhiginTopLevel getTopLevel() {
		RhiginTopLevel ret = topLevels.get();
		if (ret == null) {
			try {
				ret = new RhiginTopLevel(ContextFactory.getGlobal().enterContext());
			} finally {
				Context.exit();
			}
			topLevels.set(ret);
		}
		return ret;
	}

	// [ThreadLocal]: RhiginContextオブジェクトのカレントスレッド管理.
	private static final ThreadLocal<RhiginContext> currentRhiginContext = new ThreadLocal<RhiginContext>();

	/**
	 * カレントのRhiginContextを取得.
	 * 
	 * @return RhiginContext カレントのRhiginContextが返却されます.
	 */
	public static final RhiginContext currentRhiginContext() {
		return currentRhiginContext.get();
	}
	
	/**
	 * カレントのRhiginContextを削除.
	 */
	public static final void clearCurrentRhiginContext() {
		currentRhiginContext.set(null);
	}

	/**
	 * 指定javascriptをコンパイル.
	 * 
	 * @param script
	 *            対象のスクリプトを設定します.
	 * @return Script コンパイル結果が返却されます.
	 * @throws Exception
	 *             例外.
	 */
	public static final Script compile(String script) throws Exception {
		return compile(script, null);
	}

	/**
	 * 指定javascriptをコンパイル.
	 * 
	 * @param script
	 *            対象のスクリプトを設定します.
	 * @param name
	 *            スクリプトファイル名を設定します.
	 * @return Script コンパイル結果が返却されます.
	 * @throws Exception
	 *             例外.
	 */
	public static final Script compile(String script, String name) throws Exception {
		return compile(new StringReader(script), name);
	}

	/**
	 * 指定javascriptをコンパイル.
	 * 
	 * @param r
	 *            readerを設定します.
	 * @param name
	 *            スクリプトファイル名を設定します.
	 * @return Script コンパイル結果が返却されます.
	 * @throws Exception
	 *             例外.
	 */
	public static final Script compile(Reader r, String name) throws Exception {
		return compile(r, name, "", "", 1);
	}

	// スクリプト情報を生成.
	private static final Reader getScript(Reader r, String headerScript, String footerScript) throws Exception {
		//if (headerScript.isEmpty() && footerScript.isEmpty()) {
		//	return r;
		//}
		int len;
		char[] c = new char[1024];
		StringBuilder buf = new StringBuilder(headerScript);
		while ((len = r.read(c)) != -1) {
			buf.append(c, 0, len);
		}
		c = null;
		buf.append(footerScript);

		return new StringReader(JsChangesCode.changeCode(buf.toString()));
	}

	/**
	 * 指定javascriptをコンパイル.
	 * 
	 * @param r
	 *            readerを設定します.
	 * @param name
	 *            スクリプトファイル名を設定します.
	 * @param headerScript
	 *            ヘッダに追加するスクリプトを設定します.
	 * @param footerScript
	 *            フッタに追加するスクリプトを設定します.
	 * @param lineNo
	 *            ライン開始番号を設定します.
	 * @return Script コンパイル結果が返却されます.
	 * @throws Exception
	 *             例外.
	 */
	public static final Script compile(Reader r, String name, String headerScript, String footerScript, int lineNo)
			throws Exception {
		name = (name == null || name.isEmpty()) ? NO_SCRIPT_NAME : name;
		Context ctx = ContextFactory.getGlobal().enterContext();
		try {
			// コンパイル.
			return ctx.compileReader(getScript(r, headerScript, footerScript), name, lineNo, null);
		} finally {
			Context.exit();
			EntityFunctions.exit();
		}
	}

	/**
	 * コンパイル結果を実行.
	 * 
	 * @param context
	 *            RhiginContextオブジェクトを設定します.
	 * @param compiled
	 *            コンパイル済みオブジェクトを設定します.
	 * @return Object スクリプト実行結果を返却します.
	 * @throws Exception
	 *             例外.
	 */
	public static final Object execute(RhiginContext context, Script compiled) throws Exception {
		RhiginContext old = ExecuteScript.currentRhiginContext();
		currentRhiginContext.set(context);
		try {
			return eval(compiled);
		} finally {
			currentRhiginContext.set(old);
		}
	}
	
	/**
	 * コンパイル結果を実行.
	 * JS実行中に重ねて呼び出す場合は、こちらでJSを実行します.
	 * 
	 * @param compiled
	 *            コンパイル済みオブジェクトを設定します.
	 * @return Object スクリプト実行結果を返却します.
	 * @throws Exception
	 *             例外.
	 */
	public static final Object eval(Script compiled) throws Exception {
		boolean resetRhiginContextFlag = false;
		Context ctx = ContextFactory.getGlobal().enterContext();
		RhiginContext context = ExecuteScript.currentRhiginContext();
		if(context == null) {
			context = new RhiginContext();
			currentRhiginContext.set(context);
			resetRhiginContextFlag = true;
		}
		try {
			// 実行処理.
			RhiginScriptable scope = new RhiginScriptable(context);
			scope.setPrototype(getTopLevel());
			settingRhiginObject(ctx, scope);
			final Object ret = compiled.exec(ctx, scope);
			return RhiginWrapUtil.unwrap(ret);
//		} catch(RhiginWrapException wre) {
//			// rhino用のラップ例外の場合は、RhiginExceptionに変換して返却.
//			Throwable t = wre.getWrappedException();
//			if(t instanceof RhiginException) {
//				((RhiginException)t).setMessage(wre.getMessage());
//				throw (RhiginException)t;
//			}
//			throw new RhiginException(wre.getStatus(), t);
//			} catch(RhiginException re) {
//			throw re;
//		} catch(Throwable t) {
//			throw new RhiginException(t);
		} catch(RhiginWrapException wre) {
			throw wre;
		} catch(RhiginException re) {
			throw new RhiginWrapException(re);
		} catch(Throwable t) {
			throw new RhiginWrapException(t);
		} finally {
			if(resetRhiginContextFlag) {
				currentRhiginContext.set(null);
			}
			Context.exit();
			EntityFunctions.exit();
		}
	}

	/**
	 * 指定javascriptを実行.
	 * 
	 * @param context
	 *            RhiginContextオブジェクトを設定します.
	 * @param script
	 *            スクリプトを設定します.
	 * @return Object スクリプト実行結果を返却します.
	 * @throws Exception
	 *             例外.
	 */
	public static final Object execute(RhiginContext context, String script) throws Exception {
		return execute(context, script, null);
	}

	/**
	 * 指定javascriptを実行.
	 * 
	 * @param context
	 *            RhiginContextオブジェクトを設定します.
	 * @param script
	 *            スクリプトを設定します.
	 * @param name
	 *            スクリプトファイル名を設定します.
	 * @return Object スクリプト実行結果を返却します.
	 * @throws Exception
	 *             例外.
	 */
	public static final Object execute(RhiginContext context, String script, String name) throws Exception {
		return execute(context, new StringReader(script), name);
	}

	/**
	 * 指定javascriptを実行.
	 * 
	 * @param context
	 *            RhiginContextオブジェクトを設定します.
	 * @param r
	 *            readerを設定します.
	 * @param name
	 *            スクリプトファイル名を設定します.
	 * @return Object スクリプト実行結果を返却します.
	 * @throws Exception
	 *             例外.
	 */
	public static final Object execute(RhiginContext context, Reader r, String name) throws Exception {
		return execute(context, r, name, "", "", 1);
	}

	/**
	 * 指定javascriptを実行.
	 * 
	 * @param context
	 *            RhiginContextオブジェクトを設定します.
	 * @param r
	 *            readerを設定します.
	 * @param name
	 *            スクリプトファイル名を設定します.
	 * @param headerScript
	 *            ヘッダに追加するスクリプトを設定します.
	 * @param footerScript
	 *            フッタに追加するスクリプトを設定します.
	 * @param lineNo
	 *            ライン開始番号を設定します.
	 * @return Object スクリプト実行結果を返却します.
	 * @throws Exception
	 *             例外.
	 */
	public static final Object execute(RhiginContext context, Reader r, String name, String headerScript,
			String footerScript, int lineNo) throws Exception {
		RhiginContext old = ExecuteScript.currentRhiginContext();
		currentRhiginContext.set(context);
		try {
			return eval(r, name, headerScript, footerScript, lineNo);
		} finally {
			currentRhiginContext.set(old);
		}
	}
	
	/**
	 * 指定javascriptを実行.
	 * JS実行中に重ねて呼び出す場合は、こちらでJSを実行します.
	 * 
	 * @param r
	 *            readerを設定します.
	 * @param name
	 *            スクリプトファイル名を設定します.
	 * @param headerScript
	 *            ヘッダに追加するスクリプトを設定します.
	 * @param footerScript
	 *            フッタに追加するスクリプトを設定します.
	 * @param lineNo
	 *            ライン開始番号を設定します.
	 * @return Object スクリプト実行結果を返却します.
	 * @throws Exception
	 *             例外.
	 */
	public static final Object eval(Reader r, String name, String headerScript,
		String footerScript, int lineNo) throws Exception {
		boolean resetRhiginContextFlag = false;
		name = (name == null || name.isEmpty()) ? NO_SCRIPT_NAME : name;
		Context ctx = ContextFactory.getGlobal().enterContext();
		ctx.setOptimizationLevel(SCRIPT_NOT_COMPILE_OPTIMIZE_LEVEL);
		RhiginContext context = ExecuteScript.currentRhiginContext();
		if(context == null) {
			context = new RhiginContext();
			currentRhiginContext.set(context);
			resetRhiginContextFlag = true;
		}
		try {
			// 対象ソースをコンパイル.
			RhiginScriptable scope = new RhiginScriptable(context);
			scope.setPrototype(getTopLevel());
			settingRhiginObject(ctx, scope);
			Script compiled = ctx.compileReader(getScript(r, headerScript, footerScript), name, lineNo, null);
			// 実行処理.
			final Object ret = compiled.exec(ctx, scope);
			return RhiginWrapUtil.unwrap(ret);
//		} catch(RhiginWrapException wre) {
//			// rhino用のラップ例外の場合は、RhiginExceptionに変換して返却.
//			Throwable t = wre.getWrappedException();
//			if(t instanceof RhiginException) {
//				((RhiginException)t).setMessage(wre.getMessage());
//				throw (RhiginException)t;
//			}
//			throw new RhiginException(wre.getStatus(), t);
//		} catch(RhiginException re) {
//			throw re;
//		} catch(Throwable t) {
//			throw new RhiginException(t);
		} catch(RhiginWrapException wre) {
			throw wre;
		} catch(RhiginException re) {
			throw new RhiginWrapException(re);
		} catch(Throwable t) {
			throw new RhiginWrapException(t);
		} finally {
			if(resetRhiginContextFlag) {
				currentRhiginContext.set(null);
			}
			Context.exit();
			EntityFunctions.exit();
		}
	}

	// 基本オブジェクトをセット.
	private static final void settingRhiginObject(Context ctx, RhiginScriptable scope) throws Exception {
		// contextにbaseFunctionsを追加.
		setBaseFunctions(scope.getContext());
		
		// オリジナルオブジェクトを設定.
		final ListMap<String, Object> list = originalFunctionAndObjectList;
		final int len = list.size();
		for (int i = 0; i < len; i++) {
			scope.put(list.keyAt(i), scope, list.valueAt(i));
		}
	}

	/**
	 * オリジナルなRhigin用のFunction及びオブジェクトを設定.
	 * 
	 * @param args
	 *            [name], [value], [name], [value] .... のように設定します.
	 */
	public static final void addOriginals(Object... args) {
		final int len = args == null ? 0 : args.length;
		final ListMap<String, Object> list = originalFunctionAndObjectList;
		for(int i = 0; i < len; i += 2) {
			list.put((String)args[i], args[i+1]);
		}
	}

	/**
	 * オリジナルなRhigin用のFunction及びオブジェクトを格納するオブジェクトを取得.
	 * 
	 * @return ListMap
	 */
	public static final ListMap<String, Object> getOriginal() {
		return originalFunctionAndObjectList;
	}
	
	/**
	 * スクリプト終了時に実行する処理を追加.
	 * @param endScript
	 */
	public static final void addEndScripts(RhiginEndScriptCall... endScripts) {
		final int len = endScripts.length;
		for(int i = 0; i < len; i ++) {
			endScriptCallList.add(endScripts[i]);
		}
	}
	
	/**
	 * スクリプト終了時に実行する処理群を取得.
	 * @return
	 */
	public static final OList<RhiginEndScriptCall> getEndScriptList() {
		return endScriptCallList;
	}
	
	/**
	 * システム終了時に実行する処理を追加.
	 * @param endScript
	 */
	public static final void addExitSystemScripts(RhiginEndScriptCall... exitSystemScripts) {
		final int len = exitSystemScripts.length;
		for(int i = 0; i < len; i ++) {
			exitSystemScriptCallList.add(exitSystemScripts[i]);
		}
	}
	
	/**
	 * システム終了時に実行する処理群を取得.
	 * @return
	 */
	public static final OList<RhiginEndScriptCall> getExitSystemScriptList() {
		return exitSystemScriptCallList;
	}
	
	/**
	 * スクリプト終了時、システム終了時に実行する処理群を実行.
	 * @param exitSystemFlag
	 * @param cache
	 */
	public static final void callEndScripts(boolean exitSystemFlag) {
		final OList<RhiginEndScriptCall> list = exitSystemFlag ? exitSystemScriptCallList : endScriptCallList;
		final int len = list.size();
		if(len > 0) {
			RhiginEndScriptCall n;
			final Log log = LogFactory.create();
			for(int i = 0; i < len; i ++) {
				n = list.get(i);
				if(n != null) {
					try {
						n.call();
					} catch(Exception e) {
						// ログ出力.
						log.error(e);
					}
				}
			}
		}
	}
	
	/**
	 * ヘッダの改行数を取得.
	 * @param s
	 * @return
	 */
	public static final int getEnterCount(String s) {
		int ret = 0;
		int len = s.length();
		for (int i = 0; i < len; i++) {
			if (s.charAt(i) == '\n') {
				ret++;
			}
		}
		return ret;
	}
}