package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.Jwt;

/**
 * [js]Jwtオブジェクト.
 * 
 * var a = Jwt.create("hoge", {a:"moge"});
 * var b = Jwt.payload(a);
 * var c = Jwt.validate("hoge", a);
 */
public final class JwtObject {
	private static final class Execute extends RhiginFunction {
		final int type;
		Execute(int t) {
			this.type = t;
		}
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args.length >= 1) {
				switch(type) {
				case 0: if(args.length >= 2) {
					return Jwt.create(""+args[0], args[1]);
					}
					break;
				case 1: return Jwt.payload(""+args[0]);
				case 2: if(args.length >= 2) {
						return Jwt.validate("" + args[0],  "" + args[1]);
					}
					break;
				}
			}
			return Undefined.instance;
		}
		@Override
		public final String getName() {
			switch(type) {
			case 0: return "create";
			case 1: return "payload";
			case 2: return "validate";
			}
			return "unknown";
		}
	};
	
	// オブジェクトリスト.
	private static final RhiginFunction[] list = {
		new Execute(0), new Execute(1), new Execute(2)
	};
	
	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject("Jwt", list);
	public static final RhiginObject getInstance() {
		return THIS;
	}
	
	/**
	 * スコープにライブラリを登録.
	 * @param scope 登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("Jwt", scope, JwtObject.getInstance());
	}
}
