package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.net.IpPermission;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.FixedKeyValues;

public class IpPermissionObject {
	public static final String OBJECT_NAME = "IpPermission";
	private static final class Execute extends RhiginFunction {
		final int type;

		Execute(int t) {
			this.type = t;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			final IpPermission ip = IpPermission.getMainIpPermission();
			if(ip == null) {
				throw new RhiginException("IpPertetion has not been initialized.");
			}
			switch (type) {
			case 0: // add.
				if(args != null && args.length >= 1 && args[0] instanceof String) {
					String name = null;
					String addr = null;
					if(args.length >= 2 && args[1] instanceof String) {
						name = (String)args[0];
						addr = (String)args[1];
					} else {
						addr = (String)args[0];
					}
					ip.add(name, addr);
					return Undefined.instance;
				}
				break;
			case 1: // remove
				if(args != null && args.length >= 1 && args[0] instanceof String) {
					if(args.length >= 2 && args[1] instanceof String) {
						return ip.remove((String)args[0], (String)args[1]);
					} else {
						return ip.removeName((String)args[0]);
					}
				}
				break;
			case 2: // isName.
				if(args != null && args.length >= 1 && args[0] instanceof String) {
					return ip.isName((String)args[0]);
				}
				return ip.isName();
			case 3: // contains.
				if(args != null && args.length >= 1 && args[0] instanceof String) {
					if(args.length >= 2 && args[1] instanceof String) {
						return ip.isIpRange((String)args[0], (String)args[1]);
					} else {
						return ip.isIpRange((String)args[0]);
					}
				}
				break;
			case 4: // names.
				return ip.getNames();
			case 5: // list.
				if(args != null && args.length >= 1 && args[0] instanceof String) {
					return ip.getIpRanges((String)args[0]);
				}
				break;
			}
			return argsException(OBJECT_NAME);
		}

		@Override
		public final String getName() {
			switch (type) {
			case 0:
				return "add";
			case 1:
				return "remove";
			case 2:
				return "isName";
			case 3:
				return "contains";
			case 4:
				return "names";
			case 5:
				return "list";
			}
			return "unknown";
		}
	};
	
	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject(OBJECT_NAME,
		new RhiginFunction[]{ new Execute(0), new Execute(1), new Execute(2), new Execute(3), new Execute(4), new Execute(5) });

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
		scope.put(OBJECT_NAME, scope, IpPermissionObject.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(OBJECT_NAME, IpPermissionObject.getInstance());
	}

}
