package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.scripts.RhiginFunction;
import rhigin.util.Args;
import rhigin.util.Converter;

/**
 * [js]rhigin実行時の引数I/Oメソッド.
 */
public final class ArgsFunction extends RhiginFunction {
	private static final ArgsFunction THIS = new ArgsFunction();
	public static final ArgsFunction getInstance() {
		return THIS;
	}
	
	@Override
	public String getName() {
		return "args";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if(args.length >= 1) {
			Object o = args[0];
			if(Converter.isNumeric(o)) {
				return Args.get()[Converter.convertInt(o)];
			} else {
				return Args.getInstance().get(""+o);
			}
		}
		return Args.get().length;
	}
	
	/**
	 * スコープにライブラリを登録.
	 * @param scope 登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("args", scope, ArgsFunction.getInstance());
	}
}