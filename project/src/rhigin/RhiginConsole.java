package rhigin;

import org.mozilla.javascript.Undefined;

import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.comple.CompileCache;
import rhigin.scripts.function.RequireFunction;
import rhigin.util.ConsoleInKey;

public class RhiginConsole {
	public static final void main(String[] args) throws Exception {
		RhiginConsole o = new RhiginConsole();
		o.console(new ConsoleInKey());
	}
	
	public void console(ConsoleInKey console)
		throws Exception {
		
		// コンパイルキャッシュをセット.
		RequireFunction.getInstance().setCache(new CompileCache());
		
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
