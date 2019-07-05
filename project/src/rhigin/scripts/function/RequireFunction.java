package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.RhiginException;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.comple.CompileCache;
import rhigin.scripts.comple.ScriptElement;

/**
 * [Function]: requireメソッド.
 */
public final class RequireFunction extends AbstractFunction {
	private static final RequireFunction THIS = new RequireFunction();
	public static final RequireFunction getInstance() {
		return THIS;
	}
	
	// threadローカルでCompileCacaheを管理.
	private final ThreadLocal<CompileCache> cache = new ThreadLocal<CompileCache>();
	public final void setCache(CompileCache c) {
		cache.set(c);
	}
	
	// ヘッダ・フッタ.
	private static final String HEADER_SCRIPT = "'use strict';(function(_g){var _$def_$exports={};var module= {exports:_$def_$exports};var exports=_$def_$exports;\n";
	private static final String FOOTER_SCRIPT = "\nreturn (module.exports!=_$def_$exports)?module.exports:exports;})(this);";

	@Override
	public String getName() {
		return "require";
	}
	
	@Override
    public final Object call(Context ctx, Scriptable scope, Scriptable thisObj,
                       Object[] args)
    {
		if(args == null || args.length < 1) {
			throw new RhiginException(404, "require引数が設定されていません");
		}
		try {
			CompileCache c = cache.get();
			if(c == null) {
				throw new RhiginException(500, "compileCacheが設定されていません");
			}
			ScriptElement se = c.get(""+args[0], HEADER_SCRIPT, FOOTER_SCRIPT);
			return ExecuteScript.execute(null, se.getScript());
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(500, e);
		}
    }
	
	
}
