package rhigin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.http.HttpInfo;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.JsonOut;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.compile.CompileCache;
import rhigin.scripts.function.RandomFunction;
import rhigin.scripts.function.RequireFunction;
import rhigin.util.Args;
import rhigin.util.ConsoleInKey;

/**
 * Rhiginコンソール.
 */
public class RhiginConsole {
	public static final void main(String[] args) throws Exception {
		Args.set(args);
		if(viewArgs()) {
			System.exit(0);
			return;
		}
		int ret = 0;
		try {
			RhiginConfig conf = RhiginStartup.initLogFactory(false);
			RhiginConsole o = new RhiginConsole();
			o.console(conf, new ConsoleInKey(".rcons"));
		} catch(Throwable t) {
			t.printStackTrace();
			ret = 1;
		}
		System.exit(ret);
	}
	
	// プログラム引数による命令.
	private static final boolean viewArgs() {
		Args params = Args.getInstance();
		if(params.isValue("-v", "--version")) {
			System.out.println(RhiginConstants.VERSION);
			return true;
		} else if(params.isValue("-h", "--help")) {
			System.out.println("rcons [-e]");
			System.out.println(" Run the rhigin console.");
			System.out.println("  [-e] [--env]");
			System.out.println("    Set the environment name for reading the configuration.");
			System.out.println("    For example, when `-e hoge` is specified, the configuration ");
			System.out.println("    information under `./conf/hoge/` is read.");
			return true;
		}
		return false;
	}
	
	// ゼロサプレスを取得.
	private static final String zero() {
		StringBuilder buf = new StringBuilder();
		int len = RhiginConstants.NAME.length();
		for(int i = 0; i < len; i ++) {
			buf.append("0");
		}
		return buf.toString();
	}
	
	// コマンドの先頭のスペース・タブ情報を取得.
	private static final String cmdHead(String cmd) {
		if(cmd == null) {
			return null;
		}
		char c;
		int len = cmd.length();
		for(int i = 0; i < len; i ++) {
			c = cmd.charAt(i);
			if(!(c == ' ' || c == '\t')) {
				return cmd.substring(0, i);
			}
		}
		return cmd;
	}
	
	/**
	 * コンソール処理.
	 * @param conf
	 * @param console
	 * @throws Exception
	 */
	public void console(RhiginConfig conf, ConsoleInKey console) throws Exception {
		// 開始処理.
		HttpInfo httpInfo = RhiginStartup.startup(conf);

		// コンパイルキャッシュ生成.
		// コンパイルキャッシュを require命令に設定.
		CompileCache cache = new CompileCache(httpInfo.getCompileCacheSize(), httpInfo.getCompileCacheRootDir());
		RequireFunction.init(cache);

		// ランダムオブジェクトをセット.
		RandomFunction.init();

		System.out.println("" + RhiginConstants.NAME + " console version (" + RhiginConstants.VERSION + ")");
		System.out.println("");
		try {
			Object o;
			String cmd;
			String cmdHead;
			String allCmd = null;
			String zero = zero();
			String simbol;
			int cmdLineCount = 0;
			RhiginContext context = new RhiginContext();
			while (true) {
				try {
					if(cmdLineCount == 0) {
						simbol = RhiginConstants.NAME;
					} else {
						simbol = zero.substring((""+cmdLineCount).length()) + cmdLineCount;
					}
					// コマンド入力.
					cmd = console.readLine(simbol + "> ");
					cmdHead = cmdHead(cmd);
					if (cmd == null) {
						// null返却の場合は、ctrl+cの可能性があるので、
						// 終了処理をおこなってコンソール処理終了.
						System.out.println("");
						return;
					} else if ((cmd = cmd.trim()).length() == 0) {
						if(cmdLineCount == 0) {
							continue;
						}
					} else if (cmdLineCount == 0 && ("?".equals(cmd) || "help".equals(cmd))) {
						System.out.println("exit [quit]   Exit the console.");
						System.out.println("end  [close]  Terminate javascript processing.");
						System.out.println("");
						continue;
					} else if (cmdLineCount == 0 && ("exit".equals(cmd) || "quit".equals(cmd))) {
						System.out.println("");
						return;
					} else if (cmdLineCount == 0 && ("end".equals(cmd) || "close".equals(cmd))) {
						ExecuteScript.callEndScripts(false, cache);
						context = new RhiginContext();
						System.out.println("");
						continue;
					} else if (cmd.endsWith("\\")) {
						// 行の最後に￥マークをセットした場合は、次のコマンドと連結できる.
						if(cmdLineCount == 0) {
							allCmd = cmdHead + cmd.substring(0, cmd.length() - 1).trim() + "\n";
						} else {
							allCmd += cmdHead + cmd.substring(0, cmd.length() - 1).trim() + "\n";
						}
						cmdLineCount ++;
						continue;
					}
					if(cmdLineCount != 0) {
						cmd = allCmd + cmdHead + cmd;
						allCmd = null;
						cmdLineCount = 0;
						System.out.println(">" + cmd);
					}
					o = ExecuteScript.execute(context, cmd);
					cmd = null;
					if(o == null) {
						System.out.println("null");
					} else if (o instanceof Undefined) {
						System.out.println("");
					} else if (o instanceof Scriptable) {
						if(o instanceof Map || o instanceof List) {
							System.out.println(JsonOut.toString(o));
						} else {
							ContextFactory.getGlobal().enterContext();
							try {
								System.out.println(Context.toString(o));
							} finally {
								Context.exit();
							}
						}
					} else if(o instanceof Map || o instanceof List ||
						o instanceof Iterator || o.getClass().isArray()) {
						System.out.println(JsonOut.toString(o));
					} else {
						System.out.println(o);
					}
				} catch (Throwable e) {
					e.printStackTrace();
				} finally {
					o = null;
					cmd = null;
				}
			}
		} finally {
			ExecuteScript.callEndScripts(false, cache);
			ExecuteScript.callEndScripts(true, cache);
			console.close();
		}
	}
}
