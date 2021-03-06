package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.util.Converter;
import rhigin.util.FixedKeyValues;

/**
 * [js]parseInt32用処理.
 * 
 * javaのint型に変換する.
 */
public class ParseInt32Function extends RhiginFunction {
	private static final ParseInt32Function THIS = new ParseInt32Function();

	public static final ParseInt32Function getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "parseInt32";
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
			if(s.length() > 2 && s.charAt(0) == '0' && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
				s = s.substring(2);
				radix = 16;
			}
			if(radix == 10) {
				return Converter.parseInt(s);
			}
			return Integer.parseInt(s, radix);
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
		scope.put("parseInt32", scope, ParseInt32Function.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("parseInt32", ParseInt32Function.getInstance());
	}
}
