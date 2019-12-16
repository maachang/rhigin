package rhigin.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 変換処理系.
 */
public final class Converter {
	private Converter() {
	}

	/** char文字のチェックを行う配列. **/
	public static final byte[] CHECK_CHAR = new byte[65536];
	static {
		// スペース系は１.
		// ドットは２.
		// 数字の終端文字は３.
		CHECK_CHAR[' '] = 1;
		CHECK_CHAR['\t'] = 1;
		CHECK_CHAR['\r'] = 1;
		CHECK_CHAR['\n'] = 1;
		CHECK_CHAR['.'] = 2;
		CHECK_CHAR['L'] = 3;
		CHECK_CHAR['l'] = 3;
		CHECK_CHAR['F'] = 3;
		CHECK_CHAR['f'] = 3;
		CHECK_CHAR['D'] = 3;
		CHECK_CHAR['d'] = 3;
	}

	/**
	 * 文字列内容が数値かチェック.
	 * 
	 * @param num
	 *            対象のオブジェクトを設定します.
	 * @return boolean [true]の場合、文字列内は数値が格納されています.
	 */
	public static final boolean isNumeric(Object num) {
		if (num == null) {
			return false;
		} else if (num instanceof Number) {
			return true;
		} else if (!(num instanceof String)) {
			num = num.toString();
		}
		char c;
		String s = (String) num;
		int i, start, end, flg, dot;
		start = flg = 0;
		dot = -1;
		end = s.length() - 1;

		for (i = start; i <= end; i++) {
			c = s.charAt(i);
			if (flg == 0 && CHECK_CHAR[c] != 1) {
				if (c == '-') {
					start = i + 1;
				} else {
					start = i;
				}
				flg = 1;
			} else if (flg == 1 && CHECK_CHAR[c] != 0) {
				if (c == '.') {
					if (dot != -1) {
						return false;
					}
					dot = i;
				} else {
					end = i - 1;
					break;
				}
			}
		}
		if (flg == 0) {
			return false;
		}
		if (start <= end) {
			for (i = start; i <= end; i++) {
				if (!((c = s.charAt(i)) == '.' || (c >= '0' && c <= '9'))) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * 対象文字列内容が小数点かチェック.
	 * 
	 * @param n
	 *            対象のオブジェクトを設定します.
	 * @return boolean [true]の場合は、数値内容です.
	 */
	public static final boolean isFloat(Object n) {
		if (Converter.isNumeric(n)) {
			if (n instanceof Float || n instanceof Double || n instanceof BigDecimal) {
				return true;
			} else if (n instanceof String) {
				return ((String) n).indexOf(".") != -1;
			}
			return n.toString().indexOf(".") != -1;
		}
		return false;
	}

	/**
	 * Boolean変換.
	 * 
	 * @param n
	 *            変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Boolean convertBool(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Boolean) {
			return (Boolean) o;
		}
		if (o instanceof Number) {
			return (((Number) o).intValue() == 0) ? false : true;
		}
		if (o instanceof String) {
			return Converter.parseBoolean((String) o);
		}
		throw new ConvertException("BOOL型変換に失敗しました[" + o + "]");
	}

	/**
	 * Integer変換.
	 * 
	 * @param n
	 *            変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Integer convertInt(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Integer) {
			return (Integer) o;
		} else if (o instanceof Number) {
			return ((Number) o).intValue();
		} else if (o instanceof String) {
			return Converter.parseInt((String) o);
		} else if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1 : 0;
		}
		throw new ConvertException("Int型変換に失敗しました[" + o + "]");
	}

	/**
	 * Long変換.
	 * 
	 * @param n
	 *            変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Long convertLong(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Long) {
			return (Long) o;
		} else if (o instanceof Number) {
			return ((Number) o).longValue();
		} else if (o instanceof String) {
			return Converter.parseLong((String) o);
		} else if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1L : 0L;
		}
		throw new ConvertException("Long型変換に失敗しました[" + o + "]");
	}

	/**
	 * Float変換.
	 * 
	 * @param n
	 *            変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Float convertFloat(final Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Float) {
			return (Float) o;
		} else if (o instanceof Number) {
			return ((Number) o).floatValue();
		} else if (o instanceof String && isNumeric(o)) {
			return parseFloat((String) o);
		} else if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1F : 0F;
		}
		throw new ConvertException("Float型変換に失敗しました[" + o + "]");
	}

	/**
	 * Double変換.
	 * 
	 * @param n
	 *            変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Double convertDouble(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Double) {
			return (Double) o;
		} else if (o instanceof Number) {
			return ((Number) o).doubleValue();
		} else if (o instanceof String) {
			return Converter.parseDouble((String) o);
		} else if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1D : 0D;
		}
		throw new ConvertException("Double型変換に失敗しました[" + o + "]");
	}

	/**
	 * 文字列変換.
	 * 
	 * @param n
	 *            変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final String convertString(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof String) {
			return (String) o;
		}
		return o.toString();
	}

	/** 日付のみ表現. **/
	private static final java.sql.Date _cDate(long d) {
		return _cDate(new java.util.Date(d));
	}

	@SuppressWarnings("deprecation")
	private static final java.sql.Date _cDate(java.util.Date n) {
		return new java.sql.Date(n.getYear(), n.getMonth(), n.getDate());
	}

	/**
	 * 日付変換.
	 * 
	 * @param n
	 *            変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final java.sql.Date convertSqlDate(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof java.util.Date) {
			return _cDate(((java.util.Date) o));
		} else if (o instanceof Long) {
			return _cDate((Long) o);
		} else if (o instanceof Number) {
			return _cDate(((Number) o).longValue());
		} else if (o instanceof String) {
			if (isNumeric(o)) {
				return _cDate(parseLong((String) o));
			}
			return DateConvert.getDate((String) o);
		}
		throw new ConvertException("java.sql.Date型変換に失敗しました[" + o + "]");
	}

	/** 時間のみ表現. **/
	private static final java.sql.Time _cTime(long d) {
		return _cTime(new java.util.Date(d));
	}

	@SuppressWarnings("deprecation")
	private static final java.sql.Time _cTime(java.util.Date n) {
		return new java.sql.Time(n.getHours(), n.getMinutes(), n.getSeconds());
	}

	/**
	 * 時間変換.
	 * 
	 * @param n
	 *            変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final java.sql.Time convertSqlTime(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof java.util.Date) {
			return _cTime((java.util.Date) o);
		} else if (o instanceof Long) {
			return _cTime((Long) o);
		} else if (o instanceof Number) {
			return _cTime(((Number) o).longValue());
		} else if (o instanceof String) {
			if (isNumeric(o)) {
				return _cTime(parseLong((String) o));
			}
			return DateConvert.getTime((String) o);
		}
		throw new ConvertException("java.sql.Time型変換に失敗しました[" + o + "]");
	}

	/**
	 * 日付時間変換.
	 * 
	 * @param n
	 *            変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final java.sql.Timestamp convertSqlTimestamp(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof java.util.Date) {
			if (o instanceof java.sql.Timestamp) {
				return (java.sql.Timestamp) o;
			}
			return new java.sql.Timestamp(((java.util.Date) o).getTime());
		} else if (o instanceof Long) {
			return new java.sql.Timestamp((Long) o);
		} else if (o instanceof Number) {
			return new java.sql.Timestamp(((Number) o).longValue());
		} else if (o instanceof String) {
			if (isNumeric(o)) {
				return new java.sql.Timestamp(parseLong((String) o));
			}
			return DateConvert.getTimestamp((String) o);
		}
		throw new ConvertException("java.sql.Timestamp型変換に失敗しました[" + o + "]");
	}

	/**
	 * 通常日付変換.
	 * 
	 * @param n
	 *            変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 * @exception Exception
	 *                例外.
	 */
	public static final java.util.Date convertDate(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof java.util.Date) {
			return (java.util.Date) o;
		} else if (o instanceof Long) {
			return new java.util.Date((Long) o);
		} else if (o instanceof Number) {
			return new java.util.Date(((Number) o).longValue());
		} else if (o instanceof String) {
			if (isNumeric(o)) {
				return new java.sql.Timestamp(parseLong((String) o));
			}
			return DateConvert.getTimestamp((String) o);
		}
		throw new ConvertException("java.util.Date型変換に失敗しました[" + o + "]");
	}

	/**
	 * 文字列から、Boolean型に変換.
	 * 
	 * @param s
	 *            対象の文字列を設定します.
	 * @return boolean Boolean型が返されます.
	 */
	public static final boolean parseBoolean(String s) {
		char c;
		int i, start, flg, len;

		start = flg = 0;
		len = s.length();

		for (i = start; i < len; i++) {
			c = s.charAt(i);
			if (flg == 0 && CHECK_CHAR[c] != 1) {
				start = i;
				flg = 1;
			} else if (flg == 1 && CHECK_CHAR[c] == 1) {
				len = i;
				break;
			}
		}
		if (flg == 0) {
			throw new ConvertException("Boolean変換に失敗しました:" + s);
		}

		if (isNumeric(s)) {
			return "0".equals(s) ? false : true;
		} else if (eqEng(s, start, (len -= start), "true") || eqEng(s, start, len, "t") || eqEng(s, start, len, "on")) {
			return true;
		} else if (eqEng(s, start, len, "false") || eqEng(s, start, len, "f") || eqEng(s, start, len, "off")) {
			return false;
		}
		throw new ConvertException("Boolean変換に失敗しました:" + s);
	}

	/**
	 * 文字列から、int型数値に変換.
	 * 
	 * @param num
	 *            対象の文字列を設定します.
	 * @return int int型で変換された数値が返されます.
	 */
	public static final int parseInt(final String num) {
		char c;
		boolean mFlg = false;
		int v, i, len, ret, end;

		ret = end = v = 0;
		len = num.length();

		for (i = end; i < len; i++) {
			c = num.charAt(i);
			if (v == 0 && CHECK_CHAR[c] != 1) {
				if (c == '-') {
					end = i + 1;
					mFlg = true;
				} else {
					end = i;
				}
				v = 1;
			} else if (v == 1 && CHECK_CHAR[c] != 0) {
				len = i;
				break;
			}
		}
		if (v == 0) {
			throw new ConvertException("Int数値変換に失敗しました:" + num);
		}

		v = 1;
		for (i = len - 1; i >= end; i--) {
			c = num.charAt(i);
			if (c >= '0' && c <= '9') {
				ret += (v * (c - '0'));
				v *= 10;
			} else {
				throw new ConvertException("Int数値変換に失敗しました:" + num);
			}
		}
		return mFlg ? ret * -1 : ret;
	}

	/**
	 * 文字列から、long型数値に変換.
	 * 
	 * @param num
	 *            対象の文字列を設定します.
	 * @return long long型で変換された数値が返されます.
	 */
	public static final long parseLong(final String num) {
		char c;
		boolean mFlg = false;
		long ret = 0L;
		int len, end, i, flg;

		end = flg = 0;
		len = num.length();

		for (i = end; i < len; i++) {
			c = num.charAt(i);
			if (flg == 0 && CHECK_CHAR[c] != 1) {
				if (c == '-') {
					end = i + 1;
					mFlg = true;
				} else {
					end = i;
				}
				flg = 1;
			} else if (flg == 1 && CHECK_CHAR[c] != 0) {
				len = i;
				break;
			}
		}
		if (flg == 0) {
			throw new ConvertException("Long数値変換に失敗しました:" + num);
		}

		long v = 1L;
		for (i = len - 1; i >= end; i--) {
			c = num.charAt(i);
			if (c >= '0' && c <= '9') {
				ret += (v * (long) (c - '0'));
				v *= 10L;
			} else {
				throw new ConvertException("Long数値変換に失敗しました:" + num);
			}
		}
		return mFlg ? ret * -1L : ret;
	}

	/**
	 * 文字列から、float型数値に変換.
	 * 
	 * @param num
	 *            対象の文字列を設定します.
	 * @return float float型で変換された数値が返されます.
	 */
	public static final float parseFloat(final String num) {
		char c;
		boolean mFlg = false;
		float ret = 0f;
		int end, len, flg, dot, i;

		end = flg = 0;
		dot = -1;
		len = num.length();

		for (i = end; i < len; i++) {
			c = num.charAt(i);
			if (flg == 0 && CHECK_CHAR[c] != 1) {
				if (c == '-') {
					end = i + 1;
					mFlg = true;
				} else {
					end = i;
				}
				flg = 1;
			} else if (flg == 1 && CHECK_CHAR[c] != 0) {
				if (c == '.') {
					if (dot != -1) {
						throw new ConvertException("Float数値変換に失敗しました:" + num);
					}
					dot = i;
				} else {
					len = i;
					break;
				}
			}
		}
		if (flg == 0) {
			throw new ConvertException("Float数値変換に失敗しました:" + num);
		}

		float v = 1f;
		if (dot == -1) {
			for (i = len - 1; i >= end; i--) {
				c = num.charAt(i);
				if (c >= '0' && c <= '9') {
					ret += (v * (float) (c - '0'));
					v *= 10f;
				} else {
					throw new ConvertException("Float数値変換に失敗しました:" + num);
				}
			}
			return mFlg ? ret * -1f : ret;
		} else {
			for (i = dot - 1; i >= end; i--) {
				c = num.charAt(i);
				if (c >= '0' && c <= '9') {
					ret += (v * (float) (c - '0'));
					v *= 10f;
				} else {
					throw new ConvertException("Float数値変換に失敗しました:" + num);
				}
			}
			float dret = 0f;
			v = 1f;
			for (i = len - 1; i > dot; i--) {
				c = num.charAt(i);
				if (c >= '0' && c <= '9') {
					dret += (v * (float) (c - '0'));
					v *= 10f;
				} else {
					throw new ConvertException("Float数値変換に失敗しました:" + num);
				}
			}
			return mFlg ? (ret + (dret / v)) * -1f : ret + (dret / v);
		}
	}

	/**
	 * 文字列から、double型数値に変換.
	 * 
	 * @param num
	 *            対象の文字列を設定します.
	 * @return double double型で変換された数値が返されます.
	 */
	public static final double parseDouble(final String num) {
		char c;
		boolean mFlg = false;
		double ret = 0d;
		int end, len, flg, dot, i;

		end = flg = 0;
		dot = -1;
		len = num.length();

		for (i = end; i < len; i++) {
			c = num.charAt(i);
			if (flg == 0 && CHECK_CHAR[c] != 1) {
				if (c == '-') {
					end = i + 1;
					mFlg = true;
				} else {
					end = i;
				}
				flg = 1;
			} else if (flg == 1 && CHECK_CHAR[c] != 0) {
				if (c == '.') {
					if (dot != -1) {
						throw new ConvertException("Double数値変換に失敗しました:" + num);
					}
					dot = i;
				} else {
					len = i;
					break;
				}
			}
		}
		if (flg == 0) {
			throw new ConvertException("Double数値変換に失敗しました:" + num);
		}

		double v = 1d;
		if (dot == -1) {
			for (i = len - 1; i >= end; i--) {
				c = num.charAt(i);
				if (c >= '0' && c <= '9') {
					ret += (v * (double) (c - '0'));
					v *= 10d;
				} else {
					throw new ConvertException("Double数値変換に失敗しました:" + num);
				}
			}
			return mFlg ? ret * -1d : ret;
		} else {
			for (i = dot - 1; i >= end; i--) {
				c = num.charAt(i);
				if (c >= '0' && c <= '9') {
					ret += (v * (double) (c - '0'));
					v *= 10d;
				} else {
					throw new ConvertException("Double数値変換に失敗しました:" + num);
				}
			}
			double dret = 0d;
			v = 1d;
			for (i = len - 1; i > dot; i--) {
				c = num.charAt(i);
				if (c >= '0' && c <= '9') {
					dret += (v * (double) (c - '0'));
					v *= 10d;
				} else {
					throw new ConvertException("Double数値変換に失敗しました:" + num);
				}
			}
			return mFlg ? (ret + (dret / v)) * -1d : ret + (dret / v);
		}
	}

	/**
	 * 文字情報の置き換え.
	 * 
	 * @param src
	 *            置き換え元の文字列を設定します.
	 * @param s
	 *            置き換え文字条件を設定します.
	 * @param d
	 *            置き換え先の文字条件を設定します.
	 * @return String 文字列が返却されます.
	 */
	public static final String changeString(String src, String s, String d) {
		return changeString(src, 0, src.length(), s, d);
	}

	/**
	 * 文字情報の置き換え.
	 * 
	 * @param src
	 *            置き換え元の文字列を設定します.
	 * @param off
	 *            置き換え元文字のオフセット値を設定します.
	 * @param len
	 *            置き換え元文字の長さを設定します.
	 * @param s
	 *            置き換え文字条件を設定します.
	 * @param d
	 *            置き換え先の文字条件を設定します.
	 * @return String 文字列が返却されます.
	 */
	public static final String changeString(String src, int off, int len, String s, String d) {
		int j, k;
		char t = s.charAt(0);
		int lenS = s.length();
		StringBuilder buf = new StringBuilder(len);
		for (int i = off; i < len; i++) {
			if (src.charAt(i) == t) {
				j = i;
				k = 0;
				while (++k < lenS && ++j < len && src.charAt(j) == s.charAt(k))
					;
				if (k >= lenS) {
					buf.append(d);
					i += (lenS - 1);
				} else {
					buf.append(t);
				}
			} else {
				buf.append(src.charAt(i));
			}
		}
		return buf.toString();
	}

	/**
	 * 対象文字列が存在するかチェック.
	 * 
	 * @param v
	 *            対象の情報を設定します.
	 * @return boolean [true]の場合、文字列が存在します.
	 */
	@SuppressWarnings("rawtypes")
	public static final boolean useString(Object v) {
		if (v == null) {
			return false;
		}
		if (v instanceof CharSequence) {
			CharSequence cs = (CharSequence) v;
			if (cs.length() > 0) {
				int len = cs.length();
				for (int i = 0; i < len; i++) {
					if (CHECK_CHAR[cs.charAt(i)] == 1) {
						continue;
					}
					return true;
				}
			}
			return false;
		} else if (v instanceof Collection) {
			return !((Collection) v).isEmpty();
		}
		return true;
	}

	/**
	 * 前後のスペース等を取り除く.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @return String 文字列が返されます.
	 */
	public static final String trim(String string) {
		int s = -1;
		int e = -1;
		int len = string.length();
		boolean sFlg = false;
		for (int i = 0; i < len; i++) {
			char c = string.charAt(i);
			if (c != ' ' && c != '　' && c != '\r' && c != '\n' && c != '\t') {
				s = i;
				break;
			}
			sFlg = true;
		}
		if (sFlg && s == -1) {
			return "";
		}
		boolean eFlg = false;
		for (int i = len - 1; i >= 0; i--) {
			char c = string.charAt(i);
			if (c != ' ' && c != '　' && c != '\r' && c != '\n' && c != '\t') {
				e = i;
				break;
			}
			eFlg = true;
		}
		if (sFlg == true && eFlg == true) {
			return string.substring(s, e + 1);
		} else if (sFlg == true) {
			return string.substring(s);
		} else if (eFlg == true) {
			return string.substring(0, e + 1);
		}
		return string;
	}

	/**
	 * URLデコード.
	 * 
	 * @param info
	 *            変換対象の条件を設定します.
	 * @param charset
	 *            対象のキャラクタセットを設定します.
	 * @return 変換された情報が返されます.
	 * @exception IOException
	 *                例外.
	 */
	public static final String urlDecode(String info, String charset) throws IOException {
		int len;
		if (info == null || (len = info.length()) <= 0) {
			return "";
		}
		if (charset == null || charset.length() <= 0) {
			charset = "utf-8";
		}
		char c;
		byte[] bin = new byte[len];
		int j = 0;
		for (int i = 0; i < len; i++) {
			c = info.charAt(i);
			if (c == '%') {
				bin[j] = (byte) ((hexChar(info.charAt(i + 1)) << 4) | hexChar(info.charAt(i + 2)));
				i += 2;
				j++;
			} else if (c == '+') {
				bin[j] = (byte) ' ';
				j++;
			} else {
				bin[j] = (byte) c;
				j++;
			}
		}
		return new String(bin, 0, j, charset);
	}

	/**
	 * URLエンコード.
	 * 
	 * @param info
	 *            変換対象の条件を設定します.
	 * @param charset
	 *            対象のキャラクタセットを設定します.
	 * @return 変換された情報が返されます.
	 * @exception IOException
	 *                例外.
	 */
	public static final String urlEncode(String info, String charset) throws IOException {
		int len;
		if (info == null || (len = info.length()) <= 0) {
			return "";
		}
		if (charset == null || charset.length() <= 0) {
			charset = "utf-8";
		}
		CharsetEncoder cEnd = null;
		CharBuffer cBuf = null;
		ByteBuffer bBuf = null;
		StringBuilder buf = new StringBuilder(len << 1);
		int n, j;
		char c;
		for (int i = 0; i < len; i++) {
			c = info.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '.') || (c == '-')
					|| (c == '_')) {
				buf.append(c);
			} else if (c == ' ') {
				// buf.append( "+" ) ;
				buf.append("%20");
			} else {
				if (cEnd == null) {
					cEnd = Charset.forName(charset).newEncoder();
					cBuf = CharBuffer.allocate(1);
					bBuf = ByteBuffer.allocate(4);
				} else {
					bBuf.clear();
					cBuf.clear();
				}
				cBuf.put(c);
				cBuf.flip();
				cEnd.encode(cBuf, bBuf, true);
				n = bBuf.position();
				for (j = 0; j < n; j++) {
					buf.append("%");
					toHex(buf, bBuf.get(j));
				}
			}
		}
		return buf.toString();
	}

