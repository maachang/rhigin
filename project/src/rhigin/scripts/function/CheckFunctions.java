package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.Scriptable;

import rhigin.scripts.RhiginFunction;
import rhigin.util.Converter;
import rhigin.util.DateConvert;
import rhigin.util.FixedKeyValues;

/**
 * Numericファンクション.
 */
public class CheckFunctions {
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

	// isDate
	private static final class IsDateFunction extends RhiginFunction {
		private static final IsDateFunction THIS = new IsDateFunction();

		public static final IsDateFunction getInstance() {
			return THIS;
		}

		@Override
		public String getName() {
			return "isDate";
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				if(args[0] instanceof IdScriptableObject &&
					"Date".equals(((IdScriptableObject)args[0]).getClassName())) {
					return true;
				} else if(args[0] instanceof java.util.Date) {
					return true;
				} else if(args[0] instanceof String) {
					// 文字列の場合は、ISO8601形式のみ対応する.
					try {
						return DateConvert.isISO8601((String)args[0]);
					} catch(Exception e) {
					}
				}
				return false;
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
		scope.put("isDate", scope, IsDateFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("isNumeric", IsNumericFunction.getInstance());
		fkv.put("isFloat", IsFloatFunction.getInstance());
		fkv.put("isDate", IsDateFunction.getInstance());
	}

}
