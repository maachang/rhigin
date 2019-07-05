package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

public class GetClassFunction extends AbstractFunction {
	private static final GetClassFunction THIS = new GetClassFunction();
	public static final GetClassFunction getInstance() {
		return THIS;
	}
	
	@Override
	public String getName() {
		return "getClass";
	}

	@Override
    public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args)
    {
		if(args.length >= 1) {
			return args[0] == null ? "null" : args[0].getClass().getName();
		}
        return Undefined.instance;
    }
}