	/** HexChar変換. **/
	private static final int hexChar(char c) throws IOException {
		if (c >= '0' && c <= '9') {
			return ((int) (c - '0') & 0x0000000f);
		} else if (c >= 'A' && c <= 'F') {
			return ((int) (c - 'A') & 0x0000000f) + 10;
		} else if (c >= 'a' && c <= 'f') {
			return ((int) (c - 'a') & 0x0000000f) + 10;
		}
		throw new IOException("16進[" + c + "]数値ではありません");
	}

	/** Hex文字列変換. **/
	private static final void toHex(StringBuilder buf, byte b) {
		switch ((b & 0x000000f0) >> 4) {
		case 0:
			buf.append("0");
			break;
		case 1:
			buf.append("1");
			break;
		case 2:
			buf.append("2");
			break;
		case 3:
			buf.append("3");
			break;
		case 4:
			buf.append("4");
			break;
		case 5:
			buf.append("5");
			break;
		case 6:
			buf.append("6");
			break;
		case 7:
			buf.append("7");
			break;
		case 8:
			buf.append("8");
			break;
		case 9:
			buf.append("9");
			break;
		case 10:
			buf.append("A");
			break;
		case 11:
			buf.append("B");
			break;
		case 12:
			buf.append("C");
			break;
		case 13:
			buf.append("D");
			break;
		case 14:
			buf.append("E");
			break;
		case 15:
			buf.append("F");
			break;
		}
		switch (b & 0x0000000f) {
		case 0:
			buf.append("0");
			break;
		case 1:
			buf.append("1");
			break;
		case 2:
			buf.append("2");
			break;
		case 3:
			buf.append("3");
			break;
		case 4:
			buf.append("4");
			break;
		case 5:
			buf.append("5");
			break;
		case 6:
			buf.append("6");
			break;
		case 7:
			buf.append("7");
			break;
		case 8:
			buf.append("8");
			break;
		case 9:
			buf.append("9");
			break;
		case 10:
			buf.append("A");
			break;
		case 11:
			buf.append("B");
			break;
		case 12:
			buf.append("C");
			break;
		case 13:
			buf.append("D");
			break;
		case 14:
			buf.append("E");
			break;
		case 15:
			buf.append("F");
			break;
		}
	}

