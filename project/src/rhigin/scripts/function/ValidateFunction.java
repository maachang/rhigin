package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.http.Params;
import rhigin.http.Request;
import rhigin.http.Validate;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.RhiginFunction;

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
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		final RhiginContext context = ExecuteScript.currentRhiginContext();
		final Request req = (Request) context.getAttribute("request");
		final Params pms = (Params) context.getAttribute("params");
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
}
