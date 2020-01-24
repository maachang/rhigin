package rhigin.util;

import java.util.LinkedList;

/**
 * コンソールカラー文字出力.
 * 
 * 基本的にlinuxやmacのコンソールでしか、動作しない。
 * 
 * 使い方は以下の通り。
 * 
 * ColorConsoleOut c = new ColorConsoleOut();
 * c.println("aa<#blue>aa<#red>bbbb<#/red>ccc<#/blue>ddd");
 * 
 * または
 * c.println("aa<#blue>aa<#red>bbbb<#/end>ccc<#/end>ddd");
 * 
 */
public class ColorConsoleOut {
	private static final String black   = "\u001b[00;30m"; // 黒.
	private static final String red     = "\u001b[00;31m"; // 赤.
	private static final String green   = "\u001b[00;32m"; // 緑.
	private static final String yellow  = "\u001b[00;33m"; // 黄色.
	private static final String blue    = "\u001b[00;34m"; // 青.
	private static final String purple  = "\u001b[00;35m"; // 紫.
	private static final String cyan    = "\u001b[00;36m"; // 水色.
	private static final String white   = "\u001b[00;37m"; // 白.
	private static final String endCode = "\u001b[00m";    // 元に戻す.
	
	// カラー要素.
	private static final String[] colorValues = new String[] {
		black, red, green, yellow, blue, purple, cyan, white
	};
	
	// カラーテーブル.
	private static final FixedSearchArray<String> colorNames = new FixedSearchArray<String>(
		"black", "red", "green", "yellow", "blue", "purple", "cyan", "white"
	);
	
	// OS名を取得.
	private static final int OS_TYPE;
	static {
		IsOs io = IsOs.getInstance();
		int os = io.getOS();
		int type = -1;
		if(os == IsOs.OS_WIN9X || os == IsOs.OS_WINNT) {
			type = IsOs.OS_WINNT;
		} else if(os == IsOs.OS_MACINTOSH && os == IsOs.OS_MAC_OS_X) {
			type = IsOs.OS_MAC_OS_X;
		} else if(os == IsOs.OS_UNIX) {
			type = IsOs.OS_UNIX;
		}
		OS_TYPE = type;
	}
	
	// カラーコードをセット.
	private static final String colorString(boolean noColorMode, String s) {
		final boolean linuxMode = noColorMode && (OS_TYPE == IsOs.OS_UNIX || OS_TYPE == IsOs.OS_MAC_OS_X);
		StringBuilder buf = new StringBuilder();
		LinkedList<Integer> stack = new LinkedList<Integer>();
		
		int p, b, pp, v;
		boolean endFlg = false;
		String code = "";
		b = 0;
		while(true) {
			// <#..... を検索.
			p = s.indexOf("<#", b);
			if(p == -1) {
				// 無い場合は終了.
				break;
			}
			// ￥<#...... の場合は、無視して次の条件を検索.
			if(p - 1 >= 0 && s.charAt(p - 1) == '\\') {
				b = p + 1;
				continue;
			}
			// <#....> を検索.
			pp = s.indexOf(">", p + 2);
			if(pp == -1) {
				// 無い場合は終了.
				break;
			}
			// <#....> 内の文字列を取得.
			code = s.substring(p + 2, pp).trim().toLowerCase();
			// <#/....> の場合は終端.
			if(code.startsWith("/")) {
				endFlg = true;
				code = code.substring(1).trim();
			} else {
				endFlg = false;
			}
			// 現在までの情報をセット.
			buf.append(s.substring(b, p));
			b = pp + 1;
			// カラーコードを取得.
			v = colorNames.search(code);
			if(v == -1) {
				// linuxモードの場合のみ.
				if(linuxMode) {
					// <#/end> である場合.
					if(endFlg && "end".equals(code)) {
						stack.pop();
						if(stack.size() == 0) {
							// ENDコードをセット.
							buf.append(endCode);
						} else {
							buf.append(colorValues[stack.peek()]);
						}
					}
				}
				continue;
			}
			// 条件の終端の場合.
			if(endFlg) {
				// linuxモードの場合のみ.
				if(linuxMode) {
					if(v != stack.pop()) {
						// 構成がおかしいので、全返却.
						return s;
					}
					if(stack.size() == 0) {
						// ENDコードをセット.
						buf.append(endCode);
					} else {
						buf.append(colorValues[stack.peek()]);
					}
				}
			// linuxモードの場合のみ.
			} else if(linuxMode) {
				// カラーコードをセット.
				stack.push(v);
				buf.append(colorValues[v]);
			}
		}
		// 残りを出力.
		buf.append(s.substring(b));
		if(linuxMode && stack.size() > 0) {
			// ENDコードをセット.
			buf.append(endCode);
		}
		return buf.toString();
	}
	
	// カラーモード.
	private final Flag colorMode = new Flag();
	
	/**
	 * コンストラクタ.
	 */
	public ColorConsoleOut() {
		colorMode.set(true);
	}
	
	/**
	 * コンストラクタ.
	 * @param mode [false]の場合は、カラー出力しません.
	 */
	public ColorConsoleOut(boolean mode) {
		colorMode.set(mode);
	}
	
	/**
	 * カラーモードをセット.
	 * @param mode
	 */
	public void setColorMode(boolean mode) {
		colorMode.set(mode);
	}
	
	/**
	 * カラーモードの設定内容を取得.
	 * @return boolean [true]の場合、利用可能です.
	 */
	public boolean isColorMode() {
		return colorMode.get();
	}
	
	/**
	 * カラーモードが利用可能かチェック.
	 * @return boolean [true]の場合、利用可能です.
	 */
	public boolean useColorMode() {
		if(OS_TYPE == IsOs.OS_UNIX || OS_TYPE == IsOs.OS_MAC_OS_X) {
			return colorMode.get();
		}
		return false;
	}
	
	/**
	 * 文字列を出力.
	 * @param s
	 * @return
	 */
	public String toString(String s) {
		return colorString(colorMode.get(), s);
	}
	
	/**
	 * コンソール出力.
	 * @param s
	 */
	public void print(String s) {
		System.out.print(toString(s));
	}
	
	/**
	 * コンソール出力.
	 * @param s
	 */
	public void println(String s) {
		System.out.println(toString(s));
	}
	
	/**
	 * [エラー]コンソール出力.
	 * @param s
	 */
	public void errPrint(String s) {
		System.err.print(toString(s));
	}
	
	/**
	 * [エラー]コンソール出力.
	 * @param s
	 */
	public void errPrintln(String s) {
		System.err.println(toString(s));
	}
	
	// static 用.
	private static final ColorConsoleOut SNGL = new ColorConsoleOut();
	
	/**
	 * シングルトンを取得.
	 * @return
	 */
	public static final ColorConsoleOut getInstance() {
		return SNGL;
	}
}