	/**
	 * 文字列１６進数を数値変換.
	 * 
	 * @param s
	 *            対象の文字列を設定します.
	 * @return int 変換された数値が返されます.
	 */
	public static final int parseHexInt(String s) throws IOException {
		int len = s.length();
		int ret = 0;
		int n = 0;
		char[] mM = Alphabet._mM;
		for (int i = len - 1; i >= 0; i--) {
			switch (mM[s.charAt(i)]) {
			case '0':
				break;
			case '1':
				ret |= 1 << n;
				break;
			case '2':
				ret |= 2 << n;
				break;
			case '3':
				ret |= 3 << n;
				break;
			case '4':
				ret |= 4 << n;
				break;
			case '5':
				ret |= 5 << n;
				break;
			case '6':
				ret |= 6 << n;
				break;
			case '7':
				ret |= 7 << n;
				break;
			case '8':
				ret |= 8 << n;
				break;
			case '9':
				ret |= 9 << n;
				break;
			case 'a':
				ret |= 10 << n;
				break;
			case 'b':
				ret |= 11 << n;
				break;
			case 'c':
				ret |= 12 << n;
				break;
			case 'd':
				ret |= 13 << n;
				break;
			case 'e':
				ret |= 14 << n;
				break;
			case 'f':
				ret |= 15 << n;
				break;
			default:
				throw new IOException("16進文字列に不正な文字列を検知:" + s);
			}
			n += 4;
		}
		return ret;
	}

