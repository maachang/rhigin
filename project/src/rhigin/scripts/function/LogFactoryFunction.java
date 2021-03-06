package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.logs.LogFactory;
import rhigin.scripts.RhiginFunction;
import rhigin.util.FixedKeyValues;

/**
 * [js]ログファクトリ用ファンクション.
 */
public class LogFactoryFunction extends RhiginFunction {
	private static final LogFactoryFunction THIS = new LogFactoryFunction();

	public static final LogFactoryFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "logFactory";
	}

	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (args.length >= 1) {
			return LogFactory.create("" + args[0]);
		}
		return LogFactory.create();
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("logFactory", scope, LogFactoryFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("logFactory", LogFactoryFunction.getInstance());
	}
}
