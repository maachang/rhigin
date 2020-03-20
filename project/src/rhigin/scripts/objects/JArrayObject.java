package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.JavaScriptable;
import rhigin.scripts.RhiginFunction;
import rhigin.util.FixedKeyValues;
import rhigin.util.ObjectList;

/**
 * [js]Java用Arrayオブジェクト.
 */
public class JArrayObject extends RhiginFunction {
	public static final String OBJECT_NAME = "JArray";
	private static final JArrayObject THIS = new JArrayObject();

	public static final JArrayObject getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return OBJECT_NAME;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Scriptable jconstruct(Context ctx, Scriptable thisObject, Object[] args) {
		return new JavaScriptable.GetList(new ObjectList(args));
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
			return instance instanceof JavaScriptable.List ||
				"Array".equals(n) || "JArray".equals(n);
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
		scope.put(OBJECT_NAME, scope, JArrayObject.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(OBJECT_NAME, JArrayObject.getInstance());
	}
}