	/** HTTPタイムスタンプ条件を生成. **/
	private static final String[] _TIMESTAMP_TO_WEEK;
	private static final String[] _TIMESTAMP_TO_MONTH;
	static {
		String[] n = new String[9];
		n[1] = "Sun, ";
		n[2] = "Mon, ";
		n[3] = "Tue, ";
		n[4] = "Wed, ";
		n[5] = "Thu, ";
		n[6] = "Fri, ";
		n[7] = "Sat, ";

		String[] nn = new String[12];
		nn[0] = "Jan";
		nn[1] = "Feb";
		nn[2] = "Mar";
		nn[3] = "Apr";
		nn[4] = "May";
		nn[5] = "Jun";
		nn[6] = "Jul";
		nn[7] = "Aug";
		nn[8] = "Sep";
		nn[9] = "Oct";
		nn[10] = "Nov";
		nn[11] = "Dec";

		_TIMESTAMP_TO_WEEK = n;
		_TIMESTAMP_TO_MONTH = nn;
	}

	/**
	 * HTTPタイムスタンプを取得.
	 * 
	 * @param mode
	 *            [true]の場合、ハイフン区切りの条件で出力します.
	 * @param date
	 *            出力対象の日付オブジェクトを設定します.
	 * @return String タイムスタンプ値が返却されます.
	 */
	public static final String createTimestamp(boolean mode, java.util.Date date) {
		StringBuilder buf = new StringBuilder();
		try {
			String tmp;
			Calendar cal = new GregorianCalendar(DateConvert.GMT_TIMEZONE);
			cal.setTime(date);
			buf.append(_TIMESTAMP_TO_WEEK[cal.get(Calendar.DAY_OF_WEEK)]);
			tmp = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (mode) {
				buf.append("00".substring(tmp.length())).append(tmp).append("-");
				buf.append(_TIMESTAMP_TO_MONTH[cal.get(Calendar.MONTH)]).append("-");
			} else {
				buf.append("00".substring(tmp.length())).append(tmp).append(" ");
				buf.append(_TIMESTAMP_TO_MONTH[cal.get(Calendar.MONTH)]).append(" ");
			}
			tmp = String.valueOf(cal.get(Calendar.YEAR));
			buf.append("0000".substring(tmp.length())).append(tmp).append(" ");
			tmp = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
			buf.append("00".substring(tmp.length())).append(tmp).append(":");

			tmp = String.valueOf(cal.get(Calendar.MINUTE));
			buf.append("00".substring(tmp.length())).append(tmp).append(":");

			tmp = String.valueOf(cal.get(Calendar.SECOND));
			buf.append("00".substring(tmp.length())).append(tmp).append(" ");
			buf.append("GMT");
		} catch (Exception e) {
			throw new ConvertException("タイムスタンプ生成に失敗", e);
		}
		return buf.toString();
	}

