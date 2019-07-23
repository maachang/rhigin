package rhigin.http;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rhigin.RhiginException;
import rhigin.scripts.Json;
import rhigin.util.Alphabet;
import rhigin.util.Converter;

/**
 * Validate処理.
 * validation(
 *     "name",          "String", "not null",   // name文字パラメータで、必須情報.
 *     "age",           "Number", "",           // age数値パラメータ.
 *     "comment",       "String", "max 128",    // comment文字パラメータで、最大文字が128文字.
 *     "X-Test-Code",   "String", "not null"    // X-Test-CodeHttpヘッダパラメータで、必須.
 * );
 * 
 * また、先頭に[method]を設定した場合、許可する条件として設定します. ※何も定義していない場合は、全てのmethodが有効です.
 *
 * validate( "POST", ・・・・・・・ );
 * 上記の場合、POSTのみ許可.
 */
public class Validate {
	private static boolean documentMode = false;
	/**
	 * Validateのドキュメント出力フラグをセット.
	 * @param m [true]の場合、ドキュメント出力となります.
	 */
	public static final void setDocumentMode(boolean m) {
		documentMode = m;
	}
	
	/**
	 * Validateのドキュメント出力フラグを取得.
	 * @return boolean [true]が返却された場合、ドキュメント出力可能です.
	 */
	public static final boolean isDocumentMode() {
		return documentMode;
	}
	
	// validate判断条件のチェック.
	public static final class ConditionsChecker {
		protected ConditionsChecker(){}
		
		// パラメータ変換.
		public static final Object check(String[] renameOut,String column, String type, Object value, String conditions) {
			if (!Converter.useString(conditions)) {
				return value;
			}

			// 情報をカット.
			List<String> list = new ArrayList<String>();
			Converter.cutString(list, true, false, conditions," 　\t_");
			if (list.size() == 0) {
				return value;
			}

			// 区切り条件毎に、チェック.
			int len = list.size();
			boolean notFlag = false;
			Object[] o = new Object[1];
			for (int pos = 0; pos < len; pos++) {
				if (not(list.get(pos))) {
					notFlag = true;
					continue;
				}

				// バリデーションなし.
				if (Alphabet.eq("none",list.get(pos))) {
					notFlag = false;
					continue;
				}

				if (isNull(notFlag, list.get(pos), column, conditions, value)) {
					notFlag = false;
					continue;
				}

				if (date(notFlag, list.get(pos), column, conditions, value)) {
					notFlag = false;
					continue;
				}

				if (time(notFlag, list.get(pos), column, conditions, value)) {
					notFlag = false;
					continue;
				}

				if (zip(notFlag, list.get(pos), column, conditions, value)) {
					notFlag = false;
					continue;
				}

				if (tel(notFlag, list.get(pos), column, conditions, value)) {
					notFlag = false;
					continue;
				}

				if (tel(notFlag, list.get(pos), column, conditions, value)) {
					notFlag = false;
					continue;
				}

				if (ipv4(notFlag, list.get(pos), column, conditions, value)) {
					notFlag = false;
					continue;
				}

				if (url(notFlag, list.get(pos), column, conditions, value)) {
					notFlag = false;
					continue;
				}

				if (email(notFlag, list.get(pos), column, conditions, value)) {
					notFlag = false;
					continue;
				}

				if (min(notFlag, list, pos, column, conditions, value)) {
					pos += 1;
					notFlag = false;
					continue;
				}

				if (max(notFlag, list, pos, column, conditions, value)) {
					pos += 1;
					notFlag = false;
					continue;
				}

				if (range(notFlag, list, pos, column, conditions, value)) {
					pos += 2;
					notFlag = false;
					continue;
				}

				o[0] = value;
				if(defaultValue(o,notFlag,list,pos,column,type,conditions)) {
					value = o[0];
					o[0] = null;
					pos += 1;
					notFlag = false;
					continue;
				}
				
				o[0] = null;
				if(renameValue(o,notFlag,list,pos,column,type,conditions)) {
					if(o[0] != null) {
						renameOut[0] = (String)o[0];
					}
					o[0] = null;
					pos += 1;
					notFlag = false;
					continue;
				}
				
				throw new RhiginException(500, "The validate condition for '" + column + "' is unknown:" + conditions);
			}
			
			if (notFlag) {
				throw new RhiginException(500, "The validate condition for '" + column + "' is unknown:" + conditions);
			}
			
			return value;
		}

		// not.
		private static final boolean not(String a) {
			return Alphabet.eq("not",a);
		}

		// null.
		private static final boolean isNull(boolean notFlag, String a,
				String column, String conditions, Object value) {
			if (Alphabet.eq("null",a)) {
				if (notFlag && value == null) {
					throw new RhiginException(400, "The value of '" + column + "' is null:" + conditions);
				}
				return true;
			}
			return false;
		}

