package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.util.Converter;
import rhigin.util.DateConvert;
import rhigin.util.FixedKeyValues;

/**
 * Numericファンクション.
 */
public class CheckFunctions {
	// isNull.
	private static class IsNullFunction extends RhiginFunction {
		private static final IsNullFunction THIS = new IsNullFunction();

		public static final IsNullFunction getInstance() {
			return THIS;
		}

		@Override
		public String getName() {
			return "isNull";
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				return args[0] == null || Undefined.isUndefined(args[0]);
			}
			return argsException();
		}
	}
	
	// useString.
	private static class UseStringFunction extends RhiginFunction {
		private static final UseStringFunction THIS = new UseStringFunction();

		public static final UseStringFunction getInstance() {
			return THIS;
		}

		@Override
		public String getName() {
			return "useString";
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				return !Undefined.isUndefined(args[0]) && Converter.useString(args[0]);
			}
			return argsException();
		}
	}

	
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
		scope.put("isNull", scope, IsNullFunction.getInstance());
		scope.put("useString", scope, UseStringFunction.getInstance());
		scope.put("isNumeric", scope, IsNumericFunction.getInstance());
		scope.put("isFloat", scope, IsFloatFunction.getInstance());
		scope.put("isDate", scope, IsDateFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("isNull", IsNullFunction.getInstance());
		fkv.put("useString", UseStringFunction.getInstance());
		fkv.put("isNumeric", IsNumericFunction.getInstance());
		fkv.put("isFloat", IsFloatFunction.getInstance());
		fkv.put("isDate", IsDateFunction.getInstance());
	}

}
