package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.Converter;
import rhigin.util.FComp;
import rhigin.util.FCompBuffer;
import rhigin.util.FixedKeyValues;

/**
 * 簡易圧縮解凍処理.
 */
public class FCompObject {
	public static final String OBJECT_NAME = "FComp";
	private static final class Execute extends RhiginFunction {
		final int type;

		Execute(int t) {
			this.type = t;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			FCompBuffer buf = null;
			if (args.length >= 1 && args[0] instanceof byte[]) {
				if (args.length >= 3) {
					switch (type) {
					case 0:
						buf = FComp.compress((byte[]) args[0], Converter.convertInt(args[1]),
								Converter.convertInt(args[2]));
						return buf.toByteArray();
					case 1:
						buf = FComp.decompress((byte[]) args[0], Converter.convertInt(args[1]),
								Converter.convertInt(args[2]));
						return buf.toByteArray();
					}
				} else if (args.length >= 2) {
					switch (type) {
					case 0:
						buf = FComp.compress((byte[]) args[0], 0, Converter.convertInt(args[1]));
						return buf.toByteArray();
					case 1:
						buf = FComp.decompress((byte[]) args[0], 0, Converter.convertInt(args[1]));
						return buf.toByteArray();
					}
				} else {
					switch (type) {
					case 0:
						buf = FComp.compress((byte[]) args[0]);
						return buf.toByteArray();
					case 1:
						buf = FComp.decompress((byte[]) args[0]);
						return buf.toByteArray();
					}
				}
			}
			return argsError(args);
		}

		@Override
		public final String getName() {
			switch (type) {
			case 0:
				return "freeze";
			case 1:
				return "unfreeze";
			}
			return "unknown";
		}

		private final Object argsError(Object[] args) {
			switch (type) {
			case 0:
			case 1:
				if (!(args.length >= 1 && args[0] instanceof byte[])) {
					argsException(OBJECT_NAME);
				}
			}
			return Undefined.instance;
		}
	};

	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject(OBJECT_NAME, new RhiginFunction[] {
		new Execute(0), new Execute(1) });

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
		scope.put(OBJECT_NAME, scope, FCompObject.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(OBJECT_NAME, FCompObject.getInstance());
	}
}
