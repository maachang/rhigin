package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.RhiginException;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginInstanceObject;
import rhigin.scripts.RhiginInstanceObject.ObjectFunction;
import rhigin.util.Converter;
import rhigin.util.DateConvert;
import rhigin.util.FixedSearchArray;

/**
 * [js]Java用日付オブジェクト.
 */
public class JDateObject extends RhiginFunction {
	private static final JDateObject THIS = new JDateObject();

	public static final JDateObject getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "JDate";
	}
	
	// メソッド名群.
	private static final String[] FUNCTION_NAMES = new String[] {
		"clone"
		,"getDate"
		,"getDay"
		,"getHours"
		,"getMinutes"
		,"getMonth"
		,"getSeconds"
		,"getTime"
		,"getTimezoneOffset"
		,"getYear"
		,"toGMTString"
		,"toLocaleString"
		,"getFullYear"
		,"after"
		,"before"
		,"compareTo"
		,"equals"
		,"parse"
		,"setDate"
		,"setHours"
		,"setMinutes"
		,"setMonth"
		,"setSeconds"
		,"setTime"
		,"setYear"
		,"setFullYear"
		,"toString"
		,"hashCode"
		,"object"
	};
	
	// メソッド生成処理.
	private static final ObjectFunction FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(FUNCTION_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new Execute(no, (java.util.Date)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};

	@SuppressWarnings("deprecation")
	@Override
	public Scriptable construct(Context ctx, Scriptable thisObject, Object[] args) {
		java.util.Date date = null;
		if (args.length >= 1) {
			if (args.length >= 6) {
				if (Converter.isNumeric(args[0]) && Converter.isNumeric(args[1]) && Converter.isNumeric(args[2])
						&& Converter.isNumeric(args[3]) && Converter.isNumeric(args[4])
						&& Converter.isNumeric(args[5])) {
					date = new java.util.Date(Converter.convertInt(args[0]) - 1900, Converter.convertInt(args[1]),
							Converter.convertInt(args[2]), Converter.convertInt(args[3]), Converter.convertInt(args[4]),
							Converter.convertInt(args[5]));
				}
			} else if (args.length >= 5) {
				if (Converter.isNumeric(args[0]) && Converter.isNumeric(args[1]) && Converter.isNumeric(args[2])
						&& Converter.isNumeric(args[3]) && Converter.isNumeric(args[4])) {
					date = new java.util.Date(Converter.convertInt(args[0]) - 1900, Converter.convertInt(args[1]),
							Converter.convertInt(args[2]), Converter.convertInt(args[3]),
							Converter.convertInt(args[4]));
				}
			} else if (args.length >= 3) {
				if (Converter.isNumeric(args[0]) && Converter.isNumeric(args[1]) && Converter.isNumeric(args[2])) {
					date = new java.util.Date(Converter.convertInt(args[0]) - 1900, Converter.convertInt(args[1]),
							Converter.convertInt(args[2]));
				}
			} else if (Converter.isNumeric(args[0])) {
				date = new java.util.Date(Converter.convertLong(args[0]));
			} else {
				date = new java.util.Date("" + args[0]);
			}
		} else {
			date = new java.util.Date();
		}
		if (date == null) {
			throw new RhiginException(500, "Failed to initialize JDate object");
		}
		return new RhiginInstanceObject("JDate", FUNCTIONS, date);
	}

	// JDate用メソッド群.
	private static final class Execute extends RhiginFunction {
		final int type;
		final java.util.Date date;

		Execute(int t, java.util.Date date) {
			this.type = t;
			this.date = date;
		}

		@SuppressWarnings({ "deprecation", "static-access" })
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length <= 0) {
				// 引数がなしの条件.
				switch (type) {
				case 0:
					return date.clone();
				case 1:
					return date.getDate();
				case 2:
					return date.getDay();
				case 3:
					return date.getHours();
				case 4:
					return date.getMinutes();
				case 5:
					return date.getMonth();
				case 6:
					return date.getSeconds();
				case 7:
					return date.getTime();
				case 8:
					return date.getTimezoneOffset();
				case 9:
					return date.getYear();
				case 10:
					return date.toGMTString();
				case 11:
					return date.toLocaleString();
				case 12:
					return date.getYear() + 1900;
				case 26:
					return DateConvert.getISO8601(date);
				case 27:
					return date.hashCode();
				case 28:
					return date;
				}
			} else {
				// 引数が必要な場合.
				Object o = args[0];
				switch (type) {
				case 13:
					return date.after(Converter.convertDate(o));
				case 14:
					return date.before(Converter.convertDate(o));
				case 15:
					return date.compareTo(Converter.convertDate(o));
				case 16:
					return date.equals(o);
				case 17:
					return date.parse("" + o);
				case 18:
					date.setDate(Converter.convertInt(o));
					break;
				case 19:
					date.setHours(Converter.convertInt(o));
					break;
				case 20:
					date.setMinutes(Converter.convertInt(o));
					break;
				case 21:
					date.setMonth(Converter.convertInt(o));
					break;
				case 22:
					date.setSeconds(Converter.convertInt(o));
					break;
				case 23:
					date.setTime(Converter.convertLong(o));
					break;
				case 24:
					date.setYear(Converter.convertInt(o));
					break;
				case 25:
					date.setYear(Converter.convertInt(o) - 1900);
					break;
				}
			}
			// 引数が必要な条件で、引数が無い場合はエラー.
			argsException("JDate");
			return null;
		}

		@Override
		public final String getName() {
			return FUNCTION_NAMES[type];
		}

		@Override
		public String toString() {
			return date.toString();
		}
	}
	
	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("JDate", scope, JDateObject.getInstance());
	}
}
