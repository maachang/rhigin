package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.util.FixedKeyValues;

/**
 * ガベージコレクション呼び出し.
 */
public class GcFunction extends RhiginFunction {
	private static final GcFunction THIS = new GcFunction();

	public static final GcFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "gc";
	}

	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		System.gc();
		return Undefined.instance;
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("gc", scope, GcFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("gc", GcFunction.getInstance());
	}
}
