package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.scripts.UniqueIdManager;
import rhigin.util.Converter;
import rhigin.util.FixedKeyValues;

/**
 * ユニークなIDを生成するオブジェクト.
 */
public class UniqueIdObject {
	public static final String OBJECT_NAME = "UniqueId";

	// uniqueId用メソッド群.
	private static final class Execute extends RhiginFunction {
		final int type;

		Execute(int t) {
			this.type = t;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (type == 0) {
				return UniqueIdManager.get().getUUID();
			}
			if (args.length >= 1) {
				switch (type) {
				case 1:
					return UniqueIdManager.get().get(Converter.convertInt(args[0]));
				case 2:
					return UniqueIdManager.get().get64(Converter.convertInt(args[0]));
				case 3:
					return UniqueIdManager.get().code64(Converter.convertString(args[0]));
				case 4:
					return UniqueIdManager.get().decode64(Converter.convertString(args[0]));
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
					return argsException(OBJECT_NAME);
				}
			}
			return Undefined.instance;
		}
	};

	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject(OBJECT_NAME,
		new Execute(0), new Execute(1), new Execute(2), new Execute(3),
		new Execute(4));

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
		scope.put(OBJECT_NAME, scope, UniqueIdObject.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(OBJECT_NAME, UniqueIdObject.getInstance());
	}
}
