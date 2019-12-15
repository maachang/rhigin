package rhigin.scripts;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import rhigin.RhiginException;
import rhigin.util.ArrayMap;
import rhigin.util.Converter;
import rhigin.util.DateConvert;
import rhigin.util.ObjectList;

/**
 * Json変換処理. Rhino専用に改造.
 */
@SuppressWarnings("rawtypes")
public final class Json {
	protected Json() {
	}

	private static final int TYPE_ARRAY = 0;
	private static final int TYPE_MAP = 1;

	/**
	 * JSON変換.
	 * 
	 * @param target 対象のターゲットオブジェクトを設定します.
	 * @return String 変換されたJSON情報が返されます.
	 */
	public static final String encode(Object target) {
		StringBuilder buf = new StringBuilder();
		_encode(buf, target, target);
		return buf.toString();
	}

	/**
	 * JSON形式から、オブジェクト変換2.
	 * 
	 * @param json 対象のJSON情報を設定します.
	 * @return Object 変換されたJSON情報が返されます.
	 */
	public static final Object decode(String json) {
		if (json == null) {
			return null;
		}
		List<Object> list;
		int[] n = new int[1];
		while (true) {
			// token解析が必要な場合.
			if (json.startsWith("[") || json.startsWith("{")) {
				// JSON形式をToken化.
				list = analysisJsonToken(json);
				// Token解析処理.
				if ("[".equals(list.get(0))) {
					// List解析.
					return createJsonInfo(n, list, TYPE_ARRAY, 0, list.size());
				} else {
					// Map解析.
					return createJsonInfo(n, list, TYPE_MAP, 0, list.size());
				}
			} else if (json.startsWith("(") && json.endsWith(")")) {
				json = json.substring(1, json.length() - 1).trim();
				continue;
			}
			break;
		}
		return decJsonValue(n, 0, json);
	}

	/** [encodeJSON]jsonコンバート. **/
	private static final void _encode(StringBuilder buf, Object base, Object target) {
		// null or undefined の場合.
		if (target == null || target instanceof Undefined) {
			// nullで表現.
			buf.append("null");
			return;
		}
		// rhinoのjavaオブジェクトwrapper対応.
		if (target instanceof Wrapper) {
			target = ((Wrapper) target).unwrap();
		}
		if (target instanceof Map) {
			encodeJsonMap(buf, base, (Map) target);
		} else if (target instanceof List) {
			encodeJsonList(buf, base, (List) target);
		} else if (target instanceof Number || target instanceof Boolean) {
			buf.append(target);
		} else if (target instanceof Character || target instanceof CharSequence) {
			buf.append("\"").append(target).append("\"");
		} else if (target instanceof byte[]) {
			buf.append("null");
		} else if (target instanceof char[]) {
			buf.append("\"").append(new String((char[]) target)).append("\"");
		} else if (target instanceof java.util.Date) {
			buf.append("\"").append(dateToString((java.util.Date) target)).append("\"");
		} else if (target.getClass().isArray()) {
			if (Array.getLength(target) == 0) {
				buf.append("[]");
			} else {
				encodeJsonArray(buf, base, target);
			}
		} else if (target instanceof IdScriptableObject) {
			final java.util.Date d = getJSDate((IdScriptableObject) target);
			if (d == null) {
				buf.append("null");
			} else {
				buf.append("\"").append(dateToString(d)).append("\"");
			}
		} else {
			buf.append("\"").append(target.toString()).append("\"");
		}
	}

