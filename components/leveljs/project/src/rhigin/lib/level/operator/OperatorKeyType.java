package rhigin.lib.level.operator;

import java.util.List;

import org.maachang.leveldb.LevelOption;
import org.maachang.leveldb.LeveldbException;
import org.maachang.leveldb.util.Alphabet;
import org.maachang.leveldb.util.Converter;

import rhigin.lib.level.runner.RhiginOriginCode;
import rhigin.scripts.objects.JDateObject;

/**
 * オペレータが対応するキータイプ.
 */
public class OperatorKeyType {
	/** キータイプ: 不明. **/
	public static final int KEY_NONE = -1;
	/** キータイプ: 文字列. **/
	public static final int KEY_STRING = 0;
	/** キータイプ: ３２ビット数値. **/
	public static final int KEY_INT = 1;
	/** キータイプ: ６４ビット数値. **/
	public static final int KEY_LONG = 2;
	/** キータイプ: ３２ビット浮動小数点. **/
	public static final int KEY_FLOAT = 3;
	/** キータイプ: ６４ビット浮動小数点. **/
	public static final int KEY_DOUBLE = 4;
	/** キータイプ: 日付. **/
	public static final int KEY_DATE = 5;
	/** キータイプ: 時間. **/
	public static final int KEY_TIME = 6;
	/** キータイプ: タイムスタンプ. **/
	public static final int KEY_TIMESTAMP = 7;
	
	// インデックスタイプ文字列パターン.
	private static final String[] PATTERN_STR = new String[] { "str", "string", "char" };
	private static final String[] PATTERN_INT = new String[] { "n32", "int", "integer", "number32" };
	private static final String[] PATTERN_LONG = new String[] { "n64", "long", "number64", "bigint" };
	private static final String[] PATTERN_FLOAT = new String[] { "float", "decimal32", "dec32", "float32" };
	private static final String[] PATTERN_DOUBLE = new String[] { "double", "decimal64", "dec64", "float64" };
	private static final String[] PATTERN_DATE = new String[] { "date" };
	private static final String[] PATTERN_TIME = new String[] { "time" };
	private static final String[] PATTERN_TIMESTAMP = new String[] { "datetime", "timestamp" };
	private static final Object[] PATTERNS = new Object[] {
		PATTERN_STR, PATTERN_INT, PATTERN_LONG, PATTERN_FLOAT, PATTERN_DOUBLE,
		PATTERN_DATE, PATTERN_TIME, PATTERN_TIMESTAMP
	};

	/**
	 * 文字列で設定されたカラムタイプを数値変換.
	 * @param name
	 * @return
	 */
	public static final int convertStringByKeyType(final String name) {
		int j, lenJ;
		String[] target;
		final int len = PATTERNS.length;
		for(int i = 0; i < len; i ++) {
			target = (String[])PATTERNS[i];
			lenJ = target.length;
			for (j = 0; j < lenJ; j++) {
				if (Alphabet.eq(name, target[j])) {
					return i;
				}
			}
		}
		return KEY_NONE;
	}
	
	/**
	 * 文字列変換.
	 * @param type
	 * @return
	 */
	public static final String toString(final int type) {
		switch(type) {
		case KEY_STRING: return "string";
		case KEY_INT: return "int";
		case KEY_LONG: return "long";
		case KEY_FLOAT: return "float";
		case KEY_DOUBLE: return "double";
		case KEY_DATE: return "date";
		case KEY_TIME: return "time";
		case KEY_TIMESTAMP: return "timestamp";
		}
		return "none";
	}
	
	/**
	 * キータイプをLevelOptionのタイプに変換.
	 * @param type
	 * @return
	 */
	public static final int convertLevelOptionType(final int type) {
		switch(type) {
		case KEY_NONE: return LevelOption.TYPE_NONE;
		case KEY_STRING: return LevelOption.TYPE_STRING;
		case KEY_INT: return LevelOption.TYPE_NUMBER32;
		case KEY_LONG: return LevelOption.TYPE_NUMBER64;
		case KEY_FLOAT: return LevelOption.TYPE_NUMBER32;
		case KEY_DOUBLE: return LevelOption.TYPE_NUMBER64;
		case KEY_DATE: return LevelOption.TYPE_NUMBER32;
		case KEY_TIME: return LevelOption.TYPE_NUMBER32;
		case KEY_TIMESTAMP: return LevelOption.TYPE_NUMBER64;
		}
		throw new LeveldbException("Unknown key type: " + type);
	}
	
