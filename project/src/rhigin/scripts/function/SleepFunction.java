package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.util.Converter;
import rhigin.util.FixedKeyValues;

/**
 * [js]sleep処理.
 */
public class SleepFunction extends RhiginFunction {
	private static final SleepFunction THIS = new SleepFunction();

	public static final SleepFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "sleep";
	}

	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (args.length >= 1 && Converter.isNumeric(args[0])) {
			try {
				Thread.sleep(Converter.convertInt(args[0]));
			} catch (Exception e) {
			}
			return Undefined.instance;
		}
		return argsException();
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("sleep", scope, SleepFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("sleep", SleepFunction.getInstance());
	}
}