	/** [encodeJSON]jsonMapコンバート. **/
	private static final void encodeJsonMap(StringBuilder buf, Object base, Map map) {
		boolean flg = false;
		Map mp = (Map) map;
		Iterator it = mp.keySet().iterator();
		buf.append("{");
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = mp.get(key);
			if (base == value) {
				continue;
			}
			if (flg) {
				buf.append(",");
			}
			flg = true;
			buf.append("\"").append(key).append("\":");
			_encode(buf, base, value);
		}
		buf.append("}");
	}

	/** [encodeJSON]jsonListコンバート. **/
	private static final void encodeJsonList(StringBuilder buf, Object base, List list) {
		boolean flg = false;
		List lst = (List) list;
		buf.append("[");
		int len = lst.size();
		for (int i = 0; i < len; i++) {
			Object value = lst.get(i);
			if (base == value) {
				continue;
			}
			if (flg) {
				buf.append(",");
			}
			flg = true;
			_encode(buf, base, value);
		}
		buf.append("]");
	}

	/** [encodeJSON]json配列コンバート. **/
	private static final void encodeJsonArray(StringBuilder buf, Object base, Object list) {
		boolean flg = false;
		int len = Array.getLength(list);
		buf.append("[");
		for (int i = 0; i < len; i++) {
			Object value = Array.get(list, i);
			if (base == value) {
				continue;
			}
			if (flg) {
				buf.append(",");
			}
			flg = true;
			_encode(buf, base, value);
		}
		buf.append("]");
	}

	/** [decodeJSON]１つの要素を変換. **/
	private static final Object decJsonValue(int[] n, int no, String json) {
		int len;
		if ((len = json.length()) <= 0) {
			return json;
		}
		// NULL文字.
		if ("null".equals(json)) {
			return null;
		}
		// BOOLEAN true.
		else if ("true".equals(json)) {
			return Boolean.TRUE;
		}
		// BOOLEAN false.
		else if ("false".equals(json)) {
			return Boolean.FALSE;
		}
		// 数値.
		if (isNumeric(json)) {
			if (json.indexOf(".") != -1) {
				return Double.parseDouble(json);
			}
			return Long.parseLong(json);
		}
		// 文字列コーテーション区切り.
		if ((json.startsWith("\"") && json.endsWith("\"")) || (json.startsWith("\'") && json.endsWith("\'"))) {
			json = json.substring(1, len - 1);
		}
		// ISO8601の日付フォーマットかチェック.
		if (DateConvert.isISO8601(json)) {
			return stringToDate(json);
		}
		return json;
	}

	/** JSON_Token_解析処理 **/
	private static final List<Object> analysisJsonToken(String json) {
		int s = -1;
		char c;
		int cote = -1;
		int bef = -1;
		int len = json.length();
		List<Object> ret = new ObjectList<Object>();
		// Token解析.
		for (int i = 0; i < len; i++) {
			c = json.charAt(i);
			// コーテーション内.
			if (cote != -1) {
				// コーテーションの終端.
				if (bef != '\\' && cote == c) {
					ret.add(json.substring(s - 1, i + 1));
					cote = -1;
					s = i + 1;
				}
			}
			// コーテーション開始.
			else if (bef != '\\' && (c == '\'' || c == '\"')) {
				cote = c;
				if (s != -1 && s != i && bef != ' ' && bef != '　' && bef != '\t' && bef != '\n' && bef != '\r') {
					ret.add(json.substring(s, i + 1));
				}
				s = i + 1;
				bef = -1;
			}
			// ワード区切り.
			else if (c == '[' || c == ']' || c == '{' || c == '}' || c == '(' || c == ')' || c == ':' || c == ';'
					|| c == ',' || (c == '.' && (bef < '0' || bef > '9'))) {
				if (s != -1 && s != i && bef != ' ' && bef != '　' && bef != '\t' && bef != '\n' && bef != '\r') {
					ret.add(json.substring(s, i));
				}
				ret.add(new String(new char[] { c }));
				s = i + 1;
			}
			// 連続空間区切り.
			else if (c == ' ' || c == '　' || c == '\t' || c == '\n' || c == '\r') {
				if (s != -1 && s != i && bef != ' ' && bef != '　' && bef != '\t' && bef != '\n' && bef != '\r') {
					ret.add(json.substring(s, i));
				}
				s = -1;
			}
			// その他文字列.
			else if (s == -1) {
				s = i;
			}
			bef = c;
		}
		return ret;
	}

	/** Json-Token解析. **/
	private static final Object createJsonInfo(int[] n, List<Object> token, int type, int no, int len) {
		String value;
		StringBuilder before = null;
		// List.
		if (type == TYPE_ARRAY) {
			List<Object> ret = new ObjectList<Object>();
			int flg = 0;
			for (int i = no + 1; i < len; i++) {
				value = (String) token.get(i);
				if (",".equals(value) || "]".equals(value)) {
					if ("]".equals(value)) {
						if (flg == 1) {
							if (before != null) {
								ret.add(decJsonValue(n, i, before.toString()));
							}
						}
						n[0] = i;
						return ret;
					} else {
						if (flg == 1) {
							if (before == null) {
								ret.add(null);
							} else {
								ret.add(decJsonValue(n, i, before.toString()));
							}
						}
					}
					before = null;
					flg = 0;
				} else if ("[".equals(value)) {
					ret.add(createJsonInfo(n, token, 0, i, len));
					i = n[0];
					before = null;
					flg = 0;
				} else if ("{".equals(value)) {
					ret.add(createJsonInfo(n, token, 1, i, len));
					i = n[0];
					before = null;
					flg = 0;
				} else {
					if (before == null) {
						before = new StringBuilder();
						before.append(value);
					} else {
						before.append(" ").append(value);
					}
					flg = 1;
				}
			}
			n[0] = len - 1;
			return ret;
		}
		// map.
		else if (type == TYPE_MAP) {
			Map<String, Object> ret;
			// ret = new HashMap<String, Object>();
			ret = new ArrayMap();
			String key = null;
			for (int i = no + 1; i < len; i++) {
				value = (String) token.get(i);
				if (":".equals(value)) {
					if (key == null) {
						throw new RhiginException(500, "Map format is invalid(No:" + i + ")");
					}
				} else if (",".equals(value) || "}".equals(value)) {
					if ("}".equals(value)) {
						if (key != null) {
							if (before == null) {
								ret.put(key, null);
							} else {
								ret.put(key, decJsonValue(n, i, before.toString()));
							}
						}
						n[0] = i;
						return ret;
					} else {
						if (key == null) {
							if (before == null) {
								continue;
							}
							throw new RhiginException(500, "Map format is invalid(No:" + i + ")");
						}
						if (before == null) {
							ret.put(key, null);
						} else {
							ret.put(key, decJsonValue(n, i, before.toString()));
						}
						before = null;
						key = null;
					}
				} else if ("[".equals(value)) {
					if (key == null) {
						throw new RhiginException(500, "Map format is invalid(No:" + i + ")");
					}
					ret.put(key, createJsonInfo(n, token, 0, i, len));
					i = n[0];
					key = null;
					before = null;
				} else if ("{".equals(value)) {
					if (key == null) {
						throw new RhiginException(500, "Map format is invalid(No:" + i + ")");
					}
					ret.put(key, createJsonInfo(n, token, 1, i, len));
					i = n[0];
					key = null;
					before = null;
				} else if (key == null) {
					key = value;
					if ((key.startsWith("'") && key.endsWith("'")) || (key.startsWith("\"") && key.endsWith("\""))) {
						key = key.substring(1, key.length() - 1).trim();
					}
				} else {
					if (before == null) {
						before = new StringBuilder();
						before.append(value);
					} else {
						before.append(" ").append(value);
					}
				}
			}
			n[0] = len - 1;
			return ret;
		}
		// その他.
		throw new RhiginException(500, "Failed to parse JSON.");
	}

	/** 数値チェック. **/
	protected static final boolean isNumeric(String o) {
		return Converter.isNumeric(o);
	}

	/** 日付を文字変換. **/
	protected static final String dateToString(java.util.Date d) {
		return DateConvert.getISO8601(d);
	}

	/** 文字を日付変換. **/
	protected static final java.util.Date stringToDate(String s) {
		return DateConvert.stringToDate(s);
	}

	/** JSDateオブジェクトの場合は、java.util.Dateに変換. **/
	protected static final java.util.Date getJSDate(IdScriptableObject io) {
		if ("Date".equals(io.getClassName())) {
			// NativeDate.
			try {
				// 現状リフレクションで直接取得するようにする.
				// 本来は ScriptRuntime.toNumber(NativeDate) で取得できるのだけど、
				// これは rhinoのContextの範囲内でないとエラーになるので.
				final Method md = io.getClass().getDeclaredMethod("getJSTimeValue");
				md.setAccessible(true);
				return new java.util.Date(Converter.convertLong(md.invoke(io)));
			} catch (Exception e) {
				// エラーの場合は処理しない.
			}
		}
		return null;
	}
}
