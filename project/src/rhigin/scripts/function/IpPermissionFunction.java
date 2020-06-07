package rhigin.scripts.function;

import java.net.InetAddress;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.http.Request;
import rhigin.net.IpPermission;
import rhigin.net.NetConstants;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.RhiginFunction;
import rhigin.util.FixedKeyValues;

/**
 * アクセスされたIPアドレスがIpPermissionの定義の範囲内かチェック.
 */
public class IpPermissionFunction extends RhiginFunction {
	private static final String NAME = "ipPermission";
	private static final IpPermissionFunction THIS = new IpPermissionFunction();

	public static final IpPermissionFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		IpPermission ip = IpPermission.getMainIpPermission();
		if(ip == null) {
			// IpPermissionが取得出来ない場合.
			// チェックしない.
			return Undefined.instance;
		}
		String target = NetConstants.IP_PERMISSION_DEFAULT_NAME;
		// ターゲットの定義名を引数より取得.
		if(args != null && args.length > 0 && args[0] instanceof String) {
			target = (String)args[0];
		}
		// 指定名の定義が存在しない場合はエラー返却.
		if(!ip.isName(target)) {
			throw new RhiginException(500, "Definition name of ip permission does not exist: " + target);
		}
		// args[0] = 定義名, args[1] ipアドレス or ドメイン名の場合.
		if(args != null && args.length >= 2) {
			if(!(args[1] instanceof String)) {
				throw new RhiginException(401, "invalid access.");
			}
			if(!ip.isPermission(target, (String)args[1])) {
				throw new RhiginException(401, "invalid access.");
			}
			return Undefined.instance;
		}
		// Requestを取得して取得出来ない場合はエラー.
		RhiginContext context = ExecuteScript.currentRhiginContext();
		Object o = context.getAttribute("request");
		System.out.println(o);
		if(!(o instanceof Request)) {
			throw new RhiginException(401, "invalid access.");
		}
		Request req = (Request)o;
		// 接続元のアドレスを取得して、範囲内かチェック.
		InetAddress addr = req.getRemoteInetAddress();
		if(!ip.isPermission(target, addr)) {
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
		scope.put(NAME, scope, IpPermissionFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(NAME, IpPermissionFunction.getInstance());
	}
}