	/**
	 * HTTPタイムスタンプを取得.
	 * 
	 * @param buf
	 *            出力先のStringBuilderを設定します.
	 * @param mode
	 *            [true]の場合、ハイフン区切りの条件で出力します.
	 * @param date
	 *            出力対象の日付オブジェクトを設定します.
	 */
	public static final void createTimestamp(StringBuilder buf, boolean mode, java.util.Date date) {
		try {
			String tmp;
			Calendar cal = new GregorianCalendar(DateConvert.GMT_TIMEZONE);
			cal.setTime(date);
			buf.append(_TIMESTAMP_TO_WEEK[cal.get(Calendar.DAY_OF_WEEK)]);
			tmp = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (mode) {
				buf.append("00".substring(tmp.length())).append(tmp).append("-");
				buf.append(_TIMESTAMP_TO_MONTH[cal.get(Calendar.MONTH)]).append("-");
			} else {
				buf.append("00".substring(tmp.length())).append(tmp).append(" ");
				buf.append(_TIMESTAMP_TO_MONTH[cal.get(Calendar.MONTH)]).append(" ");
			}
			tmp = String.valueOf(cal.get(Calendar.YEAR));
			buf.append("0000".substring(tmp.length())).append(tmp).append(" ");
			tmp = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
			buf.append("00".substring(tmp.length())).append(tmp).append(":");

			tmp = String.valueOf(cal.get(Calendar.MINUTE));
			buf.append("00".substring(tmp.length())).append(tmp).append(":");

			tmp = String.valueOf(cal.get(Calendar.SECOND));
			buf.append("00".substring(tmp.length())).append(tmp).append(" ");
			buf.append("GMT");
		} catch (Exception e) {
			throw new ConvertException("タイムスタンプ生成に失敗", e);
		}
	}