		// date.
		private static final boolean date(boolean notFlag, String a, String column,
				String conditions, Object value) {
			return exp(DATE_EXP, "date", notFlag, a, column, conditions, value,
				" is not a date format.");
		}

		// time.
		private static final boolean time(boolean notFlag, String a, String column,
				String conditions, Object value) {
			return exp(TIME_EXP, "time", notFlag, a, column, conditions, value,
				" is not a time format.");
		}

		// zip.
		private static final boolean zip(boolean notFlag, String a, String column,
				String conditions, Object value) {
			return exp(ZIP_EXP, "zip", notFlag, a, column, conditions, value,
				" is not a zip code format.");
		}

		// tel.
		private static final boolean tel(boolean notFlag, String a, String column,
				String conditions, Object value) {
			return exp(TEL_EXP, "tel", notFlag, a, column, conditions, value,
				" is not a phone number format.");
		}

		// ipv4.
		private static final boolean ipv4(boolean notFlag, String a, String column,
				String conditions, Object value) {
			return exp(IPV4_EXP, "ipv4", notFlag, a, column, conditions, value,
				" is not in the format of IP address (IPV4).");
		}

		// url.
		private static final boolean url(boolean notFlag, String a, String column,
				String conditions, Object value) {
			return exp(URL_EXP, "url", notFlag, a, column, conditions, value,
				" is not a URL format.");
		}

		// email.
		private static final boolean email(boolean notFlag, String a,
				String column, String conditions, Object value) {
			return exp(EMAIL_EXP, "email", notFlag, a, column, conditions, value,
				" is not an email address format.");
		}

		// min [number].
		private static final boolean min(boolean notFlag, List<String> list,
				int pos, String column, String conditions, Object value) {
			if(Alphabet.eq("min",list.get(pos))) {
				if (value == null) {
					throw new RhiginException(400, "The value of '" + column + "' is null.");
				}
				int len = list.size();
				if (pos + 1 >= len) {
					throw new RhiginException(500, "Condition definition of '" + column + "' is not numeric:" + conditions);
				}
				String b = list.get(pos + 1);
				boolean eq;
				if (Converter.isNumeric(b)) {
					eq = Converter.convertString(value).length() < Converter.convertInt(b);
					if (eq != notFlag) {
						throw new RhiginException(400, "Length of '" + column + "' is out of condition:" + conditions);
					}
				} else {
					throw new RhiginException(500, "Condition definition of '" + column + "' is not numeric:" + conditions);
				}
				return true;
			}
			return false;
		}

		// max [number].
		private static final boolean max(boolean notFlag, List<String> list,
				int pos, String column, String conditions, Object value) {
			if(Alphabet.eq("max",list.get(pos))) {
				if (value == null) {
					throw new RhiginException(400, "The value of '" + column + "' is null.");
				}
				int len = list.size();
				if (pos + 1 >= len) {
					throw new RhiginException(500, "Condition definition of '" + column + "' is not numeric:" + conditions);
				}
				String b = list.get(pos + 1);
				boolean eq;
				if (Converter.isNumeric(b)) {
					eq = Converter.convertString(value).length() > Converter.convertInt(b);
					if (eq != notFlag) {
						throw new RhiginException(400, "Length of '" + column + "' is out of condition:" + conditions);
					}
				} else {
					throw new RhiginException(500, "Condition definition of '" + column + "' is not numeric:" + conditions);
				}
				return true;
			}
			return false;
		}
		
		// default [value].
		private static final boolean defaultValue(Object[] out,boolean notFlag, List<String> list,
				int pos, String column, String type, String conditions) {
			if(Alphabet.eq("default",list.get(pos))) {
				if(notFlag) {
					throw new RhiginException(400, "not condition of '" + column + "' is wrong:" + conditions);
				} else if (out[0] != null) {
					return true;
				}
				int len = list.size();
				if (pos + 1 >= len) {
					throw new RhiginException(500, "Condition definition of '" + column + "' is not numeric:" + conditions);
				}
				String b = list.get(pos + 1);
				out[0] = TypeConvert.convert(column,type,b);
				return true;
			}
			return false;
		}

		// rename [value].
		private static final boolean renameValue(Object[] out,boolean notFlag,List<String> list,
				int pos, String column, String type, String conditions) {
			if(Alphabet.eq("rename",list.get(pos))) {
				if(notFlag) {
					throw new RhiginException(400, "not condition of '" + column + "' is wrong:" + conditions);
				}
				int len = list.size();
				if (pos + 1 >= len) {
					throw new RhiginException(500, "Condition definition of '" + column + "' is not numeric:" + conditions);
				}
				out[0] = list.get(pos + 1);
				return true;
			}
			return false;
		}

