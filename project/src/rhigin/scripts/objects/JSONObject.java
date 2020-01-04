package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.Json;
import rhigin.scripts.JsonOut;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;

/**
 * [js]Jsonオブジェクト.
 * 
 * var a = JSON.stringify({hoge: "moge"});
 * var b = JSON.parse(a);
 * console.log(JSON.toString(b));
 */
public final class JSONObject {
	private static final class Execute extends RhiginFunction {
		final int type;

		Execute(int t) {
			this.type = t;
		}

		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				switch (type) {
				case 0:
					return Json.encode(args[0]);
				case 1:
					return Json.decode("" + args[0]);
				case 2:
					return JsonOut.toString(args[0]);
				}
			}
			return argsError(args);
		}

		@Override
		public final String getName() {
			switch (type) {
			case 0:
				return "stringify";
			case 1:
				return "parse";
			case 2:
				return "toString";
			}
			return "unknown";
		}

		private final Object argsError(Object[] args) {
			switch (type) {
			case 0:
			case 1:
			case 2:
				if (!(args.length >= 1)) {
					argsException("JSON");
				}
			}
			return Undefined.instance;
		}
	};

	// オブジェクトリスト.
	private static final RhiginFunction[] list = { new Execute(0), new Execute(1), new Execute(2) };

	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject("JSON", list);

	public static final RhiginObject getInstance() {
		return THIS;
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("JSON", scope, JSONObject.getInstance());
	}
}
