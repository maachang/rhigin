package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.util.ColorConsoleOut;
import rhigin.util.FixedKeyValues;

/**
 * コンソール上の文字のカラー設定できる出力を作成.
 */
public class ColorOutFunction extends RhiginFunction {
	private static final ColorOutFunction THIS = new ColorOutFunction();

	public static final ColorOutFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "colorOut";
	}

	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if(args.length > 0 && args[0] instanceof String) {
			return ColorConsoleOut.getInstance().toString((String)args[0]);
		}
		return Undefined.instance;
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("colorOut", scope, ColorOutFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("colorOut", ColorOutFunction.getInstance());
	}
}
