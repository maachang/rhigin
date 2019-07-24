package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.Converter;

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
	
	@SuppressWarnings("deprecation")
	@Override
	public Scriptable construct(Context ctx, Scriptable thisObject, Object[] args) {
		java.util.Date date = null;
		if(args.length >= 1) {
			if(Converter.isNumeric(args[0])) {
				date = new java.util.Date(Converter.convertLong(args[0]));
			} else {
				date = new java.util.Date("" + args[0]);
			}
		} else if(args.length == 3) {
			if(Converter.isNumeric(args[0]) && Converter.isNumeric(args[1]) && Converter.isNumeric(args[2])) {
				date = new java.util.Date(Converter.convertInt(args[0]) + 1900, Converter.convertInt(args[1]), Converter.convertInt(args[2]));
			}
		} else if(args.length == 5) {
			if(Converter.isNumeric(args[0]) && Converter.isNumeric(args[1]) && Converter.isNumeric(args[2])
					 && Converter.isNumeric(args[3]) && Converter.isNumeric(args[4])) {
				date = new java.util.Date(Converter.convertInt(args[0]) + 1900, Converter.convertInt(args[1]), Converter.convertInt(args[2])
						, Converter.convertInt(args[3]), Converter.convertInt(args[4]));
			}
		} else if(args.length == 6) {
			if(Converter.isNumeric(args[0]) && Converter.isNumeric(args[1]) && Converter.isNumeric(args[2])
					 && Converter.isNumeric(args[3]) && Converter.isNumeric(args[4]) && Converter.isNumeric(args[5])) {
				date = new java.util.Date(Converter.convertInt(args[0]) + 1900, Converter.convertInt(args[1]), Converter.convertInt(args[2])
						, Converter.convertInt(args[3]), Converter.convertInt(args[4]), Converter.convertInt(args[5]));
			}
		} else {
			date = new java.util.Date();
		}
		if(date == null) {
			throw new RhiginException(500, "Failed to initialize JDate object");
		}
		
		return new JDateClass("JDate", new RhiginFunction[] {
			new Execute(0, date),
			new Execute(1, date),
			new Execute(2, date),
			new Execute(3, date),
			new Execute(4, date),
			new Execute(5, date),
			new Execute(6, date),
			new Execute(7, date),
			new Execute(8, date),
			new Execute(9, date),
			new Execute(10, date),
			new Execute(11, date),
			new Execute(12, date),
			new Execute(13, date),
			new Execute(14, date),
			new Execute(15, date),
			new Execute(16, date),
			new Execute(17, date),
			new Execute(18, date),
			new Execute(19, date),
			new Execute(20, date),
			new Execute(20, date),
			new Execute(21, date),
			new Execute(22, date),
			new Execute(23, date),
			new Execute(24, date),
			new Execute(25, date),
			new Execute(26, date),
			new Execute(99, date)
		});
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
			switch(type) {
			case 0: return date.clone();
			case 1: return date.getDate();
			case 2: return date.getDay();
			case 3: return date.getHours();
			case 4: return date.getMinutes();
			case 5: return date.getMonth();
			case 6: return date.getSeconds();
			case 7: return date.getTime();
			case 8: return date.getTimezoneOffset();
			case 9: return date.getYear();
			case 10: return date.toGMTString();
			case 11: return date.toLocaleString();
			case 12: return date.getYear() + 1900;
			case 26: return date.toString();
			case 27: return date.hashCode();
			case 99: return date;
			}
			if(args.length >= 1) {
				Object o = args[0];
				switch(type) {
				case 13: return date.after(Converter.convertDate(o));
				case 14: return date.before(Converter.convertDate(o));
				case 15: return date.compareTo(Converter.convertDate(o));
				case 16: return date.equals(o);
				case 17: return date.parse(""+o);
				case 18: date.setDate(Converter.convertInt(o));break;
				case 19: date.setHours(Converter.convertInt(o));break;
				case 20: date.setMinutes(Converter.convertInt(o));break;
				case 21: date.setMonth(Converter.convertInt(o));break;
				case 22: date.setSeconds(Converter.convertInt(o));break;
				case 23: date.setTime(Converter.convertLong(o));break;
				case 24: date.setYear(Converter.convertInt(o));break;
				case 25: date.setYear(Converter.convertInt(o) + 1900);break;
				}
			}
			return Undefined.instance;
		}
		@Override
		public final String getName() {
			switch(type) {
			case 0: return "clone";
			case 1: return "getDate";
			case 2: return "getDay";
			case 3: return "getHours";
			case 4: return "getMinutes";
			case 5: return "getMonth";
			case 6: return "getSeconds";
			case 7: return "getTime";
			case 8: return "getTimezoneOffset";
			case 9: return "getYear";
			case 10: return "toGMTString";
			case 11: return "toLocaleString";
			case 12: return "getFullYear";
			case 13: return "after";
			case 14: return "before";
			case 15: return "compareTo";
			case 16: return "equals";
			case 17: return "parse";
			case 18: return "setDate";
			case 19: return "setHours";
			case 20: return "setMinutes";
			case 21: return "setMonth";
			case 22: return "setSeconds";
			case 23: return "setTime";
			case 24: return "setYear";
			case 25: return "setFullYear";
			case 26: return "toString";
			case 27: return "hashCode";
			case 99: return "object";
			}
			return "unknown";
		}
		
		@Override
		public String toString() {
			return date.toString();
		}
	}
	
	// JDate用クラス.
	private static final class JDateClass extends RhiginObject {
		public JDateClass(String name, RhiginFunction[] list) {
			super(name, list);
		}
		@Override
		public String toString() {
			return ((Execute)get("object", null)).date.toString();
		}
	}
	
	/**
	 * スコープにライブラリを登録.
	 * @param scope 登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("JDate", scope, JDateObject.getInstance());
	}
}
