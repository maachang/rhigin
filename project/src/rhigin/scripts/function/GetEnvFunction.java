package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.util.EnvCache;
import rhigin.util.FixedKeyValues;

/**
 * [js]環境変数を読み込む.
 * 
 * env("RHIGIN_HOME");
 * 
 * or
 * 
 * env.RHIGIN_HOME;
 */
public class GetEnvFunction extends RhiginFunction {
	private static final GetEnvFunction THIS = new GetEnvFunction();

	public static final GetEnvFunction getInstance() {
		return THIS;
	}

	@Override
	public String getName() {
		return "env";
	}

	@Override
	public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (args.length >= 1) {
			return EnvCache.get("" + args[0]);
		}
		return argsException();
	}
	
	@Override
	public void delete(String arg0) {
	}

	@Override
	public void delete(int arg0) {
	}

	@Override
	public Object get(String arg0, Scriptable arg1) {
		return EnvCache.get(arg0);
	}

	@Override
	public Object get(int arg0, Scriptable arg1) {
		return null;
	}

	@Override
	public String getClassName() {
		return GetEnvFunction.class.getName();
	}

	@Override
	public Object getDefaultValue(Class<?> arg0) {
		return (arg0 == null || String.class.equals(arg0)) ? toString() : Undefined.instance;
	}

	@Override
	public Object[] getIds() {
		return new Object[0];
	}

	@Override
	public Scriptable getParentScope() {
		return null;
	}

	@Override
	public Scriptable getPrototype() {
		return null;
	}

	@Override
	public boolean has(String arg0, Scriptable arg1) {
		return EnvCache.get(arg0) == null ? false : true;
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		return false;
	}

	@Override
	public boolean hasInstance(Scriptable instance) {
		if(instance != null) {
			return this.getClassName().equals(instance.getClassName());
		}
		return false;
	}

	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {
	}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {
	}

	@Override
	public void setParentScope(Scriptable arg0) {
	}

	@Override
	public void setPrototype(Scriptable arg0) {
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
		scope.put("env", scope, GetEnvFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("env", GetEnvFunction.getInstance());
	}
}