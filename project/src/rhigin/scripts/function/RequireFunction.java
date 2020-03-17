package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.scripts.JavaRequire;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.ScriptConstants;
import rhigin.scripts.compile.CompileCache;
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (args == null || args.length < 1) {
			throw new RhiginException(404, "The require argument has not been set.");
		}
		String path = "" + args[0];
		// パスの先頭に[@]が存在する場合は、javaのクラス[JavaRequire]でロード.
		if(path.startsWith("@")) {
			// パス名は/は[.]に変換.
			path = Converter.changeString(path.substring(1), "/", ".");
			Class c = null;
			try {
				// コンストラクタは引数が空のものでインスタンスで作成して、JavaRequire.loadで
				// js用のオブジェクトを生成.
				c = Class.forName(path);
				return ((JavaRequire)c.getConstructor().newInstance()).load();
			} catch (Exception e) {
				if(c == null) {
					throw new RhiginException(500, "Failed to read the specified class: " + args[0], e);
				}
				throw new RhiginException(500, "The specified class is not an inherited object of 'JavaRequire': " + args[0], e);
			}
		}
		return CompileCache.eval(path, HEADER_SCRIPT, FOOTER_SCRIPT);
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
