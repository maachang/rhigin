package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.http.Http;
import rhigin.http.HttpInfo;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.JavaRequire;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.ScriptConstants;
import rhigin.scripts.compile.CompileCache;
import rhigin.scripts.compile.ScriptElement;
import rhigin.util.Converter;
import rhigin.util.FixedKeyValues;

/**
 * [Function]: require.
 */
public final class RequireFunction extends RhiginFunction {
	private static final RequireFunction THIS = new RequireFunction();

	public static final RequireFunction getInstance() {
		return THIS;
	}

	// threadローカルでCompileCacaheを管理.
	private final ThreadLocal<CompileCache> cache = new ThreadLocal<CompileCache>();

	protected final void setCache(CompileCache c) {
		cache.set(c);
	}

	public final CompileCache getCache() {
		// 別スレッドを作成した場合の拡張対応.
		CompileCache ret = cache.get();
		if (ret == null) {
			HttpInfo info = Http.getHttpInfo();
			if (info != null) {
				ret = new CompileCache(info.getCompileCacheSize(), info.getCompileCacheRootDir());
				cache.set(ret);
			}
		}
		return ret;
	}
	
	@Override
	public Object get(String arg0, Scriptable arg1) {
		return Undefined.instance;
	}
	
	@Override
	public boolean has(String arg0, Scriptable arg1) {
		return false;
	}
	
	@Override
	public Object[] getIds() {
		return ScriptConstants.BLANK_ARGS;
	}


	// ヘッダ・フッタ.
	private static final String HEADER_SCRIPT = "'use strict';(function(_g){var _$def_$exports={};var module= {exports:_$def_$exports};var exports=_$def_$exports;\n";
	private static final String FOOTER_SCRIPT = "\nreturn (module.exports!=_$def_$exports)?module.exports:exports;})(this);";

	@Override
	public String getName() {
		return "require";
	}

	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (args == null || args.length < 1) {
			throw new RhiginException(404, "The require argument has not been set.");
		}
		String path = "" + args[0];
		// パスの先頭に[@]が存在する場合は、javaのクラス[JavaRequire]でロード.
		if(path.startsWith("@")) {
			try {
				// パス名は/は[.]に変換.
				path = Converter.changeString(path.substring(1), "/", ".");
				// コンストラクタは引数が空のものでインスタンスで作成して、JavaRequire.loadで
				// js用のオブジェクトを生成.
				return ((JavaRequire)Class.forName(path).getConstructor().newInstance()).load();
			} catch (Exception e) {
				throw new RhiginException(500, e);
			}
		}
		try {
			CompileCache c = getCache();
			if (c == null) {
				throw new RhiginException(500, "compileCache has not been set.");
			}
			ScriptElement se = c.get(path, HEADER_SCRIPT, FOOTER_SCRIPT);
			return ExecuteScript.eval(se.getScript());
		} catch (RhiginException re) {
			throw re;
		} catch (Exception e) {
			throw new RhiginException(500, e);
		}
	}

	/**
	 * 初期化処理.
	 * 
	 * @param cache
	 *            スクリプトコンパイルキャッシュを設定します.
	 */
	public static final void init(CompileCache cache) {
		RequireFunction.getInstance().setCache(cache);
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("require", scope, RequireFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("require", RequireFunction.getInstance());
	}
}
