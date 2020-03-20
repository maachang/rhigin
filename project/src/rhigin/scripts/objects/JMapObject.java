package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.JavaScriptable;
import rhigin.scripts.RhiginFunction;
import rhigin.util.ArrayMap;
import rhigin.util.FixedKeyValues;

/**
 * [js]Java用Arrayオブジェクト.
 */
public class JMapObject extends RhiginFunction {
	public static final String OBJECT_NAME = "JMap";
	private static final JMapObject THIS = new JMapObject();

	public static final JMapObject getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return OBJECT_NAME;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Scriptable jconstruct(Context ctx, Scriptable thisObject, Object[] args) {
		return new JavaScriptable.GetMap(new ArrayMap(args));
	}

	// コールさせない.
	@Override
	public Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		return Undefined.instance;
	}

	@Override
	public boolean hasInstance(Scriptable instance) {
		if(instance != null) {
			String n = instance.getClassName();
			return instance instanceof JavaScriptable.Map ||
				"Map".equals(n) || "JMap".equals(n);
		}
		return false;
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put(OBJECT_NAME, scope, JMapObject.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(OBJECT_NAME, JMapObject.getInstance());
	}
}
