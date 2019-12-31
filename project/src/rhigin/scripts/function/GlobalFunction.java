package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginFunction;

/**
 * ExecuteScript.currentRhiginContextを取得.
 * 
 * global("xxxx");
 * 
 * or
 * 
 * global.xxxx;
 */
public class GlobalFunction extends RhiginFunction {
	private static final GlobalFunction THIS = new GlobalFunction();

	public static final GlobalFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "global";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		return ExecuteScript.currentRhiginContext();
	}
	
	@Override
	public void delete(String arg0) {
		ExecuteScript.currentRhiginContext().delete(arg0);
	}

	@Override
	public void delete(int arg0) {
		ExecuteScript.currentRhiginContext().delete(arg0);
	}

	@Override
	public Object get(String arg0, Scriptable arg1) {
		return ExecuteScript.currentRhiginContext().get(arg0, arg1);
	}

	@Override
	public Object get(int arg0, Scriptable arg1) {
		return ExecuteScript.currentRhiginContext().get(arg0, arg1);
	}

	@Override
	public String getClassName() {
		return ExecuteScript.currentRhiginContext().getClassName();
	}

	@Override
	public Object getDefaultValue(Class<?> arg0) {
		return (arg0 == null || String.class.equals(arg0)) ? toString() : Undefined.instance;
	}

	@Override
	public Object[] getIds() {
		return ExecuteScript.currentRhiginContext().getIds();
	}

	@Override
	public Scriptable getParentScope() {
		return ExecuteScript.currentRhiginContext().getParentScope();
	}

	@Override
	public Scriptable getPrototype() {
		return ExecuteScript.currentRhiginContext().getPrototype();
	}

	@Override
	public boolean has(String arg0, Scriptable arg1) {
		return ExecuteScript.currentRhiginContext().has(arg0, arg1);
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		return ExecuteScript.currentRhiginContext().has(arg0, arg1);
	}

	@Override
	public boolean hasInstance(Scriptable arg0) {
		return ExecuteScript.currentRhiginContext().hasInstance(arg0);
	}

	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {
		if(arg2 == THIS) {
			return;
		}
		ExecuteScript.currentRhiginContext().put(arg0, arg1, arg2);
	}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {
		ExecuteScript.currentRhiginContext().put(arg0, arg1, arg2);
	}

	@Override
	public void setParentScope(Scriptable arg0) {
		ExecuteScript.currentRhiginContext().setParentScope(arg0);
	}

	@Override
	public void setPrototype(Scriptable arg0) {
		ExecuteScript.currentRhiginContext().setPrototype(arg0);
	}
	
	@Override
	public String toString() {
		return "[" + getName() + "]";
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("global", scope, GlobalFunction.getInstance());
	}
}
