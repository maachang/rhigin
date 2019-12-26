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
import rhigin.util.ConsoleInKey;

/**
 * Rhiginコンソール.
 */
public class RhiginConsole {
	public static final void main(String[] args) throws Exception {
		RhiginConfig conf = RhiginStartup.initLogFactory(false, args);
		RhiginConsole o = new RhiginConsole();
		o.console(conf, new ConsoleInKey(".rcons"));
	}

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
			RhiginContext context = new RhiginContext();
			while (true) {
				try {
					if ((cmd = console.readLine(RhiginConstants.NAME + "> ")) == null) {
						// null返却の場合は、ctrl+cの可能性があるので、
						// 終了処理をおこなってコンソール処理終了.
						System.out.println("");
						return;
					} else if ((cmd = cmd.trim()).length() == 0) {
						continue;
					} else if ("?".equals(cmd) || "help".equals(cmd)) {
						System.out.println("exit [quit]   Exit the console.");
						System.out.println("end  [close]  Terminate javascript processing.");
						System.out.println("");
						continue;
					} else if ("exit".equals(cmd) || "quit".equals(cmd)) {
						System.out.println("");
						return;
					} else if ("end".equals(cmd) || "close".equals(cmd)) {
						ExecuteScript.callEndScripts(cache);
						context = new RhiginContext();
						System.out.println("");
						continue;
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
			ExecuteScript.callEndScripts(cache);
			console.close();
		}
	}
}