	/**
	 * HTMLタイムスタンプを時間変換.
	 * 
	 * @param timestamp
	 *            変換対象のHTMLタイムスタンプを設定します.
	 * @return Date 変換された時間が返されます.
	 * @exception Exception
	 *                例外.
	 */
	public static final Date convertTimestamp(String timestamp) throws Exception {
		return DateConvert.getWebTimestamp(timestamp);
	}

	/**
	 * 英字の大文字小文字を区別せずにチェック.
	 * 
	 * @param src
	 *            比較元文字を設定します.
	 * @param dest
	 *            比較先文字を設定します.
	 * @return boolean [true]の場合、一致します.
	 */
	public static final boolean eqEng(String src, String dest) {
		return Alphabet.eq(src, dest);
	}

	/**
	 * 英字の大文字小文字を区別せずにチェック.
	 * 
	 * @param src
	 *            比較元文字を設定します.
	 * @param off
	 *            srcのオフセット値を設定します.
	 * @param len
	 *            srcのlength値を設定します.
	 * @param dest
	 *            比較先文字を設定します.
	 * @return boolean [true]の場合、一致します.
	 */
	public static final boolean eqEng(String src, int off, int len, String dest) {
		return Alphabet.eq(src, off, len, dest);
	}

	/**
	 * 英字の大文字小文字を区別しない、バイトチェック.
	 * 
	 * @param s
	 *            比較の文字を設定します.
	 * @param d
	 *            比較の文字を設定します.
	 * @return boolean [true]の場合、一致します.
	 */
	public static final boolean oneEng(char s, char d) {
		return Alphabet.oneEq(s, d);
	}

	/**
	 * 英字の大文字小文字を区別しない、文字indexOf.
	 * 
	 * @param buf
	 *            設定対象の文字情報を設定します.
	 * @param chk
	 *            チェック対象の文字情報を設定します.
	 * @param off
	 *            設定対象のオフセット値を設定します.
	 * @return int マッチする位置が返却されます. [-1]の場合は情報は存在しません.
	 */
	public static final int indexOfEng(final String buf, final String chk) {
		return Alphabet.indexOf(buf, chk, 0);
	}

	/**
	 * 英字の大文字小文字を区別しない、文字indexOf.
	 * 
	 * @param buf
	 *            設定対象の文字情報を設定します.
	 * @param chk
	 *            チェック対象の文字情報を設定します.
	 * @param off
	 *            設定対象のオフセット値を設定します.
	 * @return int マッチする位置が返却されます. [-1]の場合は情報は存在しません.
	 */
	public static final int indexOfEng(final String buf, final String chk, final int off) {
		return Alphabet.indexOf(buf, chk, off);
	}

	/**
	 * 比較処理.
	 * 
	 * @param s
	 *            比較の文字を設定します.
	 * @param d
	 *            比較の文字を設定します.
	 * @return int 数字が返却されます. [マイナス]の場合、sのほうが小さい. [プラス]の場合は、sのほうが大きい.
	 *         [0]の場合は、sとdは同一.
	 */
	public static final int compareToEng(String s, String d) {
		return Alphabet.compareTo(s, d);
	}

	/**
	 * 小文字変換.
	 * 
	 * @param s
	 *            対象の文字列を設定します.
	 * @return String 小文字変換された情報が返却されます.
	 */
	public static final String toLowerCase(String s) {
		return Alphabet.toLowerCase(s);
	}

	/**
	 * 指定条件をList変換.
	 * 
	 * @param list
	 *            対象の要素群を設定します.
	 * @return List<Object> List要素が返却されます.
	 */
	public static final List<Object> toList(Object... list) {
		List<Object> ret = new ArrayList<Object>();
		int len = list.length;
		for (int i = 0; i < len; i++) {
			ret.add(list[i]);
		}
		return ret;
	}

	/**
	 * 指定条件をMap変換.
	 * 
	 * @parma list 対象の要素群を設定します.
	 * @return Map<String,Object> Map要素が返却されます.
	 * @exception Exception
	 *                例外.
	 */
	public static final Map<String, Object> toMap(Object... list) throws Exception {
		Map<String, Object> ret = new HashMap<String, Object>();
		int len = list.length;
		for (int i = 0; i < len; i += 2) {
			ret.put(convertString(list[i]), list[i + 1]);
		}
		return ret;
	}

	/**
	 * 文字列をASCII型に変換. 単純にStringのchar[]をbyte[]に変換.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @return byte[] ASCII型で変換された文字列が返却されます.
	 */
	public static final byte[] toAsciiBytes(final String string) {
		return toAsciiBytes(string, 0, string.length());
	}

	/**
	 * 文字列をASCII型に変換. 単純にStringのchar[]をbyte[]に変換.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @param off
	 *            対象のオフセット値を設定します.
	 * @param len
	 *            対象の長さを設定します.
	 * @return byte[] ASCII型で変換された文字列が返却されます.
	 */
	public static final byte[] toAsciiBytes(final String string, final int off, final int len) {
		final byte[] ret = new byte[len];
		for (int i = off; i < len; i++) {
			ret[i] = (byte) (string.charAt(i) & 0x00ff);
		}
		return ret;
	}

