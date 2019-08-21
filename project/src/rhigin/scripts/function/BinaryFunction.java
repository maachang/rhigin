package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.RhiginException;
import rhigin.scripts.RhiginFunction;
import rhigin.util.Converter;

/**
 * [js]Binary生成メソッド.
 */
public final class BinaryFunction extends RhiginFunction {
	private static final BinaryFunction THIS = new BinaryFunction();
	public static final BinaryFunction getInstance() {
		return THIS;
	}
	
	@Override
	public String getName() {
		return "binary";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if(args.length >= 1) {
			if(Converter.isFloat(args[0])) {
				return new byte[Converter.convertInt(args[0])];
			} else if(args[0] instanceof String) {
				try {
					if(args.length >= 2) {
						return ((String)args[0]).getBytes("" + args[1]);
					}
					return ((String)args[0]).getBytes("UTF8");
				} catch(Exception e) {
					throw new RhiginException(500, e);
				}
			}
		}
		return argsException();
	}
	
	/**
	 * スコープにライブラリを登録.
	 * @param scope 登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("binary", scope, BinaryFunction.getInstance());
	}
}