package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.util.Converter;

/**
 * [js]parseInt用処理.
 * 
 * rhinoのparseIntでは Double型に変換されるので、longに変換したかったので、別途作成.
 */
public class ParseIntFunction extends RhiginFunction {
	private static final ParseIntFunction THIS = new ParseIntFunction();

	public static final ParseIntFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "parseInt";
	}

	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if(args == null || args.length == 0) {
			return Undefined.instance;
		}
		Object o = args[0];
		int radix = args.length >= 2 && Converter.isNumeric(args[1]) ?
			Converter.convertInt(args[1]) : 10;
		if(o instanceof String) {
			String s = (String)o;
			if(s.startsWith("0x") || s.startsWith("0X")) {
				s = s.substring(2);
				radix = 16;
			}
			if(radix == 10) {
				return Converter.parseLong(s);
			}
			return Long.parseLong(s, radix);
		}
		return Converter.convertLong(o);
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("parseInt", scope, ParseIntFunction.getInstance());
	}
}
