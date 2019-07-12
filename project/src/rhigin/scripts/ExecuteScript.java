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

import rhigin.scripts.function.BinaryFunction;
import rhigin.scripts.function.GetClassFunction;
import rhigin.scripts.function.GetEnvFunction;
import rhigin.scripts.function.RequireFunction;
import rhigin.scripts.function.SleepFunction;
import rhigin.scripts.objects.ConsoleObject;
import rhigin.scripts.objects.JSONObject;
import rhigin.util.ListMap;
import rhigin.util.OList;

/**
 * javscriptを実行.
 */
public class ExecuteScript {
	
	/** 名無しスクリプト名. **/
	protected static final String NO_SCRIPT_NAME = "<script>";
	
	/** Javascriot最適化レベル: コンパイル時は最適化. **/
	private static final int SCRIPT_COMPILE_OPTIMIZE_LEVEL = 1;
	
	/** Javascriot最適化レベル: 非コンパイル時は最適化無効. **/
	private static final int SCRIPT_NOT_COMPILE_OPTIMIZE_LEVEL = -1;
	
	/** java LanguageVersion. **/
	//private static final int SCRIPT_LANGUAGE_VERSION = Context.VERSION_1_5;
	private static final int SCRIPT_LANGUAGE_VERSION = Context.VERSION_1_8;
	//private static final int SCRIPT_LANGUAGE_VERSION = Context.VERSION_ES6;
	
	
	/** originalFunctionAndObject. **/
	private static final ListMap originalFunctionAndObjectList = new ListMap();
	
