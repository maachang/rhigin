package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.keys.RhiginAccessKeyFactory;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.FixedKeyValues;

/**
 * サーバ管理のAccessKeyの操作を行うオブジェクト.
 */
public class AccessKeyObject {
	public static final String OBJECT_NAME = "AccessKey";
	private static final class Execute extends RhiginFunction {
		final int type;

		Execute(int t) {
			this.type = t;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch (type) {
			case 0:
				return RhiginAccessKeyFactory.getInstance().get().create();
			case 1:
				if(args.length >= 1) {
					return RhiginAccessKeyFactory.getInstance().get().contains("" + args[0]);
				}
				break;
			case 2:
				if(args.length >= 1) {
					return RhiginAccessKeyFactory.getInstance().get().delete("" + args[0]);
				}
				break;
			}
			return argsException(OBJECT_NAME);
		}

		@Override
		public final String getName() {
			switch (type) {
			case 0:
				return "create";
			case 1:
				return "contains";
			case 2:
				return "remove";
			}
			return "unknown";
		}
	};

	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject(OBJECT_NAME,
		new RhiginFunction[] { new Execute(0), new Execute(1), new Execute(2) });

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
		scope.put(OBJECT_NAME, scope, AccessKeyObject.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(OBJECT_NAME, AccessKeyObject.getInstance());
	}
}
