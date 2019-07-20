package rhigin.scripts.objects;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.logs.Log;
import rhigin.logs.LogFactory;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;

/**
 * [js]Consoleオブジェクト.
 * 
 * console.log("hogehoge");
 * console.debug("mogemoge");
 */
public class ConsoleObject {
	private static final Log LOG = LogFactory.create("console");
	
	// ログ出力用.
	private static final class Execute extends RhiginFunction {
		final int type;
		Execute(int t) {
			type = t;
		}
		@Override
	    public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args)
	    {
			if(args.length>= 1) {
				Object o = (args.length >= 1) ? args[0] : null;
				Object t = (args.length >= 2) ? args[1] : null;
				if(t != null && t instanceof Throwable) {
					switch(type) {
					case 0: System.out.println(jsString(o) + "\r\n" + getStackTrace((Throwable)t)); break;
					case 1: LOG.trace(jsString(o), (Throwable)t); break;
					case 2: LOG.debug(jsString(o), (Throwable)t); break;
					case 3: LOG.info(jsString(o), (Throwable)t); break;
					case 4: LOG.warn(jsString(o), (Throwable)t); break;
					case 5: LOG.error(jsString(o), (Throwable)t); break;
					case 6: LOG.fatal(jsString(o), (Throwable)t); break;
					}
				} else {
					switch(type) {
					case 0: System.out.println(jsString(o)); break;
					case 1: LOG.trace(jsString(o)); break;
					case 2: LOG.debug(jsString(o)); break;
					case 3: LOG.info(jsString(o)); break;
					case 4: LOG.warn(jsString(o)); break;
					case 5: LOG.error(jsString(o)); break;
					case 6: LOG.fatal(jsString(o)); break;
					}
				}
			}
			return Undefined.instance; 
		}
		@Override
		public final String getName() {
			switch(type) {
			case 0: return "log";
			case 1: return "trace";
			case 2: return "debug";
			case 3: return "info";
			case 4: return "warn";
			case 5: return "error";
			case 6: return "fatal";
			}
			return "";
		}
	};
	
	// stackTraceを文字出力.
	private static final String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}
	
	private static final String jsString(Object o) {
		ContextFactory.getGlobal().enterContext();
		try {
			return Context.toString(o);
		} finally {
			Context.exit();
		}
	}
	
	// オブジェクトリスト.
	private static final RhiginFunction[] list = {
		new Execute(0), new Execute(1), new Execute(2), new Execute(3), 
		new Execute(4), new Execute(5), new Execute(6)
	};
	
	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject("console", list);
	public static final RhiginObject getInstance() {
		return THIS;
	}
}