		// range [number] [number].
		private static final boolean range(boolean notFlag, List<String> list,
				int pos, String column, String conditions, Object value) {
			if(Alphabet.eq("range",list.get(pos))) {
				if (value == null) {
					throw new RhiginException(400, "The value of '" + column + "' is null.");
				}
				int len = list.size();
				if (pos + 2 >= len) {
					throw new RhiginException(500, "Condition definition of '" + column + "' is not numeric:" + conditions);
				}
				String b = list.get(pos + 1);
				String c = list.get(pos + 2);
				boolean eq;
				String n = Converter.convertString(value);
				if (Converter.isNumeric(b)) {
					eq = n.length() < Converter.convertInt(b);
					if (eq != notFlag) {
						throw new RhiginException(400, "Length of '" + column + "' is out of condition:" + conditions);
					}
				} else {
					throw new RhiginException(500, "Condition definition of '" + column + "' is not numeric:" + conditions);
				}
				if (Converter.isNumeric(c)) {
					eq = n.length() > Converter.convertInt(c);
					if (eq != notFlag) {
						throw new RhiginException(400, "Length of '" + column + "' is out of condition:" + conditions);
					}
				} else {
					throw new RhiginException(500, "Condition definition of '" + column + "' is not numeric:" + conditions);
				}
				return true;
			}
			return false;
		}

		// exp.
		private static final Pattern DATE_EXP = Pattern
				.compile("^\\d{2,4}\\/([1][0-2]|[0][1-9]|[1-9])\\/([3][0-1]|[1-2][0-9]|[0][1-9]|[1-9])$");
		private static final Pattern TIME_EXP = Pattern
				.compile("^([0-1][0-9]|[2][0-3]|[0-9])\\:([0-5][0-9]|[0-9])$");
		private static final Pattern ZIP_EXP = Pattern.compile("^\\d{3}-\\d{4}$");
		private static final Pattern TEL_EXP = Pattern
				.compile("^[0-9]+\\-[0-9]+\\-[0-9]+$");
		private static final Pattern IPV4_EXP = Pattern
				.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
		private static final Pattern URL_EXP = Pattern
				.compile("https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+");
		private static final Pattern EMAIL_EXP = Pattern
				.compile("\\w{1,}[@][\\w\\-]{1,}([.]([\\w\\-]{1,})){1,3}$");

		// exp.
		private static final boolean exp(Pattern p, String m, boolean notFlag,
				String a, String column, String conditions, Object value,String message) {
			if(Alphabet.eq(m,a)) {
				if (value == null) {
					throw new RhiginException(400, "The value of '" + column + "' is null.");
				}
				Matcher mc = p.matcher(Converter.convertString(value));
				if(notFlag) {
					if (mc.find()) {
						throw new RhiginException(400, column + " " + message + "");
					}
				} else if (!mc.find()) {
					throw new RhiginException(400, column + " " + message + "");
				}
				return true;
			}
			return false;
		}
	}
	
	// タイプ判別.
	public static final class TypeConvert {
		
		// タイプコード.
		protected static final int STRING = 0;
		protected static final int BOOL = 1;
		protected static final int NUMBER = 10;
		protected static final int INTEGER = 11;
		protected static final int LONG = 12;
		protected static final int FLOAT = 13;
		protected static final int DATE = 20;
		protected static final int JSON = 30;
		protected static final int ARRAY = 31;
		
		protected TypeConvert(){}
		
		// パラメータ変換.
		public static final Object convert(String column, String type, Object value) {
			int code = typeByCode(type);
			return convert(column,code,type,value);
		}
		
		// 指定タイプに対して、タイプコード変換.
		public static final int typeByCode(String type) {
			if ("string".equals(type)) {
				return STRING;
			} else if ("number".equals(type)) {
				return NUMBER;
			} else if ("int".equals(type) || "integer".equals(type)) {
				return INTEGER;
			} else if ("long".equals(type)) {
				return LONG;
			} else if ("float".equals(type) || "double".equals(type)) {
				return FLOAT;
			} else if ("date".equals(type)) {
				return DATE;
			} else if ("bool".equals(type) || "boolean".equals(type)) {
				return BOOL;
			} else if ("json".equals(type)) {
				return JSON;
			} else if ("array".equals(type) || "list".equals(type)) {
				return ARRAY;
			} else {
				return STRING;
			}
		}
		
