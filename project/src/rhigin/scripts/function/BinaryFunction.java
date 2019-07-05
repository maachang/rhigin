package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.util.Converter;

/**
 * [Function]: Binary生成メソッド.
 */
public final class BinaryFunction extends AbstractFunction {
	private static final BinaryFunction THIS = new BinaryFunction();
	public static final BinaryFunction getInstance() {
		return THIS;
	}
	
	@Override
	public String getName() {
		return "binary";
	}

	@Override
    public final Object call(Context ctx, Scriptable scope, Scriptable thisObj,
                       Object[] args)
    {
		if(args.length >= 1 && Converter.isFloat(args[0])) {
			return new byte[Converter.convertInt(args[0])];
		}
        return Undefined.instance;
    }
}