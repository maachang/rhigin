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
		o.console(conf, new ConsoleInKey());
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
			String cmd;
			RhiginContext context = new RhiginContext();
			while (true) {
				try {
					if ((cmd = console.readLine("rhigin> ")) == null) {
						return;
					} else if ((cmd = cmd.trim()).length() == 0) {
						continue;
					} else if ("exit".equals(cmd) || "quit".equals(cmd)) {
						System.out.println("");
						return;
					}
					Object o = ExecuteScript.execute(context, cmd);
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
				}
			}
		} finally {
			console.close();
		}
	}
}
