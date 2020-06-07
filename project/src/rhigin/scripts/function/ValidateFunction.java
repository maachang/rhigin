package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.RhiginException;
import rhigin.http.Params;
import rhigin.http.Request;
import rhigin.http.Validate;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.RhiginFunction;
import rhigin.util.FixedKeyValues;

/**
 * Validate処理.
 */
public class ValidateFunction extends RhiginFunction {
	private static final ValidateFunction THIS = new ValidateFunction();

	public static final ValidateFunction getInstance() {
		return THIS;
	}
	
	@Override
	public String getName() {
		return "validate";
	}

	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		final RhiginContext context = ExecuteScript.currentRhiginContext();
		Object o = context.getAttribute("request");
		if(!(o instanceof Request)) {
			throw new RhiginException(401, "invalid access.");
		}
		final Request req = (Request)o;
		o = context.getAttribute("params");
		if(!(o instanceof Params)) {
			throw new RhiginException(401, "invalid access.");
		}
		final Params pms = (Params)o;
		final Params newParams = Validate.execute(req, pms, args);
		context.setAttribute("params", newParams);
		return newParams;
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("validate", scope, ValidateFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("validate", ValidateFunction.getInstance());
	}
}