		// パラメータ変換.
		public static final Object convert(String column, int typeCode, String type, Object value) {
			try {
				if (value == null) {
					value = null;
				}
				switch(typeCode) {
				case STRING :
					try {
						value = Converter.convertString(value);
					} catch(Exception e) {
						value = null;
					}
					break;
				case NUMBER :
					if (Converter.isNumeric(value)) {
						if (Converter.isFloat(value)) {
							value = Converter.convertDouble(value);
						} else {
							Integer v1 = Converter.convertInt(value);
							Long v2 = Converter.convertLong(value);
							if ((long) v1 == v2) {
								value = v1;
							} else {
								value = v2;
							}
						}
					} else {
						value = null;
					}
					break;
				case INTEGER :
					if (Converter.isNumeric(value)) {
						value = Converter.convertInt(value);
					} else {
						value = null;
					}
					break;
				case LONG :
					if (Converter.isNumeric(value)) {
						value = Converter.convertLong(value);
					} else {
						value = null;
					}
					break;
				case FLOAT:
					if (Converter.isNumeric(value)) {
						value = Converter.convertDouble(value);
					} else {
						value = null;
					}
					break;
				case DATE :
					try {
						value = Converter.convertDate(value);
					} catch(Exception e) {
						value = null;
					}
					break;
				case BOOL :
					try {
						value = Converter.convertBool(value);
					} catch(Exception e) {
						value = null;
					}
					break;
				case JSON :
					try {
						value = Json.decode(Converter.convertString(value));
					} catch(Exception e) {
						value = null;
					}
					break;
				case ARRAY:
					if (value instanceof List) {
						return value;
					}
					value = null;
					break;
				default:
					try {
						value = Converter.convertString(value);
					} catch(Exception e) {
						value = null;
					}
					break;
				}
			} catch (RhiginException he) {
				throw he;
			} catch (Exception e) {
				throw new RhiginException(500, "The conversion condition " +
					type + " failed for the target column '" + column + "':"
					+ value, e);
			}
			return value;
		}
	}
	
	/**
	 * validate実行.
	 * @param request httpRequestを設定します.
	 * @param params httpパラメータを設定します.
	 * @param args validate条件を設定します.
	 * @return 生成された新しいパラメータが返却されます.
   */
	public static final Params execute(Request request, Params params, Object... args) {
		int i, j;
		final int len = args.length;
		// method許可チェック.
		final String method = request.getMethod();
		int off = 0;
		boolean eqMethod = false;
		// http method制限が存在する場合は、その情報を取得.
		for (i = 0; i < len; i++) {
			if (isHttpMethod(args[i])) {
				if (Alphabet.eq(method, Converter.convertString(args[0]))) {
					eqMethod = true;
				}
				off++;
			} else {
				break;
			}
		}
		if (off != 0 && !eqMethod) {
			throw new RhiginException(405, method + " method not allowed.");
		}
		// descriptionを読み飛ばす.
		// ドキュメントのみに利用.
		for (i = off; i < len; i++) {
			if(Converter.convertString(args[i]).startsWith("@")) {
				off ++;
			} else {
				break;
			}
		}
		
		// validate処理.
		Params newParams = new Params();
		for (i = off; i < len; i += 3) {
			oneValidate(newParams, params, request,
				Converter.convertString(args[i + 0]),
				Converter.convertString(args[i + 1]),
				Converter.convertString(args[i + 2]));
			// descriptionを読み飛ばす.
			// ドキュメントのみに利用.
			for(j = i + 3; j < len; j ++) {
				if(Converter.convertString(args[j]).startsWith("@")) {
					i ++;
				} else {
					break;
				}
			}
		}
		return newParams;
	}

	// POST,GET指定.
	private static final boolean isHttpMethod(Object n) {
		final String s = Converter.convertString(n);
		return Alphabet.eq("POST", s) || Alphabet.eq("GET", s);
	}

	// 1つのvalidation処理.
	private static final void oneValidate(Params newParams, Params params, Request request, String column, String type, String conditions) {
		Object value = null;
		if (!Converter.useString(column)) {
			throw new RhiginException(500, "Invalid column name in validation.");
		}
		if (!Converter.useString(type)) {
			type = "string";
		} else {
			type = type.toLowerCase();
		}
		if (!Converter.useString(conditions)) {
			conditions = null;
		}
		// Httpヘッダ情報.
		if (column.startsWith("X-")) {
			try {
				// HTTPヘッダ情報を取得.
				value = request.getHeader(column);
				
				// カラム名の[X-]を取り、-を抜いて、最初の文字を小文字に変換.
				// [X-Test-Code] -> testCode.
				column = Converter.changeString(column.substring(2), "-", "");
				column = column.substring(0, 1).toLowerCase() + column.substring(1);
			} catch (Exception e) {
				throw new RhiginException(500, "Failed to get header information '" + column + ".'", e);
			}
			// パラメータ情報.
		} else {
			value = params.get(column);
		}
		// typeデータ変換.
		value = TypeConvert.convert(column, type, value);
		// データチェック.
		String[] renameColumn = new String[]{null};
		value = ConditionsChecker.check(renameColumn, column, type, value, conditions);
		if(renameColumn[0] != null) {
			column = renameColumn[0];
		}
		// データセット.
		newParams.put(column, value);
	}
}
