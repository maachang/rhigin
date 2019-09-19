package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.Converter;
import rhigin.util.RandomUUID;
import rhigin.util.UniqueId;

/**
 * ユニークなIDを生成するオブジェクト.
 */
public class UniqueIdObject {
	// uniqueId管理.
	private static final ThreadLocal<UniqueId> local = new ThreadLocal<UniqueId>();
	private static final int RANDOM_COUNT = 8192 - 1;

	// スレッド別に作成 - unuqieId.
	private static final UniqueId get() {
		UniqueId ret = local.get();
		if (ret == null) {
			RandomUUID uuid = new RandomUUID();
			uuid.getId((int) (System.nanoTime() & RANDOM_COUNT));
			ret = new UniqueId(uuid);
			local.set(ret);
		}
		return ret;
	}

	// uniqueId用メソッド群.
	private static final class Execute extends RhiginFunction {
		final int type;

		Execute(int t) {
			this.type = t;
		}

		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (type == 0) {
				return UniqueIdObject.get().getUUID();
			}
			if (args.length >= 1) {
				switch (type) {
				case 1:
					return UniqueIdObject.get().get(Converter.convertInt(args[0]));
				case 2:
					return UniqueIdObject.get().get64(Converter.convertInt(args[0]));
				case 3:
					return UniqueIdObject.get().code64(Converter.convertString(args[0]));
				case 4:
					return UniqueIdObject.get().decode64(Converter.convertString(args[0]));
				}
			}
			return argsError(args);
		}

		@Override
		public final String getName() {
			switch (type) {
			case 0:
				return "uuid";
			case 1:
				return "id";
			case 2:
				return "id64";
			case 3:
				return "code64";
			case 4:
				return "decode64";
			}
			return "unknown";
		}

		private final Object argsError(Object[] args) {
			switch (type) {
			case 1:
			case 2:
			case 3:
			case 4:
				if (!(args.length >= 1)) {
					return argsException("UniqueId");
				}
			}
			return Undefined.instance;
		}
	};

	// オブジェクトリスト.
	private static final RhiginFunction[] list = { new Execute(0), new Execute(1), new Execute(2), new Execute(3),
			new Execute(4) };

	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject("UniqueId", list);

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
		scope.put("UniqueId", scope, UniqueIdObject.getInstance());
	}
}
