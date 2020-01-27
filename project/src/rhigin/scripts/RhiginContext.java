package rhigin.scripts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.util.FixedKeyValues;

/**
 * rhiginContet.
 */
public class RhiginContext implements Scriptable {
	private FixedKeyValues<String, Object> baseFunctions = null;
	private Map<String, Object> bindings = new HashMap<String, Object>();
	private Scriptable prototype = null;
	private Scriptable parentScope = null;
	
	public void setBaseFunctions(FixedKeyValues<String, Object> bf) {
		baseFunctions = bf;
	}

	public boolean hasAttribute(String name) {
		if (name == null) {
			throw new NullPointerException();
		} else if(bindings.containsKey(name)) {
			return true;
		}
		return baseFunctions != null && baseFunctions.containsKey(name);
	}

	public Object getAttribute(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		if(bindings.containsKey(name)) {
			return bindings.get(name);
		} else if(baseFunctions != null && baseFunctions.containsKey(name)) {
			return baseFunctions.get(name);
		}
		return Undefined.instance;
	}
	
	public Object getHasAttribute(boolean[] has, String name) {
		if(name == null) {
			throw new NullPointerException();
		}
		if(bindings.containsKey(name)) {
			has[0] = true;
			return bindings.get(name);
		} else if(baseFunctions != null && baseFunctions.containsKey(name)) {
			has[0] = true;
			return baseFunctions.get(name);
		}
		has[0] = false;
		return Undefined.instance;
	}

	public void setAttribute(String name, Object value) {
		if (name == null) {
			throw new NullPointerException();
		}
		// baseFunctionsにある情報は上書き出来ない.
		if(baseFunctions != null && baseFunctions.containsKey(name)) {
			return;
		} else {
			bindings.put(name, value);
		}
	}

	public void removeAttribute(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		// baseFunctionsにある情報は削除しない.
		if(baseFunctions == null || !baseFunctions.containsKey(name)) {
			bindings.remove(name);
		}
	}

//	public Iterator<String> keys() {
//		return bindings.keySet().iterator();
//	}
	
	public int size() {
		return bindings.size() + (baseFunctions == null ? 0 : baseFunctions.size());
	}
	
	@Override
	public void delete(String arg0) {
		removeAttribute(arg0);
	}

	@Override
	public void delete(int arg0) {
	}

	@Override
	public Object get(String arg0, Scriptable arg1) {
		return getAttribute(arg0);
	}

	@Override
	public Object get(int arg0, Scriptable arg1) {
		return null;
	}

	@Override
	public String getClassName() {
		return "RhiginContext";
	}

	@Override
	public Object getDefaultValue(Class<?> arg0) {
		return (arg0 == null || String.class.equals(arg0)) ? toString() : Undefined.instance;
	}

	@Override
	public Object[] getIds() {
		String n;
		Object[] ret;
		int cnt = 0;
		int len = bindings.size();
		Iterator<String> itr = bindings.keySet().iterator();
		if(baseFunctions == null) {
			ret = new String[len];
			while(itr.hasNext()) {
				n = itr.next();
				ret[cnt ++] = n;
			}
			return ret;
		} else {
			ret = new String[len + baseFunctions.size()];
			while(itr.hasNext()) {
				n = itr.next();
				ret[cnt ++] = n;
			}
			System.arraycopy(baseFunctions.keys(), 0, ret, cnt, baseFunctions.size());
			return ret;
		}
	}

	@Override
	public Scriptable getParentScope() {
		return parentScope;
	}

	@Override
	public Scriptable getPrototype() {
		return prototype;
	}

	@Override
	public boolean has(String arg0, Scriptable arg1) {
		return hasAttribute(arg0);
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		return false;
	}

	@Override
	public boolean hasInstance(Scriptable arg0) {
		return this.getClassName().equals(arg0.getClassName());
	}

	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {
		if(arg2 == this) {
			return;
		}
		setAttribute(arg0, arg2);
	}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {
	}

	@Override
	public void setParentScope(Scriptable arg0) {
		parentScope = arg0;
	}

	@Override
	public void setPrototype(Scriptable arg0) {
		prototype = arg0;
	}
	
	@Override
	public String toString() {
		return "[global]";
	}
}
