package rhigin.scripts;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import rhigin.RhiginException;
import rhigin.scripts.objects.JDateObject;
import rhigin.util.Alphabet;
import rhigin.util.Converter;
import rhigin.util.DateConvert;
import rhigin.util.ObjectList;

/**
 * Json変換処理.
 * 
 * Rhino専用に改造.
 */
@SuppressWarnings("rawtypes")
public final class Json {
	protected Json() {}

	private static final int TYPE_ARRAY = 0;
	private static final int TYPE_MAP = 1;
	
	private static JsonOriginCode ORIGIN_CODE = null;
	
	/**
	 * 拡張変換処理を追加.
	 * @param code
	 */
	public static final void setOriginCode(JsonOriginCode code) {
		ORIGIN_CODE = code;
	}
	
	/**
	 * 拡張変換処理が既に設定されているかチェック.
	 * @return
	 */
	public static final boolean isOriginCode() {
		return ORIGIN_CODE != null;
	}


	/**
	 * JSON変換.
	 * 
	 * @param target 対象のターゲットオブジェクトを設定します.
	 * @return String 変換されたJSON情報が返されます.
	 */
	public static final String encode(Object target) {
		StringBuilder buf = new StringBuilder();
		encodeObject(buf, target, target);
		return buf.toString();
	}

	/**
	 * JSON形式から、オブジェクト変換.
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
		return decodeJsonValue(json);
	}

	/** [encodeJSON]jsonコンバート. **/
	private static final void encodeObject(final StringBuilder buf, final Object base, Object target) {
		if(Undefined.isUndefined(target)) {
			target = null;
		} else if(target != null) {
			// rhinoのjavaオブジェクトwrapper対応.
			if (target instanceof Wrapper) {
				target = ((Wrapper) target).unwrap();
			// rhiginのjavaオブジェクトwrapper対応.
			} else if (target instanceof RhiginObjectWrapper) {
				target = ((RhiginObjectWrapper) target).unwrap();
			}
		}
		// その他変換コードが設定されている場合.
		if(ORIGIN_CODE != null) {
			try {
				// オブジェクト変換.
				target = ORIGIN_CODE.inObject(target);
				
				// その他変換コードが設定されている場合.
				String json = ORIGIN_CODE.encode(target);
				if(json != null) {
					buf.append(json);
				}
			} catch(RhiginException re) {
				throw re;
			} catch(Exception e) {
				throw new RhiginException(500, e);
			}
		}
		if (target == null) {
			buf.append("null");
		} else if (target instanceof Map) {
			if(((Map)target).size() == 0) {
				buf.append("{}");
			} else {
				encodeJsonMap(buf, base, (Map) target);
			}
		} else if (target instanceof List) {
			if(((List)target).size() == 0) {
				buf.append("[]");
			} else {
				encodeJsonList(buf, base, (List) target);
			}
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
			final java.util.Date d = getJSNativeDate((IdScriptableObject) target);
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
	private static final void encodeJsonMap(final StringBuilder buf, final Object base, final Map map) {
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
			encodeObject(buf, base, value);
		}
		buf.append("}");
	}

	/** [encodeJSON]jsonListコンバート. **/
	private static final void encodeJsonList(final StringBuilder buf, final Object base, final List list) {
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
			encodeObject(buf, base, value);
		}
		buf.append("]");
	}