	/**
	 * キータイプに従ってLevedbのキー形式に変換.
	 * @param type
	 * @param o
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static final Object convertKeyType(final int type, Object o) {
		if(o == null) {
			return null;
		}
		Object v;
		switch(type) {
		case KEY_NONE:
			return null;
		case KEY_STRING:
			return Converter.convertString(o);
		case KEY_INT:
			return Converter.convertInt(o);
		case KEY_LONG:
			return Converter.convertLong(o);
		case KEY_FLOAT:
			return Float.floatToRawIntBits(Converter.convertFloat(o));
		case KEY_DOUBLE:
			return Double.doubleToRawLongBits(Converter.convertDouble(o));
		case KEY_DATE:
			java.sql.Date d;
			// js系の日付の場合.
			if((v = RhiginOriginCode.getJSDate(o)) != null) {
				d = new java.sql.Date((long)v);
			} else {
				d = Converter.convertSqlDate(o);
			}
			// yyyyMMdd の数字変換.
			return ((d.getYear() + 1900) * 10000) +
				((d.getMonth() + 1) * 100) +
				d.getDate();
		case KEY_TIME:
			// js系の日付の場合.
			java.sql.Time t;
			if((v = RhiginOriginCode.getJSDate(o)) != null) {
				t = new java.sql.Time((long)v);
			} else {
				t = Converter.convertSqlTime(o);
			}
			// HHmmss の数値変換.
			return ((t.getHours() * 10000)) +
				(t.getMinutes() * 100) +
				t.getSeconds();
		case KEY_TIMESTAMP:
			// js系の日付の場合.
			if((v = RhiginOriginCode.getJSDate(o)) != null) {
				return v;
			}
			return Converter.convertDate(o).getTime();
		}
		throw new LeveldbException("Unknown key type: " + type);
	}
	
	/**
	 * Leveldbに格納されているキーを復元.
	 * @param type
	 * @param o
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static final Object getRestoreKey(final int type, Object o) {
		if(o == null) {
			return null;
		}
		int ymd, hms, y, m, d, h, s;
		switch(type) {
		case KEY_NONE:
			return null;
		case KEY_STRING:
			return Converter.convertString(o);
		case KEY_INT:
			return Converter.convertInt(o);
		case KEY_LONG:
			return Converter.convertLong(o);
		case KEY_FLOAT:
			return Float.intBitsToFloat(Converter.convertInt(o));
		case KEY_DOUBLE:
			return Double.longBitsToDouble(Converter.convertLong(o));
		case KEY_DATE:
			// yyyyMMdd の数字を変換.
			ymd = Converter.convertInt(o);
			y = ymd / 10000;
			m = (ymd - (y * 10000)) / 100;
			d = (ymd - (y * 10000)) - (m * 100);
			return JDateObject.newObject(new java.sql.Date(y-1900, m-1, d));
		case KEY_TIME:
			// HHmmss の数値を変換.
			hms = Converter.convertInt(o);
			h = hms / 10000;
			m = (hms - (h * 10000)) / 100;
			s = (hms - (h * 10000)) - (m * 100);
			return JDateObject.newObject(new java.sql.Time(h, m, s));
		case KEY_TIMESTAMP:
			return JDateObject.newObject(Converter.convertLong(o));
		}
		throw new LeveldbException("Unknown key type: " + type);
	}
	
	/**
	 * LevelOption に設定されているKeyTypeを取得.
	 * @param opt
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static final int getKeyType(LevelOption opt) {
		List o = opt.getExpansion();
		if(o == null || o.size() == 0 || !Converter.isNumeric(o.get(0))) {
			return OperatorKeyType.KEY_NONE;
		}
		return Converter.convertInt(o.get(0));
	}
}
