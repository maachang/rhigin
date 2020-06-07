package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.http.execute.RhiginExecuteClientByAccessKey;
import rhigin.keys.RhiginAccessKeyClient;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.FixedKeyValues;

/**
 * AccessKeyClient管理の操作を行うオブジェクト.
 */
public class AccessKeyClientObject {
	public static final String OBJECT_NAME = "AccessKeyClient";
	private static final class Execute extends RhiginFunction {
		final int type;

		Execute(int t) {
			this.type = t;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch (type) {
			case 0: // create.
				if(args.length >= 1 && args[0] instanceof String) {
					return RhiginExecuteClientByAccessKey.getInstance().createByHome((String)args[0]);
				}
			case 1: // createByCOnf
				if(args.length >= 1 && args[0] instanceof String) {
					return RhiginExecuteClientByAccessKey.getInstance().createByConf((String)args[0]);
				}
			case 2: // constants.
				if(args.length >= 1 && args[0] instanceof String) {
					if(args.length >= 2 && args[1] instanceof String) {
						return RhiginExecuteClientByAccessKey.getInstance().isAccessKey((String)args[0], (String)args[1]);
					}
					return RhiginExecuteClientByAccessKey.getInstance().isAccessKey((String)args[0]);
				}
				break;
			case 3: // delete.
				if(args.length >= 1 && args[0] instanceof String) {
					if(args.length >= 2 && args[1] instanceof String) {
						return RhiginExecuteClientByAccessKey.getInstance().delete((String)args[0], (String)args[1]);
					}
					return RhiginExecuteClientByAccessKey.getInstance().delete((String)args[0]);
				}
				break;
			case 4: // urls.
				return RhiginAccessKeyClient.getInstance().getUrls();
			case 5: // keys.
				if(args.length >= 1 && args[0] instanceof String) {
					return RhiginAccessKeyClient.getInstance().getKeys((String)args[0]);
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
				return "createByConf";
			case 2:
				return "contains";
			case 3:
				return "remove";
			case 4:
				return "urls";
			case 5:
				return "keys";
			}
			return "unknown";
		}
	};
	
	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject(OBJECT_NAME,
		new RhiginFunction[]{ new Execute(0), new Execute(1), new Execute(2), new Execute(3) });

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
		scope.put(OBJECT_NAME, scope, AccessKeyClientObject.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(OBJECT_NAME, AccessKeyClientObject.getInstance());
	}
}
