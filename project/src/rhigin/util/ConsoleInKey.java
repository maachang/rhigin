package rhigin.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.Console;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * コンソール入力支援.
 * 
 * jline2 のjar が読まれている場合は、そちらで処理.
 * そうでない場合はjava.io.Consoleを利用する.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ConsoleInKey implements Closeable, AutoCloseable {
	private Object consoleReader = null;
	private Class consoleReaderClass = null;
	private Console console = null;
	private BufferedReader in = null;

	/**
	 * コンストラクタ.
	 */
	public ConsoleInKey() {
		// jlineを利用する.
		this(null, null, false);
	}
	
	/**
	 * コンストラクタ.
	 * @param jhist jlineのHistoryのバックアップ先ファイル名を設定します.
	 */
	public ConsoleInKey(String jhist) {
		this(jhist, null, false);
	}
	
	/**
	 * コンストラクタ.
	 * @param jhist jlineのHistoryのバックアップ先ファイル名を設定します.
	 * @param jhistSize jlineのHistoryのバックアップ数を設定します.
	 */
	public ConsoleInKey(String jhist, Integer jhistSize) {
		this(jhist, jhistSize, false);
	}
	
	/**
	 * コンストラクタ.
	 * @param notJline jlineを使わない場合 true.
	 */
	public ConsoleInKey(boolean notJline) {
		this(null, null, notJline);
	}
	
	/**
	 * コンストラクタ.
	 * @param jhist jlineのHistoryのバックアップ先ファイル名を設定します.
	 * @param notJline jlineを使わない場合 true.
	 */
	public ConsoleInKey(String jhist, boolean notJline) {
		this(jhist, null, notJline);
	}
	
	/**
	 * コンストラクタ.
	 * @param jhist jlineのHistoryのバックアップ先ファイル名を設定します.
	 * @param jhistSize jlineのHistoryのバックアップ数を設定します.
	 * @param notJline jlineを使わない場合 true.
	 */
	public ConsoleInKey(String jhist, Integer jhistSize, boolean notJline) {
		if(!notJline) {
			// jline2が利用できる場合は、jline2を利用する.
			try {
				Class clazz = Class.forName("jline.console.ConsoleReader");
				consoleReader = clazz.getConstructor().newInstance();
				consoleReaderClass = clazz;
				console = null;
				in = null;
				
				// Ctrl+Cを検知.
				consoleReaderClass.getMethod("setHandleUserInterrupt", boolean.class)
					.invoke(consoleReader, true);
				
				// 履歴を読み込む.
				jlineHistoryFileName = jhist == null || jhist.isEmpty() ? DEF_JHIST_FILE : jhist;
				jlineHistorySize = jhistSize == null || jhistSize <= 0 ? DEF_JHIST_SIZE : jhistSize;
				
				// 履歴の読み込み.
				try {
					loadJhist();
				} catch(Exception ee) {}
				
			} catch(Throwable e) {
				consoleReader = null;
				consoleReaderClass = null;
				console = null;
				in = null;
				notJline = true;
			}
		}
		if(notJline) {
			BufferedReader b = null;
			Console c = System.console();
			if (c == null) {
				b = new BufferedReader(new InputStreamReader(System.in));
			}
			consoleReader = null;
			consoleReaderClass = null;
			console = c;
			in = b;
		}
	}

	/**
	 * オブジェクトクローズ.
	 */
	public void close() throws IOException {
		console = null;
		if(consoleReader != null) {
			// 履歴の書き込み.
			saveJhist();
			// クローズ処理.
			try {
				consoleReaderClass.getMethod("close").invoke(consoleReader);
			} catch(Exception e) {}
			consoleReader = null;
			consoleReaderClass = null;
		}
		if (in != null) {
			in.close();
			in = null;
		}
	}

	/**
	 * 1行入力情報を取得.
	 * 
	 * @param view
	 *            表示条件を設定します.
	 * @return String １行の入力情報が返却されます.
	 * @exception IOException
	 */
	public String readLine() throws IOException {
		return readLine("");
	}

	/**
	 * 1行入力情報を取得.
	 * 
	 * @param view
	 *            表示条件を設定します.
	 * @return String １行の入力情報が返却されます.
	 * @exception IOException
	 */
	public String readLine(String view) throws IOException {
		if (view != null && view.length() != 0) {
			if(consoleReader != null) {
				try {
					return (String)consoleReaderClass.getMethod("readLine", String.class)
						.invoke(consoleReader, view);
				} catch(Throwable e) {
					if(e instanceof InvocationTargetException) {
						e = ((InvocationTargetException)e).getCause();
					}
					if(isJlineByCtrl_C(e)) {
						// ctrl+c が検知された場合は、null返却.
						return null;
					}
					throw new IOException(e);
				}
			} else if (console != null) {
				return console.readLine(view);
			}
			System.out.print(view);
			return in.readLine();
		} else {
			if(consoleReader != null) {
				try {
					return (String)consoleReaderClass.getMethod("readLine")
						.invoke(consoleReader);
				} catch(Throwable e) {
					if(e instanceof InvocationTargetException) {
						e = ((InvocationTargetException)e).getCause();
					}
					if(isJlineByCtrl_C(e)) {
						// ctrl+c が検知された場合は、null返却.
						return null;
					}
					throw new IOException(e);
				}
			} else if (console != null) {
				return console.readLine();
			}
			return in.readLine();
		}
	}
	
	/**
	 * パスワード入力.
	 * 
	 * @param view
	 *            表示条件を設定します.
	 * @return String １行の入力情報が返却されます.
	 * @exception IOException
	 */
	public String readPassword(String view) throws IOException {
		if (view != null && view.length() != 0) {
			if(consoleReader != null) {
				try {
					return (String)consoleReaderClass.getMethod("readLine", String.class, Character.class)
						.invoke(consoleReader, view, '*');
				} catch(Throwable e) {
					if(e instanceof InvocationTargetException) {
						e = ((InvocationTargetException)e).getCause();
					}
					if(isJlineByCtrl_C(e)) {
						// ctrl+c が検知された場合は、null返却.
						return null;
					}
					throw new IOException(e);
				}
			} else if (console != null) {
				System.out.print(view);
				char[] ret = console.readPassword();
				if(ret == null || ret.length == 0) {
					return "";
				}
				return new String(ret);
			}
			System.out.print(view);
			return in.readLine();
		} else {
			if(consoleReader != null) {
				try {
					return (String)consoleReaderClass.getMethod("readLine", Character.class)
						.invoke(consoleReader, '*');
				} catch(Throwable e) {
					if(e instanceof InvocationTargetException) {
						e = ((InvocationTargetException)e).getCause();
					}
					if(isJlineByCtrl_C(e)) {
						// ctrl+c が検知された場合は、null返却.
						return null;
					}
					throw new IOException(e);
				}
			} else if (console != null) {
				char[] ret = console.readPassword();
				if(ret == null || ret.length == 0) {
					return "";
				}
				return new String(ret);
			}
			return in.readLine();
		}
	}
	
	// jlineの例外がctrl+c 押下での返却の場合.
	private static final boolean isJlineByCtrl_C(Throwable e) {
		return ("jline.console.UserInterruptException".equals(e.getClass().getName()));
	}
	
	//
	// jline向けの履歴管理.
	//
	
	// デフォルトのjist履歴のバックアップファイル先.
	private static final String DEF_JHIST_FILE = ".jline";
	
	// デフォルトのjlist履歴のバックアップ最大データ数.
	private static final int DEF_JHIST_SIZE = 100;
	
	// jlistの履歴管理条件.
	private String jlineHistoryFileName = DEF_JHIST_FILE;
	private int jlineHistorySize = DEF_JHIST_SIZE;
	
	// jhistの履歴をファイルからロード.
	private final void loadJhist() throws Exception {
		if(!FileUtil.isFile(jlineHistoryFileName)) {
			return;
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(
				new InputStreamReader(
					new FileInputStream(jlineHistoryFileName), "UTF8"));
			String in;
			final OList<String> olist = new OList<String>(jlineHistorySize);
			while((in = br.readLine()) != null) {
				olist.add(in);
			}
			in = null;
			final Object h = consoleReaderClass.getMethod("getHistory").invoke(consoleReader);
			final Method m = h.getClass().getMethod("add", CharSequence.class);
			for(int i = olist.size() - 1; i >= 0; i --) {
				m.invoke(h, olist.get(i));
			}
			br.close(); br = null;
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch(Exception e) {}
			}
		}
	}
	
	// jlineの履歴をファイルに保存.
	private final void saveJhist() {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(
				new OutputStreamWriter(
					new FileOutputStream(jlineHistoryFileName), "UTF8"));
			int cnt = 0;
			CharSequence n;
			final Object h = consoleReaderClass.getMethod("getHistory").invoke(consoleReader);
			final Class hc = h.getClass();
			final int len = (Integer)hc.getMethod("size").invoke(h);
			final Method m = hc.getMethod("get", int.class);
			final Set<CharSequence> chk = new HashSet<CharSequence>(len << 1);
			for(int i = len-1; i >= 0; i --) {
				if((n = (CharSequence)m.invoke(h, i)) == null || chk.contains(n)) {
					continue;
				}
				chk.add(n);
				if(cnt != 0) {
					bw.newLine();
				}
				bw.append(n);
				cnt ++;
				if(cnt > jlineHistorySize) {
					break;
				}
			}
			bw.close(); bw = null;
		} catch(Exception e) {
		} finally {
			if(bw != null) {
				try {
					bw.close();
				} catch(Exception e) {}
			}
		}
	}
	
	
}
