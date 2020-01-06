package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.FCipher;

/**
 * [js]FCipherオブジェクト.
 */
public class FCipherObject {
	private static final ThreadLocal<FCipher> local = new ThreadLocal<FCipher>();

	private static final FCipher getFCipher() {
		FCipher ret = local.get();
		if (ret == null) {
			ret = new FCipher();
			local.set(ret);
		}
		return ret;
	}

	private static final class Execute extends RhiginFunction {
		final int type;

		Execute(int t) {
			this.type = t;
		}

		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (type == 0 || type == 1 || type == 2) {
				if (args.length >= 1) {
					if (type == 0) {
						String src = "" + args[0];
						String word = null;
						if (args.length >= 2) {
							word = "" + args[1];
						}
						return getFCipher().key(src, word);
					} else if (args[0] instanceof byte[]) {
						return type == 1 ? FCipher.hash_s((byte[]) args[0]) : FCipher.hashUuid((byte[]) args[0]);

					}
					return type == 1 ? FCipher.hash_s("" + args[0]) : FCipher.hashUuid("" + args[0]);
				}
			} else {
				if (args.length >= 2 && args[1] instanceof byte[]) {
					Object value = args[0];
					byte[] pKey = (byte[]) args[1];
					String head = null;
					if (args.length >= 3) {
						head = "" + args[2];
					}
					switch (type) {
					case 3:
						if (value instanceof byte[]) {
							return getFCipher().benc((byte[]) value, pKey, head);
						} else {
							return getFCipher().enc("" + value, pKey, head);
						}
					case 4:
						if (value instanceof byte[]) {
							return getFCipher().benc((byte[]) value, pKey, head);
						}
						break;
					case 5:
						return getFCipher().dec("" + value, pKey, head);
					case 6:
						return getFCipher().bdec("" + value, pKey, head);
					}
				}
			}
			return argsError(args);
		}

		@Override
		public final String getName() {
			switch (type) {
			case 0:
				return "key";
			case 1:
				return "hash";
			case 2:
				return "uuid";
			case 3:
				return "enc";
			case 4:
				return "benc";
			case 5:
				return "dec";
			case 6:
				return "bdec";
			}
			return "unknown";
		}

		private final Object argsError(Object[] args) {
			switch (type) {
			case 0:
			case 1:
			case 2:
				if (!(args.length >= 1)) {
					argsException("FCipher");
				}
				break;
			case 3:
				if (!(args.length >= 2 && args[1] instanceof byte[])) {
					argsException("FCipher");
				}
				break;
			case 4:
				if (!(args.length >= 2 && args[0] instanceof byte[] && args[1] instanceof byte[])) {
					argsException("FCipher");
				}
				break;
			case 5:
				if (!(args.length >= 2 && args[1] instanceof byte[])) {
					argsException("FCipher");
				}
				break;
			case 6:
				if (!(args.length >= 2 && args[1] instanceof byte[])) {
					argsException("FCipher");
				}
				break;
			}
			return Undefined.instance;
		}
	};

	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject("FCipher", new RhiginFunction[] {
			new Execute(0), new Execute(1), new Execute(2), new Execute(3),
			new Execute(4), new Execute(5), new Execute(6) });

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
		scope.put("FCipher", scope, FCipherObject.getInstance());
	}
}