	// ゼロサプレス.
	private static final void _z2(StringBuilder buf, String no) {
		buf.append("00".substring(no.length())).append(no);
	}

	/**
	 * 16バイトデータ(4バイト配列４つ)をUUIDに変換.
	 * 
	 * @param n
	 *            int[4] のデータを設定します.
	 * @return
	 */
	public static final String byte16ToUUID(int[] n) {
		return byte16ToUUID(n[0], n[1], n[2], n[3]);
	}

	/**
	 * 16バイトデータ(4バイト配列４つ)をUUIDに変換.
	 * 
	 * @param a
	 *            4バイトデータを設定します.
	 * @param b
	 *            4バイトデータを設定します.
	 * @param c
	 *            4バイトデータを設定します.
	 * @param d
	 *            4バイトデータを設定します.
	 * @return String uuidが返却されます.
	 */
	public static final String byte16ToUUID(int a, int b, int c, int d) {
		final StringBuilder buf = new StringBuilder();
		_z2(buf, Integer.toHexString(((a & 0xff000000) >> 24) & 0x00ff));
		_z2(buf, Integer.toHexString((a & 0x00ff0000) >> 16));
		_z2(buf, Integer.toHexString((a & 0x0000ff00) >> 8));
		_z2(buf, Integer.toHexString(a & 0x000000ff));
		buf.append("-");
		_z2(buf, Integer.toHexString(((b & 0xff000000) >> 24) & 0x00ff));
		_z2(buf, Integer.toHexString((b & 0x00ff0000) >> 16));
		buf.append("-");
		_z2(buf, Integer.toHexString((b & 0x0000ff00) >> 8));
		_z2(buf, Integer.toHexString(b & 0x000000ff));
		buf.append("-");
		_z2(buf, Integer.toHexString(((c & 0xff000000) >> 24) & 0x00ff));
		_z2(buf, Integer.toHexString((c & 0x00ff0000) >> 16));
		buf.append("-");
		_z2(buf, Integer.toHexString((c & 0x0000ff00) >> 8));
		_z2(buf, Integer.toHexString(c & 0x000000ff));
		_z2(buf, Integer.toHexString(((d & 0xff000000) >> 24) & 0x00ff));
		_z2(buf, Integer.toHexString((d & 0x00ff0000) >> 16));
		_z2(buf, Integer.toHexString((d & 0x0000ff00) >> 8));
		_z2(buf, Integer.toHexString(d & 0x000000ff));
		return buf.toString();
	}

	/**
	 * UUIDを16バイトデータ(4バイト配列４つ)に変換.
	 * 
	 * @param n
	 *            uuidを設定します.
	 * @return int[] int[4]が返却されます.
	 */
	public static final int[] uuidToByte16(String n) {
		return new int[] { Integer.parseInt(n.substring(0, 8), 16),
				Integer.parseInt(n.substring(9, 13) + n.substring(14, 18), 16),
				Integer.parseInt(n.substring(19, 23) + n.substring(24, 28), 16),
				Integer.parseInt(n.substring(28), 16) };
	}

	/**
	 * チェック情報単位で情報を区切ります.
	 * 
	 * @param out
	 *            区切られた情報が格納されます.
	 * @param mode
	 *            区切られた時の文字列が無い場合に、無視するかチェックします. [true]の場合は、無視しません.
	 *            [false]の場合は、無視します.
	 * @param str
	 *            区切り対象の情報を設置します.
	 * @param check
	 *            区切り対象の文字情報をセットします. 区切り対象文字を複数設定する事により、それらに対応した区切りとなります.
	 */
	public static final void cutString(List<String> out, boolean mode, String str, String check) {
		int i, j;
		int len;
		int lenJ;
		int s = -1;
		char strCode;
		char[] checkCode = null;
		String tmp = null;
		if (out == null || str == null || (len = str.length()) <= 0 || check == null || check.length() <= 0) {
			throw new IllegalArgumentException();
		}
		out.clear();
		lenJ = check.length();
		checkCode = new char[lenJ];
		check.getChars(0, lenJ, checkCode, 0);
		if (lenJ == 1) {
			for (i = 0, s = -1; i < len; i++) {
				strCode = str.charAt(i);
				s = (s == -1) ? i : s;
				if (strCode == checkCode[0]) {
					if (s < i) {
						tmp = str.substring(s, i);
						out.add(tmp);
						tmp = null;
						s = -1;
					} else if (mode == true) {
						out.add("");
						s = -1;
					} else {
						s = -1;
					}
				}
			}
		} else {
			for (i = 0, s = -1; i < len; i++) {
				strCode = str.charAt(i);
				s = (s == -1) ? i : s;
				for (j = 0; j < lenJ; j++) {
					if (strCode == checkCode[j]) {
						if (s < i) {
							tmp = str.substring(s, i);
							out.add(tmp);
							tmp = null;
							s = -1;
						} else if (mode == true) {
							out.add("");
							s = -1;
						} else {
							s = -1;
						}
						break;
					}
				}
			}
		}
		if (s != -1) {
			tmp = str.substring(s, len);
			out.add(tmp);
			tmp = null;
		}
		checkCode = null;
		tmp = null;
	}

