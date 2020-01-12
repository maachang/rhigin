package rhigin.util;

/**
 * インデント処理.
 */
public class Indent {
	private Indent() {}
	
	/**
	 * 指定文字内のコーテーションインデントを1つ上げる.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @param indent
	 *            対象のインデント値を設定します. 0を設定した場合は１つインデントを増やします。
	 *            -1を設定した場合は１つインデントを減らします。
	 * @param dc
	 *            [true]の場合、ダブルコーテーションで処理します.
	 * @return String 変換された文字列が返されます.
	 */
	public static final String indentCote(String string, int indent, boolean dc) {
		if (string == null || string.length() <= 0) {
			return string;
		}
		char cote = (dc) ? '\"' : '\'';
		int len = string.length();
		char c;
		int j;
		int yenLen = 0;
		StringBuilder buf = new StringBuilder((int) (len * 1.25d));
		for (int i = 0; i < len; i++) {
			if ((c = string.charAt(i)) == cote) {
				if (yenLen > 0) {
					if (indent == -1) {
						yenLen >>= 1;
					} else {
						yenLen <<= 1;
					}
					for (j = 0; j < yenLen; j++) {
						buf.append("\\");
					}
					yenLen = 0;
				}
				if (indent == -1) {
					buf.append(cote);
				} else {
					buf.append("\\").append(cote);
				}
			} else if ('\\' == c) {
				yenLen++;
			} else {
				if (yenLen != 0) {
					for (j = 0; j < yenLen; j++) {
						buf.append("\\");
					}
					yenLen = 0;
				}
				buf.append(c);
			}
		}
		if (yenLen != 0) {
			for (j = 0; j < yenLen; j++) {
				buf.append("\\");
			}
		}
		return buf.toString();
	}

	/**
	 * 指定文字内のダブルコーテーションインデントを1つ上げる.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @return String 変換された文字列が返されます.
	 */
	public static final String upIndentDoubleCote(String string) {
		return indentCote(string, 0, true);
	}

	/**
	 * 指定文字内のシングルコーテーションインデントを1つ上げる.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @return String 変換された文字列が返されます.
	 */
	public static final String upIndentSingleCote(String string) {
		return indentCote(string, 0, false);
	}

	/**
	 * 指定文字内のダブルコーテーションインデントを1つ下げる.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @return String 変換された文字列が返されます.
	 */
	public static final String downIndentDoubleCote(String string) {
		// 文字列で検出されるダブルコーテーションが￥始まりの場合は、処理する.
		boolean exec = false;
		int len = string.length();
		char c, b;
		b = 0;
		for (int i = 0; i < len; i++) {
			c = string.charAt(i);
			if (c == '\"') {
				if (b == '\\') {
					exec = true;
				}
				break;
			}
			b = c;
		}
		if (exec) {
			return indentCote(string, -1, true);
		}
		return string;
	}

	/**
	 * 指定文字内のシングルコーテーションインデントを1つ下げる.
	 * 
	 * @param string
	 *            対象の文字列を設定します.
	 * @return String 変換された文字列が返されます.
	 */
	public static final String downIndentSingleCote(String string) {
		// 文字列で検出されるシングルコーテーションが￥始まりの場合は、処理する.
		boolean exec = false;
		int len = string.length();
		char c, b;
		b = 0;
		for (int i = 0; i < len; i++) {
			c = string.charAt(i);
			if (c == '\'') {
				if (b == '\\') {
					exec = true;
				}
				break;
			}
			b = c;
		}
		if (exec) {
			return indentCote(string, -1, false);
		}
		return string;
	}
}
