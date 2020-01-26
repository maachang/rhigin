package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.RhiginServerId;
import rhigin.scripts.RhiginFunction;
import rhigin.util.FixedKeyValues;

/**
 * [js]サーバIDを取得.
 */
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
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		return RhiginServerId.getInstance().getId();
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("serverId", scope, ServerIdFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("serverId", ServerIdFunction.getInstance());
	}
}
