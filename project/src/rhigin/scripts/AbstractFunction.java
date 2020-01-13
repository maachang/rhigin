package rhigin.scripts;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;

/**
 * rhigin用Abstractメソッド.
 */
public abstract class AbstractFunction implements Function {
	protected static final Object[] ZERO_ARRAY = new Object[] {};
	
	@Override
	public void delete(String arg0) {
	}

	@Override
	public void delete(int arg0) {
	}

	@Override
	public Object get(String arg0, Scriptable arg1) {
		return null;
	}

	@Override
	public Object get(int arg0, Scriptable arg1) {
		return null;
	}

	@Override
	public String getClassName() {
		return getName();
	}

	@Override
	public Object getDefaultValue(Class<?> clazz) {
		return (clazz == null || String.class.equals(clazz)) ? toString() : Undefined.instance;
	}

	@Override
	public Object[] getIds() {
		return ZERO_ARRAY;
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
		return false;
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		return false;
	}

	@Override
	public boolean hasInstance(Scriptable arg0) {
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

	/**
	 * Function の内容を実装する場合は、こちらを実装してください.
	 */
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		return Undefined.instance;
	}

	/**
	 * new XXX のようなオブジェクトを作成する場合には、こちらを実装します.
	 * また、戻り値は rhigin.scripts.objects.RhiginObject を利用すると、楽に作成できると思います.
	 */
	@Override
	public Scriptable construct(Context arg0, Scriptable arg1, Object[] arg2) {
		throw new RhiginException("This method '" + getName() +
			"' does not support instantiation.");
	}

	@Override
	public String toString() {
		return "function " + getName() + "() {\n  [native code]\n}";
	}
	
	/**
	 * function名を設定します.
	 */
	public String getName() {
		return "";
	}
	
	/**
	 * 引数エラーを返却.
	 */
	protected Object argsException() {
		return argsException(null);
	}

	/**
	 * 引数エラーを返却.
	 */
	protected Object argsException(String objName) {
		if (objName == null) {
			throw new RhiginException(500, "Insufficient arguments for " + getName() + ".");
		}
		throw new RhiginException(500, "Insufficient arguments for " + objName + "." + getName() + ".");
	}
}
