package rhigin.scripts.objects;

import java.time.Instant;
import java.util.Date;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginInstanceObject.ObjectFunction;
import rhigin.util.Converter;
import rhigin.util.DateConvert;
import rhigin.util.FixedKeyValues;
import rhigin.util.FixedSearchArray;

/**
 * [js]Java用日付オブジェクト.
 */
public class JDateObject extends RhiginFunction {
	public static final String OBJECT_NAME = "JDate";
	private static final JDateObject THIS = new JDateObject();

	public static final JDateObject getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return OBJECT_NAME;
	}
	
	/**
	 * オブジェクトを作成.
	 * @param d
	 * @return
	 */
	public static final JDateInstanceObject newObject(java.util.Date d) {
		return new JDateInstanceObject(d);
	}
	
	/**
	 * オブジェクトを作成.
	 * @param t
	 * @return
	 */
	public static final JDateInstanceObject newObject(long t) {
		return new JDateInstanceObject(t);
	}
	
	/**
	 * JDateインスタンスオブジェクト.
	 * java で利用できるように java.util.Date を継承する.
	 */
	@SuppressWarnings("deprecation")
	public static class JDateInstanceObject extends java.util.Date implements Scriptable {
		private static final long serialVersionUID = -3402163545126348166L;
		protected java.util.Date date;
		protected String name;
		protected ObjectFunction objectFunction;
		protected Object[] params;
		protected RhiginFunction[] list;
		
		public JDateInstanceObject(long time) {
			this(new java.util.Date(time));
		}
		public JDateInstanceObject(java.util.Date d) {
			this.date = d;
			this.name = OBJECT_NAME;
			this.objectFunction = FUNCTIONS;
			this.params = new Object[] {d};
			this.list = new RhiginFunction[objectFunction.getWord().size()];
		}
		@Override
		public Object get(String k, Scriptable s) {
			int no = objectFunction.getWord().search(k);
			if(no != -1) {
				RhiginFunction ret = list[no];
				if(ret == null) {
					ret = objectFunction.create(no, params);
					ret.setRhiginObject(this);
					list[no] = ret;
				}
				return ret;
			}
			return Undefined.instance;
		}
		@Override
		public boolean has(String k, Scriptable s) {
			return objectFunction.getWord().search(k) != -1;
		}
		@Override
		public Object[] getIds() {
			FixedSearchArray<String> s = objectFunction.getWord();
			final int len = s.size();
			Object[] ret = new Object[len];
			for (int i = 0; i < len; i ++) {
				ret[i] = s.get(i);
			}
			return ret;
		}
		@Override
		public Object getDefaultValue(Class<?> clazz) {
			return (clazz == null || String.class.equals(clazz)) ? toString() : null;
		}
		@Override
		public String getClassName() {
			return this.name;
		}
		@Override
		public String toString() {
			return DateConvert.getISO8601(date);
		}
		/**
		 * オブジェクト名を取得.
		 * @return
		 */
		public String getName() {
			return this.name;
		}
		
		// Scriptable.
		
		public java.util.Date getDateObject() {
			return this.date;
		}
		@Override
		public void delete(String arg0) {
		}
		@Override
		public void delete(int arg0) {
		}
		@Override
		public Object get(int arg0, Scriptable arg1) {
			return null;
		}
		@Override
		public Scriptable getParentScope() {
			return null;
		}
		@Override
		public Scriptable getPrototype() {
			return null;
		}
		@Override
		public boolean has(int arg0, Scriptable arg1) {
			return false;
		}
		@Override
		public boolean hasInstance(Scriptable instance) {
			if(instance != null) {
				return this.getClassName() == instance.getClassName();
			}
			return false;
		}
		@Override
		public void put(String arg0, Scriptable arg1, Object arg2) {
		}
		@Override
		public void put(int arg0, Scriptable arg1, Object arg2) {
		}
		@Override
		public void setParentScope(Scriptable arg0) {
		}
		@Override
		public void setPrototype(Scriptable arg0) {
		}
		
		// java.util.Date.
		
		@Override
		public boolean after(Date when) {
			return date.after(when);
		}
		@Override
		public boolean before(Date when) {
			return date.before(when);
		}
		@Override
		public Object clone() {
			return JDateObject.newObject(date.getTime());
		}
		@Override
		public int compareTo(Date anotherDate) {
			return date.compareTo(anotherDate);
		}
		@Override
		public boolean equals(Object obj) {
			return date.equals(obj);
		}
		@Override
		public int getDate() {
			return date.getDate();
		}
		@Override
		public int getDay() {
			return date.getDay();
		}
		@Override
		public int getHours() {
			return date.getHours();
		}
		@Override
		public int getMinutes() {
			return date.getMinutes();
		}
		@Override
		public int getMonth() {
			return date.getMonth();
		}
		@Override
		public int getSeconds() {
			return date.getSeconds();
		}
		@Override
		public long getTime() {
			return date.getTime();
		}
		@Override
		public int getTimezoneOffset() {
			return date.getTimezoneOffset();
		}
		@Override
		public int getYear() {
			return date.getYear();
		}
		@Override
		public int hashCode() {
			return date.hashCode();
		}
		@Override
		public void setDate(int d) {
			date.setDate(d);
		}
		@Override
		public void setHours(int hours) {
			date.setHours(hours);
		}
		@Override
		public void setMinutes(int minutes) {
			date.setMinutes(minutes);
		}
		@Override
		public void setMonth(int month) {
			date.setMonth(month);
		}
		@Override
		public void setSeconds(int seconds) {
			date.setSeconds(seconds);
		}
		@Override
		public void setTime(long time) {
			date.setTime(time);
		}
		@Override
		public void setYear(int year) {
			date.setYear(year);
		}
		@Override
		public String toGMTString() {
			return date.toGMTString();
		}
		@Override
		public Instant toInstant() {
			return date.toInstant();
		}
		@Override
		public String toLocaleString() {
			return date.toLocaleString();
		}
		// 拡張.
		public void setFullYear(int y) {
			date.setYear(y + 1900);
		}
		public int getFullYear() {
			return date.getYear() + 1900;
		}
	}
	
	// JDate用メソッド群.
	private static final class JDateFunction extends RhiginFunction {
		final int type;
		final java.util.Date date;

		JDateFunction(int t, java.util.Date date) {
			this.type = t;
			this.date = date;
		}

		@SuppressWarnings({ "deprecation", "static-access" })
		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length <= 0) {
				// 引数がなしの条件.
				switch (type) {
				case 0:
					return JDateObject.newObject(date.getTime());
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
			argsException(OBJECT_NAME);
			return null;
		}

		@Override
		public final String getName() {
			return FUNCTION_NAMES[type];
		}

//		@Override
//		public String toString() {
//			return date.toString();
//		}
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
			return new JDateFunction(no, (java.util.Date)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};

	@SuppressWarnings("deprecation")
	@Override
	public Scriptable jconstruct(Context ctx, Scriptable thisObject, Object[] args) {
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
				if(args[0] instanceof Number) {
					date = new java.util.Date(Converter.convertLong(args[0]));
				} else {
					date = DateConvert.stringToDate("" + args[0]);
				}
			} else {
				date = DateConvert.stringToDate("" + args[0]);
			}
		} else {
			date = new java.util.Date();
		}
		if (date == null) {
			throw new RhiginException(500, "Failed to initialize " + OBJECT_NAME + " object");
		}
		return newObject(date);
	}

	// コールさせない.
	@Override
	public Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		return Undefined.instance;
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put(OBJECT_NAME, scope, JDateObject.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(OBJECT_NAME, JDateObject.getInstance());
	}
}
