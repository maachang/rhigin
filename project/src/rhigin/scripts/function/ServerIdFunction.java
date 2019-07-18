package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.RhiginServerId;
import rhigin.scripts.RhiginFunction;

public class ServerIdFunction extends RhiginFunction {
	private static final ServerIdFunction THIS = new ServerIdFunction();
	public static final ServerIdFunction getInstance() {
		return THIS;
	}
	
	@Override
	public String getName() {
		return "serverId";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		return RhiginServerId.getInstance().getId();
	}
}
