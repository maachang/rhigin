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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import rhigin.RhiginException;

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
	 * タブ補完を追加します.
	 * 
	 * @param tb 
	 * @throws Exception
	 */
	public void addTabCompleter(final TabCompleter tb) throws Exception {
		addJCompleter(tb);
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
	
	// [jline2専用]例外がctrl+c 押下での返却の場合.
	private static final boolean isJlineByCtrl_C(Throwable e) {
		return ("jline.console.UserInterruptException".equals(e.getClass().getName()));
	}
	
	//
	// [jline2専用]履歴管理.
	//
	
	// デフォルトのjist履歴のバックアップファイル先.
	private static final String DEF_JHIST_FILE = ".jline";
	
	// デフォルトのjlist履歴のバックアップ最大データ数.
	private static final int DEF_JHIST_SIZE = 100;
	
	// [jline2専用]履歴管理条件.
	private String jlineHistoryFileName = DEF_JHIST_FILE;
	private int jlineHistorySize = DEF_JHIST_SIZE;
	
	// [jline2専用]履歴をファイルからロード.
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
	
	// [jline2専用]履歴をファイルに保存.
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
	
	// [jline2専用]補完処理を追加.
	private final void addJCompleter(final TabCompleter tb) throws Exception {
		if(consoleReader == null) {
			return;
		}
		Method m = consoleReaderClass.getMethod("addCompleter", jline.console.completer.Completer.class);
		m.invoke(consoleReader, new jline.console.completer.Completer() {
			final TabCompleter tab = tb;
			public int complete(String buffer, int cursor, List<CharSequence> candidates) {
				return tab.complete(buffer, cursor, candidates) ? 0 : -1;
			}
		});
	}
	
	/**
	 * [jline2専用]タブ補完定義.
	 */
	public static abstract class TabCompleter {
		
		/**
		 * 補完呼び出し.
		 * @param buffer 入力中の文字列が設定されます.
		 * @param cursor 入力位置のカーソルが設定されます.
		 * @param candidates 補完対象の文字列が返却されます.
		 * @return boolean false の場合は、情報は存在しない.
		 *                 true の場合は情報が存在する.
		 */
		public abstract boolean complete(String buffer, int cursor, List<CharSequence> candidates);
	}
	
	/**
	 * [jline2専用]文字列（大文字、小文字無視）補完用.
	 */
	public static class StringIgnoreCaseCompleter extends TabCompleter {
		protected final String[] list;
		
		public StringIgnoreCaseCompleter(String... cs) {
			Set<String> s = new HashSet<String>();
			int len = cs.length;
			for(int i = 0; i < len; i ++) {
				s.add(cs[i]);
			}
			int cnt = 0;
			len = s.size();
			final String[] list = new String[len];
			final Iterator<String> it = s.iterator();
			while(it.hasNext()) {
				list[cnt++] = it.next();
			}
			this.list = list;
		}
		
		@Override
		public boolean complete(String buffer, int cursor, List<CharSequence> candidates) {
			final String target = (cursor > 0) ? buffer.substring(0, cursor) : "";
			if(target.length() == 0) {
				return false;
			}
			final boolean upper = target.charAt(0) >= 'A' && target.charAt(0) <= 'Z';
			final int len = list.length;
			for(int i = 0; i < len; i ++) {
				if(Alphabet.indexOf(list[i], target) == 0) {
					candidates.add(upper ? list[i].toUpperCase() : list[i]);
				}
			}
			return candidates.size() > 0;
		}
	}
	
	/**
	 * [jline2専用]文字列（大文字、小文字無視）補完用.
	 */
	public static class StringCompleter extends TabCompleter {
		protected final String[] list;
		
		public StringCompleter(String... cs) {
			Set<String> s = new HashSet<String>();
			int len = cs.length;
			for(int i = 0; i < len; i ++) {
				s.add(cs[i]);
			}
			int cnt = 0;
			len = s.size();
			String[] list = new String[len];
			Iterator<String> it = s.iterator();
			while(it.hasNext()) {
				list[cnt++] = it.next();
			}
			this.list = list;
		}
		
		@Override
		public boolean complete(String buffer, int cursor, List<CharSequence> candidates) {
			final String target = (cursor > 0) ? buffer.substring(0, cursor) : "";
			if(target.length() == 0) {
				return false;
			}
			final int len = list.length;
			for(int i = 0; i < len; i ++) {
				if(list[i].startsWith(target)) {
					candidates.add(list[i]);
				}
			}
			return candidates.size() > 0;
		}
	}
}
