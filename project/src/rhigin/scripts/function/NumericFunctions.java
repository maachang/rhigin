package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.scripts.RhiginFunction;
import rhigin.util.Converter;

/**
 * Numericファンクション.
 */
public class NumericFunctions {
	// isNumeric.
	private static class IsNumericFunction extends RhiginFunction {
		private static final IsNumericFunction THIS = new IsNumericFunction();

		public static final IsNumericFunction getInstance() {
			return THIS;
		}

		@Override
		public String getName() {
			return "isNumeric";
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				return Converter.isNumeric(args[0]);
			}
			return argsException();
		}
	}

	// isFloat
	private static final class IsFloatFunction extends RhiginFunction {
		private static final IsFloatFunction THIS = new IsFloatFunction();

		public static final IsFloatFunction getInstance() {
			return THIS;
		}

		@Override
		public String getName() {
			return "isFloat";
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				return Converter.isFloat(args[0]);
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
		scope.put("isNumeric", scope, IsNumericFunction.getInstance());
		scope.put("isFloat", scope, IsFloatFunction.getInstance());
	}
}