	/** [encodeJSON]json配列コンバート. **/
	private static final void encodeJsonArray(final StringBuilder buf, final Object base, final Object list) {
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
			encodeObject(buf, base, value);
		}
		buf.append("]");
	}

	/** [decodeJSON]１つの要素を変換. **/
	private static final Object decodeJsonValue(String json) {
		try {
			final Object ret = _decodeJsonValue(json);
			if(ORIGIN_CODE != null) {
				return ORIGIN_CODE.outObject(ret);
			}
			return ret;
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(500, e);
		}
	}
	
	/** [decodeJSON]１つの要素を変換. **/
	private static final Object _decodeJsonValue(String json)
		throws Exception {
		if(ORIGIN_CODE != null) {
			boolean[] res = new boolean[] {false};
			Object value = ORIGIN_CODE.decode(res, json);
			if(res[0]) {
				return value;
			}
		}
		int len;
		// NULL文字.
		if(json == null || eq("null", json)) {
			return null;
		}
		// 空文字.
		else if ((len = json.length()) <= 0) {
			return "";
		}
		// BOOLEAN true.
		else if (eq("true", json)) {
			return Boolean.TRUE;
		}
		// BOOLEAN false.
		else if (eq("false", json)) {
			return Boolean.FALSE;
		}
		// 数値.
		else if (isNumeric(json)) {
			if (json.indexOf(".") != -1) {
				return Double.parseDouble(json);
			}
			return Long.parseLong(json);
		}
		// 文字列コーテーション区切り.
		else if ((json.startsWith("\"") && json.endsWith("\"")) || (json.startsWith("\'") && json.endsWith("\'"))) {
			json = json.substring(1, len - 1);
		}
		// ISO8601の日付フォーマットかチェック.
		else if (DateConvert.isISO8601(json)) {
			final java.util.Date d = stringToDate(json);
			if(d != null) {
				return JDateObject.newObject(d);
			}
		}
		// その他文字列.
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
	@SuppressWarnings("unchecked")
	private static final Object createJsonInfo(int[] n, List<Object> token, int type, int no, int len) {
		String value;
		StringBuilder before = null;
		// List.
		if (type == TYPE_ARRAY) {
			List<Object> ret = new JavaScriptable.GetList(new ObjectList<Object>());
			int flg = 0;
			for (int i = no + 1; i < len; i++) {
				value = (String) token.get(i);
				if (",".equals(value) || "]".equals(value)) {
					if ("]".equals(value)) {
						if (flg == 1) {
							if (before != null) {
								ret.add(decodeJsonValue(before.toString()));
							}
						}
						n[0] = i;
						return ret;
					} else {
						if (flg == 1) {
							if (before == null) {
								ret.add(null);
							} else {
								ret.add(decodeJsonValue(before.toString()));
							}
						}
					}
					before = null;
					flg = 0;
				} else if ("[".equals(value)) {
					ret.add(createJsonInfo(n, token, TYPE_ARRAY, i, len));
					i = n[0];
					before = null;
					flg = 0;
				} else if ("{".equals(value)) {
					ret.add(createJsonInfo(n, token, TYPE_MAP, i, len));
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
			Map<Object, Object> ret;
			// ret = new HashMap<Object, Object>();
			ret = new JsMap();
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
								ret.put(key, decodeJsonValue(null));
							} else {
								ret.put(key, decodeJsonValue(before.toString()));
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
							ret.put(key, decodeJsonValue(null));
						} else {
							ret.put(key, decodeJsonValue(before.toString()));
						}
						before = null;
						key = null;
					}
				} else if ("[".equals(value)) {
					if (key == null) {
						throw new RhiginException(500, "Map format is invalid(No:" + i + ")");
					}
					ret.put(key, createJsonInfo(n, token, TYPE_ARRAY, i, len));
					i = n[0];
					key = null;
					before = null;
				} else if ("{".equals(value)) {
					if (key == null) {
						throw new RhiginException(500, "Map format is invalid(No:" + i + ")");
					}
					ret.put(key, createJsonInfo(n, token, TYPE_MAP, i, len));
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
	
	/** 大文字、小文字関係なく比較. **/
	protected static final boolean eq(String a, String b) {
		return Alphabet.eq(a, b);
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
		try {
			return DateConvert.stringToDate(s);
		} catch(Exception e) {
			return null;
		}
	}

	/** JSのNativeDateオブジェクトの場合は、java.util.Dateに変換. **/
	protected static final java.util.Date getJSNativeDate(Object o) {
		Long ret = RhiginWrapUtil.convertRhinoNativeDateByLong(o);
		if(ret != null) {
			return JDateObject.newObject(ret);
		}
		return null;
	}
	
	/**
	 * 拡張エンコード、デコード処理を行う場合の継承クラス.
	 */
	public static abstract class JsonOriginCode {
		
		/**
		 * 入力オブジェクトの変換.
		 * Json.encodeObject で処理される毎に、この処理が呼ばれます.
		 * 
		 * @param o オブジェクトを設定します.
		 * @return Object 変換されたオブジェクトが返却されます.
		 * @exception Exception 例外.
		 */
		public Object inObject(Object o) throws Exception {
			return o;
		}
		
		/**
		 * 出力オブジェクトの変換.
		 * Json.decodeObject で処理結果毎に、この処理が呼ばれます.
		 * 
		 * @param o オブジェクトを設定します.
		 * @return Object 変換されたオブジェクトが返却されます.
		 * @exception Exception 例外.
		 */
		public Object outObject(Object o) throws Exception {
			return o;
		}
		
		/**
		 * オブジェクトをJSONに変換.
		 *
		 * @param o 対象のオブジェクトを設定します.
		 * @return String JSON変換された情報が返却されます.
		 *                [null]返却でこの処理で変換しません.
		 * @exception Exception 例外.
		 */
		public abstract String encode(Object o) throws Exception;
		
		/**
		 * JSONをオブジェクトに変換.
		 * 
		 * @param result result[0] = true の場合、変換処理が行われました.
		 * @parma json 対象の文字列が設定されます.
		 * @return Object 変換されたオブジェクトが返却されます.
		 * @exception Exception 例外.
		 */
		public abstract Object decode(boolean[] result, String json) throws Exception;
	}
}
