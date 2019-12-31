package rhigin.scripts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

/**
 * rhiginContet.
 */
public class RhiginContext implements Scriptable {
	private Map<String, Object> bindings = new HashMap<String, Object>();
	private Scriptable prototype = null;
	private Scriptable parentScope = null;

	public boolean hasAttribute(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		return bindings.containsKey(name);
	}

	public Object getAttribute(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		return bindings.get(name);
	}

	public void setAttribute(String name, Object value) {
		if (name == null) {
			throw new NullPointerException();
		}
		bindings.put(name, value);
	}

	public void removeAttribute(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		bindings.remove(name);
	}

	public Iterator<String> keys() {
		return bindings.keySet().iterator();
	}

	public int size() {
		return bindings.size();
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
		return RhiginContext.class.getName();
	}

	@Override
	public Object getDefaultValue(Class<?> arg0) {
		return (arg0 == null || String.class.equals(arg0)) ? toString() : Undefined.instance;
	}

	@Override
	public Object[] getIds() {
		String n;
		int cnt = 0;
		int len = size();
		Object[] ret = new Object[len - 1];
		Iterator<String> it = keys();
		while(it.hasNext()) {
			n = it.next();
			if("global".equals(n)) {
				continue;
			}
			ret[cnt++] = n;
		}
		return ret;
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
		return false;
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
