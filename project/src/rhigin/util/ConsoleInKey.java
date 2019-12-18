package rhigin.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

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
		this(false);
	}
	
	/**
	 * コンストラクタ.
	 * @param notJline jlineを使わない場合 true.
	 */
	public ConsoleInKey(boolean notJline) {
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

	protected void finalize() throws Exception {
		close();
	}

	/**
	 * オブジェクトクローズ.
	 */
	public void close() throws IOException {
		console = null;
		if(consoleReader != null) {
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
}
