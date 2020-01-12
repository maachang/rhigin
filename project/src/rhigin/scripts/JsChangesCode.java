package rhigin.scripts;

import rhigin.util.Converter;
import rhigin.util.Indent;
import rhigin.util.OList;

/**
 * Javascriptのコードチェンジ・拡張処理.
 */
public class JsChangesCode {
	
	/**
	 * javascriptのコードチェンジ・拡張処理.
	 * 
	 * @param src
	 * @return
	 */
	public static final String changeCode(String src) {
		src = templateLiteral(src);
		src = textBlocks(src);
		return src;
	}
	
	/**
	 * js の Template literal対応.
	 * 
	 * @param src
	 * @return
	 */
	public static final String templateLiteral(String src) {
		int p, pp;
		int b = 0;
		p = indexOfBt(src, b);
		if(p == -1) {
			return src;
		}
		final StringBuilder buf = new StringBuilder((int)(src.length() * 1.5));
		while(true) {
			buf.append(src.substring(b, p));
			pp = indexOfBt(src, p + 1);
			if(pp == -1) {
				buf.append(src.substring(p));
				break;
			}
			buf.append(innerTextBlock(src.substring(p + 1, pp)));
			b = pp + 1;
			p = indexOfBt(src, b);
			if(p == -1) {
				buf.append(src.substring(b));
				break;
			}
		}
		return buf.toString();
	}
	
	// バックティック文字の検索.
	private static final int indexOfBt(String src, int off) {
		int p;
		while((p = Converter.indexOfNoCote(src, "`", off)) != -1) {
			if(p > 0 && src.charAt(p-1) == '\\') {
				off = p + 1;
				continue;
			}
			return p;
		}
		return -1;
	}
	
	/**
	 * java の textblocks（JEP 355）対応.
	 * 
	 * @param src
	 * @return
	 */
	public static final String textBlocks(String src) {
		// \r の条件を削除.
		src = Converter.changeString(src, "\r", "");
		// textBlock開始位置を検索.
		int p = src.indexOf("\"\"\"");
		if(p == -1) {
			return src;
		}
		char c;
		int n = -1;
		int len = src.length();
		// textBlocks開始位置の場合は """\nで終わってる必要があるので、そのチェック.
		for(int i = p + 3; i < len; i ++) {
			c = src.charAt(i);
			if(c == '\n') {
				n = i + 1;
				break;
			// スペースやタブは許可.
			} else if(!(c == ' ' || c == '\t')) {
				// 構成がおかしい場合は、ソース自体を返却する.
				return src;
			}
		}
		if(n == -1) {
			// 構成がおかしい場合は、ソース自体を返却する.
			return src;
		}
		int start = n;
		int b = 0;
		final StringBuilder buf = new StringBuilder((int)(len * 1.5));
		while(true) {
			// textBlockの終端を検索.
			n = src.indexOf("\"\"\"", start);
			if(n == -1) {
				buf.append(src.substring(b));
				break;
			}
			buf.append(src.substring(b, p));
			buf.append(innerTextBlock(src.substring(start, n)));
			b = n + 3;
			// 次の開始位置を検索.
			p = src.indexOf("\"\"\"", b);
			if(p == -1) {
				buf.append(src.substring(b));
				break;
			}
			n = -1;
			// textBlocks開始位置の場合は """\nで終わってる必要があるので、そのチェック.
			for(int i = p + 3; i < len; i ++) {
				c = src.charAt(i);
				if(c == '\n') {
					n = i + 1;
					break;
					// スペースやタブは許可.
				} else if(!(c == ' ' || c == '\t')) {
					// 構成がおかしい場合は、ソース自体を返却する.
					return src;
				}
			}
			if(n == -1) {
				// 構成がおかしい場合は、ソース自体を返却する.
				return src;
			}
			// 次の開始位置をセット.
			start = n;
		}
		return buf.toString();
	}
	
	// テキストブロック内の処理.
	private static final String innerTextBlock(String src) {
		// 改行単位に区分け.
		OList<String> line = getEnterLine(src);
		// 行単位で整頓して、インデントをUPする.
		getStartTrim(line);
		// １行に戻す.
		final StringBuilder buf = new StringBuilder((int)(src.length() * 1.5));
		buf.append("\"");
		final int len = line.size();
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append("\\n");
			}
			buf.append(line.get(i));
		}
		buf.append("\"");
		line = null;
		return changeVal(buf.toString());
	}
	
	// 改行単位で区切る.
	private static final OList<String> getEnterLine(String src) {
		// 改行単位で区切る.
		final OList<String> ret = new OList<String>();
		int p = 0, b = 0;
		while(true) {
			p = Converter.indexOfNoCote(src, "\n", b);
			if(p == -1) {
				ret.add(src.substring(b));
				break;
			}
			ret.add(src.substring(b, p));
			b = p + 1;
		}
		return ret;
	}
	
	// 改行単位で区切られた情報の最初のスペースやタブを削除して、整える.
	private static final void getStartTrim(OList<String> line) {
		if(line.size() == 0) {
			return;
		} else if(line.size() == 1) {
			line.toArray()[0] = Indent.upIndentDoubleCote(
				line.get(0).trim());
			return;
		}
		// 一番最初のスペースやタブを取得して
		// その内容を基準として行開始文字を削除する.
		char c;
		String head = line.get(0);
		int len = head.length();
		for(int i = 0; i < len; i ++) {
			c = head.charAt(i);
			if(!(c == ' ' || c == '\t')) {
				head = head.substring(0, i);
				break;
			}
		}
		// 行開始文字の削除とインデントを１つ上げる.
		final int headLen = head.length();
		len = line.size();
		Object[] ary = line.toArray();
		for(int i = 0; i < len; i ++) {
			if(((String)ary[i]).startsWith(head)) {
				// インデントを１つ上げる.
				ary[i] = Indent.upIndentDoubleCote(
					((String)ary[i]).substring(headLen));
			}
		}
	}
	
	// 変数表示の処理変換.
	private static final String changeVal(String src) {
		char c;
		boolean yen = false;
		int sp = 0, vp = -1;
		int start = -1;
		final StringBuilder buf = new StringBuilder((int)(src.length() * 1.5));
		final int len = src.length();
		for(int i = 0; i < len; i ++) {
			c = src.charAt(i);
			if(start != -1) {
				if(start == 0) {
					if(c == '{') {
						start = 1;
						vp = i + 1;
						buf.append("\" + (");
					} else {
						start = -1;
						buf.append('$').append(c);
					}
				} else if(c == '}') {
					start = -1;
					buf.append(Indent.downIndentDoubleCote(src.substring(vp, i)));
					buf.append(") + \"");
				}
			} else if(c == '$' && !yen) {
				start = 0;
				sp = i;
			} else {
				start = -1;
				buf.append(c);
			}
			yen = c == '\\';
		}
		if(start != -1) {
			buf.append(src.substring(sp));
		}
		return buf.toString();
	}
}
