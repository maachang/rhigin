package rhigin.scripts.objects;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.logs.Log;
import rhigin.logs.LogFactory;
import rhigin.scripts.function.AbstractFunction;

/**
 * [js]: Consoleオブジェクト.
 */
public class ConsoleObject {
	private static final Log LOG = LogFactory.create("console");
	
	// ログ出力用.
	private static final class out extends AbstractFunction {
		private int type = 0;
		out(int t) {
			type = t;
		}
		@Override
	    public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args)
	    {
			if(args.length>= 1) {
				Object o = (args.length >= 1) ? args[0] : null;
				Object t = (args.length >= 2) ? args[1] : null;
				if(t instanceof Throwable) {
					switch(type) {
					case 0: System.out.println(o + "\r\n" + getStackTrace((Throwable)t)); break;
					case 1: LOG.trace(o, (Throwable)t); break;
					case 2: LOG.debug(o, (Throwable)t); break;
					case 3: LOG.info(o, (Throwable)t); break;
					case 4: LOG.warn(o, (Throwable)t); break;
					case 5: LOG.error(o, (Throwable)t); break;
					case 6: LOG.fatal(o, (Throwable)t); break;
					}
				} else {
					switch(type) {
					case 0: System.out.println(o); break;
					case 1: LOG.trace(o); break;
					case 2: LOG.debug(o); break;
					case 3: LOG.info(o); break;
					case 4: LOG.warn(o); break;
					case 5: LOG.error(o); break;
					case 6: LOG.fatal(o); break;
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
    
	
	// オブジェクトリスト.
	private static final AbstractFunction[] list = {
		new out(0), new out(1), new out(2), new out(3), 
		new out(4), new out(5), new out(6)
	};
	
	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject("console", list);
	public static final RhiginObject getInstance() {
		return THIS;
	}
}