	/**
	 * チェック情報単位で情報を区切ります。
	 * 
	 * @param out
	 *            区切られた情報が格納されます.
	 * @param cote
	 *            コーテーション対応であるか設定します. [true]を設定した場合、各コーテーション ( ",' ) で囲った情報内は
	 *            区切り文字と判別しません. [false]を設定した場合、コーテーション対応を行いません.
	 * @param coteFlg
	 *            コーテーションが入っている場合に、コーテーションを範囲に含むか否かを 設定します.
	 *            [true]を設定した場合、コーテーション情報も範囲に含みます. [false]を設定した場合、コーテーション情報を範囲としません.
	 * @param str
	 *            区切り対象の情報を設置します.
	 * @param check
	 *            区切り対象の文字情報をセットします. 区切り対象文字を複数設定する事により、それらに対応した区切りとなります.
	 */
	public static final void cutString(List<String> out, boolean cote, boolean coteFlg, String str, String check) {
		int i, j;
		int len;
		int lenJ;
		int s = -1;
		char coteChr;
		char nowChr;
		char strCode;
		char[] checkCode = null;
		String tmp = null;
		if (cote == false) {
			cutString(out, false, str, check);
		} else {
			if (out == null || str == null || (len = str.length()) <= 0 || check == null || check.length() <= 0) {
				throw new IllegalArgumentException();
			}
			out.clear();
			lenJ = check.length();
			checkCode = new char[lenJ];
			check.getChars(0, lenJ, checkCode, 0);
			if (lenJ == 1) {
				int befCode = -1;
				boolean yenFlag = false;
				for (i = 0, s = -1, coteChr = 0; i < len; i++) {
					strCode = str.charAt(i);
					nowChr = strCode;
					s = (s == -1) ? i : s;
					if (coteChr == 0) {
						if (nowChr == '\'' || nowChr == '\"') {
							coteChr = nowChr;
							if (s < i) {
								tmp = str.substring(s, i);
								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						} else if (strCode == checkCode[0]) {
							if (s < i) {
								tmp = str.substring(s, i);
								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						}
					} else {
						if (befCode != '\\' && coteChr == nowChr) {
							yenFlag = false;
							coteChr = 0;
							if (s == i && coteFlg == true) {
								out.add(new StringBuilder().append(strCode).append(strCode).toString());
								s = -1;
							} else if (s < i) {
								if (coteFlg == true) {
									tmp = str.substring(s - 1, i + 1);
								} else {
									tmp = str.substring(s, i);
								}
								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						} else if (strCode == '\\' && befCode == '\\') {
							yenFlag = true;
						} else {
							yenFlag = false;
						}
					}
					if (yenFlag) {
						yenFlag = false;
						befCode = -1;
					} else {
						befCode = strCode;
					}
				}
			} else {
				int befCode = -1;
				boolean yenFlag = false;
				for (i = 0, s = -1, coteChr = 0; i < len; i++) {
					strCode = str.charAt(i);
					nowChr = strCode;
					s = (s == -1) ? i : s;
					if (coteChr == 0) {
						if (nowChr == '\'' || nowChr == '\"') {
							coteChr = nowChr;
							if (s < i) {
								tmp = str.substring(s, i);
								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						} else {
							for (j = 0; j < lenJ; j++) {
								if (strCode == checkCode[j]) {
									if (s < i) {
										tmp = str.substring(s, i);
										out.add(tmp);
										tmp = null;
										s = -1;
									} else {
										s = -1;
									}
									break;
								}
							}
						}
					} else {
						if (befCode != '\\' && coteChr == nowChr) {
							coteChr = 0;
							yenFlag = false;
							if (s == i && coteFlg == true) {
								out.add(new StringBuilder().append(strCode).append(strCode).toString());
								s = -1;
							} else if (s < i) {
								if (coteFlg == true) {
									tmp = str.substring(s - 1, i + 1);
								} else {
									tmp = str.substring(s, i);
								}

								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						} else if (strCode == '\\' && befCode == '\\') {
							yenFlag = true;
						} else {
							yenFlag = false;
						}
					}
					if (yenFlag) {
						yenFlag = false;
						befCode = -1;
					} else {
						befCode = strCode;
					}
				}
			}
			if (s != -1) {
				if (coteChr != 0 && coteFlg == true) {
					tmp = str.substring(s - 1, len) + (char) coteChr;
				} else {
					tmp = str.substring(s, len);
				}
				out.add(tmp);
				tmp = null;
			}
			checkCode = null;
			tmp = null;
		}
	}
	
	/**
	 * コメント除去.
	 * @param sql
	 * @return
	 */
	public static final String cutComment(String sql) {
		if (sql == null || sql.length() <= 0) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		int len = sql.length();
		int cote = -1;
		int commentType = -1;
		int bef = -1;
		char c, c2;
		for (int i = 0; i < len; i++) {
			if (i != 0) {
				bef = sql.charAt(i - 1);
			}
			c = sql.charAt(i);
			// コメント内の処理.
			if (commentType != -1) {
				switch (commentType) {
				case 1: // １行コメント.
					if (c == '\n') {
						buf.append(c);
						commentType = -1;
					}
					break;
				case 2: // 複数行コメント.
					if (c == '\n') {
						buf.append(c);
					} else if (len > i + 1 && c == '*' && sql.charAt(i + 1) == '/') {
						i++;
						commentType = -1;
					}
					break;
				}
				continue;
			}
			// シングル／ダブルコーテーション内の処理.
			if (cote != -1) {
				if (c == cote && (char) bef != '\\') {
					cote = -1;
				}
				buf.append(c);
				continue;
			}
			// コメント(// or /* ... */).
			if (c == '/') {
				if (len <= i + 1) {
					buf.append(c);
					continue;
				}
				c2 = sql.charAt(i + 1);
				if (c2 == '*') {
					commentType = 2;
					continue;
				} else if (c2 == '/') {
					commentType = 1;
					continue;
				}
			}
			// コメント(--)
			else if (c == '-') {
				if (len <= i + 1) {
					buf.append(c);
					continue;
				}
				c2 = sql.charAt(i + 1);
				if (c2 == '-') {
					commentType = 1;
					continue;
				}
			}
			// コメント(#)
			else if (c == '#') {
				if (len <= i + 1) {
					buf.append(c);
					continue;
				}
				commentType = 1;
				continue;
			}
			// コーテーション開始.
			else if ((c == '\'' || c == '\"') && (char) bef != '\\') {
				cote = (int) (c & 0x0000ffff);
			}
			buf.append(c);
		}
		return buf.toString();
	}
}
