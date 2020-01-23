package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.scripts.RhiginFunction;
import rhigin.util.Base64;

/**
 * [js]base64ファンクション.
 */
public class Base64Functions {
	// base64デコード.
	private static class AtobFunction extends RhiginFunction {
		private static final AtobFunction THIS = new AtobFunction();

		public static final AtobFunction getInstance() {
			return THIS;
		}

		@Override
		public String getName() {
			return "atob";
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				if (args.length >= 2) {
					boolean binaryFlag = (args[1] instanceof Boolean) ? (boolean) args[1] : false;
					if (binaryFlag) {
						return Base64.decode("" + args[0]);
					}
				}
				return Base64.atob("" + args[0]);
			}
			return argsException();
		}
	}

	// Base64エンコード.
	private static final class BtoaFunction extends RhiginFunction {
		private static final BtoaFunction THIS = new BtoaFunction();

		public static final BtoaFunction getInstance() {
			return THIS;
		}

		@Override
		public String getName() {
			return "btoa";
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				return Base64.btoa(args[0]);
			}
			return argsException();
		}
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("atob", scope, AtobFunction.getInstance());
		scope.put("btoa", scope, BtoaFunction.getInstance());
	}
}
