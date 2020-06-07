package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.http.Request;
import rhigin.keys.RhiginAccessKeyConstants;
import rhigin.keys.RhiginAccessKeyFactory;
import rhigin.keys.RhiginAccessKeyUtil;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.RhiginFunction;
import rhigin.util.FixedKeyValues;

/**
 * アクセスキーありのアクセスかチェックする.
 * 
 * アクセスキーが存在しないか、このサーバで管理しているアクセスキーでない場合は、
 * エラーを返却する.
 */
public class AuthAccessKeyFunction extends RhiginFunction {
	private static final String NAME = "authAccessKey";
	private static final AuthAccessKeyFunction THIS = new AuthAccessKeyFunction();

	public static final AuthAccessKeyFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		RhiginContext context = ExecuteScript.currentRhiginContext();
		Object o = context.getAttribute("request");
		if(!(o instanceof Request)) {
			throw new RhiginException(401, "invalid access.");
		}
		Request req = (Request)o;
		String ac = req.getString(RhiginAccessKeyConstants.RHIGIN_ACCESSKEY_HTTP_HEADER);
		if(!RhiginAccessKeyUtil.isAccessKey(ac)
				|| !RhiginAccessKeyFactory.getInstance().get().contains(ac)) {
			throw new RhiginException(401, "invalid access.");
		}
		return Undefined.instance;
	}
	
	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put(NAME, scope, AuthAccessKeyFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(NAME, AuthAccessKeyFunction.getInstance());
	}
}
