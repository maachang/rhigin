package rhigin;

import org.mozilla.javascript.Undefined;

import rhigin.http.HttpInfo;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.compile.CompileCache;
import rhigin.scripts.function.RequireFunction;
import rhigin.util.ConsoleInKey;

/**
 * Rhiginコンソール起動用.
 */
public class RhiginConsole {
	public static final void main(String[] args) throws Exception {
		RhiginConfig conf = RhiginStartup.initLogFactory(false, args);
		RhiginConsole o = new RhiginConsole();
		o.console(conf, new ConsoleInKey());
	}
	
	public void console(RhiginConfig conf, ConsoleInKey console)
		throws Exception {
		// 開始処理.
		HttpInfo httpInfo = RhiginStartup.startup(false, conf);
		
		// コンパイルキャッシュ生成.
		CompileCache cache = new CompileCache(
			httpInfo.getCompileCacheSize(), httpInfo.getCompileCacheRootDir());
		
		// コンパイルキャッシュを require命令に設定.
		RequireFunction.getInstance().setCache(cache);
		
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
					if(o instanceof Undefined) {
							System.out.println("");
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