    static {
    	    // Context初期化.
        ContextFactory.initGlobal(new ContextFactory() {
            @Override
            protected Context makeContext() {
                Context ctx = super.makeContext();
                ctx.setLanguageVersion(SCRIPT_LANGUAGE_VERSION);
                ctx.setOptimizationLevel(SCRIPT_COMPILE_OPTIMIZE_LEVEL);
                ctx.setClassShutter(RhiginClassShutter.getInstance());
                ctx.setWrapFactory(RhiginWrapFactory.getInstance());
                return ctx;
            }
            @Override
            protected Object doTopCall(final Callable callable,
                                       final Context cx, final Scriptable scope,
                                       final Scriptable thisObj, final Object[] args) {
                AccessControlContext accCtxt = null;
                Scriptable global = ScriptableObject.getTopLevelScope(scope);
                Scriptable globalProto = global.getPrototype();
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
            private Object superDoTopCall(Callable callable,
                                          Context cx, Scriptable scope,
                                          Scriptable thisObj, Object[] args) {
                return super.doTopCall(callable, cx, scope, thisObj, args);
            }
        });
    }
    
    // [ThreadLocal]: topLevelオブジェクトを生成・取得.
    private static final ThreadLocal<RhiginTopLevel> topLevels = new ThreadLocal<RhiginTopLevel>();
    private static final RhiginTopLevel getTopLevel() {
    		RhiginTopLevel ret = topLevels.get();
	    	if(ret == null) {
            try {
            		ret = new RhiginTopLevel(ContextFactory.getGlobal().enterContext());
            } finally {
            		Context.exit();
            }
            topLevels.set(ret);
	    	}
	    	return ret;
    	}
	
	/**
	 * 指定javascriptをコンパイル.
	 * @param script 対象のスクリプトを設定します.
	 * @return Script コンパイル結果が返却されます.
	 * @throws Exception 例外.
	 */
	public static final Script compile(String script)
		throws Exception {
		return compile(script, null);
	}
	
	/**
	 * 指定javascriptをコンパイル.
	 * @param script 対象のスクリプトを設定します.
	 * @param name スクリプトファイル名を設定します.
	 * @return Script コンパイル結果が返却されます.
	 * @throws Exception 例外.
	 */
	public static final Script compile(String script, String name)
		throws Exception {
		return compile(new StringReader(script), name);
	}
	
	/**
	 * 指定javascriptをコンパイル.
	 * @param r readerを設定します.
	 * @param name スクリプトファイル名を設定します.
	 * @return Script コンパイル結果が返却されます.
	 * @throws Exception 例外.
	 */
	public static final Script compile(Reader r, String name)
		throws Exception {
		return compile(r, name, "", "", 1);
	}
	
	// スクリプト情報を生成.
	private static final Reader getScript(Reader r, String headerScript, String footerScript)
		throws Exception {
		if(headerScript.length() == 0 && footerScript.length() == 0) {
			return r;
		}
		int len;
		char[] c = new char[1024];
		StringBuilder buf = new StringBuilder(headerScript);
		while((len = r.read(c)) != -1) {
			buf.append(c, 0, len);
		}
		c = null;
		buf.append(footerScript);
		
		return new StringReader(buf.toString());
	}
	
	/**
	 * 指定javascriptをコンパイル.
	 * @param r readerを設定します.
	 * @param name スクリプトファイル名を設定します.
	 * @param headerScript ヘッダに追加するスクリプトを設定します.
	 * @param footerScript フッタに追加するスクリプトを設定します.
	 * @param lineNo ライン開始番号を設定します.
	 * @return Script コンパイル結果が返却されます.
	 * @throws Exception 例外.
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
		}
	}
	
	/**
	 * コンパイル結果を実行.
	 * @param context RhiginContextオブジェクトを設定します.
	 * @param compiled コンパイル済みオブジェクトを設定します.
	 * @return Object スクリプト実行結果を返却します.
	 * @throws Exception 例外.
	 */
	public static final Object execute(RhiginContext context, Script compiled)
		throws Exception {
		Context ctx = ContextFactory.getGlobal().enterContext();
		try {
			// 実行処理.
			Scriptable scope = new RhiginScriptable(context);
			scope.setPrototype(getTopLevel());
			settingRhiginObject(ctx, scope);
			return compiled.exec(ctx, scope);
		} finally {
			Context.exit();
		}
	}
	
	/**
	 * 指定javascriptを実行.
	 * @param context RhiginContextオブジェクトを設定します.
	 * @param script スクリプトを設定します.
	 * @return Object スクリプト実行結果を返却します.
	 * @throws Exception 例外.
	 */
	public static final Object execute(RhiginContext context, String script)
		throws Exception {
		return execute(context, script, null);
	}
	
	/**
	 * 指定javascriptを実行.
	 * @param context RhiginContextオブジェクトを設定します.
	 * @param script スクリプトを設定します.
	 * @param name スクリプトファイル名を設定します.
	 * @return Object スクリプト実行結果を返却します.
	 * @throws Exception 例外.
	 */
	public static final Object execute(RhiginContext context, String script, String name)
		throws Exception {
		return execute(context, new StringReader(script), name);
	}
	
	/**
	 * 指定javascriptを実行.
	 * @param context RhiginContextオブジェクトを設定します.
	 * @param r readerを設定します.
	 * @param name スクリプトファイル名を設定します.
	 * @return Object スクリプト実行結果を返却します.
	 * @throws Exception 例外.
	 */
	public static final Object execute(RhiginContext context, Reader r, String name)
		throws Exception {
		return execute(context, r, name, "", "", 1);
	}
	
	/**
	 * 指定javascriptを実行.
	 * @param context RhiginContextオブジェクトを設定します.
	 * @param r readerを設定します.
	 * @param name スクリプトファイル名を設定します.
	 * @param headerScript ヘッダに追加するスクリプトを設定します.
	 * @param footerScript フッタに追加するスクリプトを設定します.
	 * @param lineNo ライン開始番号を設定します.
	 * @return Object スクリプト実行結果を返却します.
	 * @throws Exception 例外.
	 */
	public static final Object execute(RhiginContext context, Reader r, String name, String headerScript, String footerScript, int lineNo)
		throws Exception {
		name = (name == null || name.isEmpty()) ? NO_SCRIPT_NAME : name;
		Context ctx = ContextFactory.getGlobal().enterContext();
		ctx.setOptimizationLevel(SCRIPT_NOT_COMPILE_OPTIMIZE_LEVEL);
		try {
			// 対象ソースをコンパイル.
			Scriptable scope = new RhiginScriptable(context);
			scope.setPrototype(getTopLevel());
			settingRhiginObject(ctx, scope);
			Script compiled = ctx.compileReader(getScript(r, headerScript, footerScript), name, lineNo, null);
			// 実行処理.
			return compiled.exec(ctx, scope);
		} finally {
			Context.exit();
		}
	}
	
	// 基本オブジェクトをセット.
	private static final void settingRhiginObject(Context ctx, Scriptable scope)
		throws Exception {
		
		// rhigin用の基本オブジェクトを設定.
		scope.put("console", scope, ConsoleObject.getInstance());
		scope.put("JSON", scope, JSONObject.getInstance());
		scope.put("require", scope, RequireFunction.getInstance());
		scope.put("binary", scope, BinaryFunction.getInstance());
		scope.put("getClass", scope, GetClassFunction.getInstance());
		scope.put("sleep", scope, SleepFunction.getInstance());
		scope.put("getEnv", scope, GetEnvFunction.getInstance());
		//ThreadFunction.set(scope);
		
		// オリジナルオブジェクトを設定.
		Object[] kv;
		final OList<Object[]> list = originalFunctionAndObjectList.rawData();
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			kv = list.get(i);
			scope.put((String)kv[0], scope, kv[1]);
		}
	}
	
	/**
	 * オリジナルなRhigin用のFunction及びオブジェクトを設定.
	 * @param args [name], [value], [name], [value] .... のように設定します.
	 */
	public static final void addOriginals(Object... args) {
		originalFunctionAndObjectList.set(args);
	}
	
	/**
	 * オリジナルなRhigin用のFunction及びオブジェクトを格納するオブジェクトを取得.
	 * @return ListMap 
	 */
	public static final ListMap getOriginal() {
		return originalFunctionAndObjectList;
	}
}