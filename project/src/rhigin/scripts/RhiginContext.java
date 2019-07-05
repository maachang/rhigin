package rhigin.scripts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RhiginContext {
	private Map<String, Object> bindings = new HashMap<String,Object>();
	
	public boolean hasAttribute(String name) {
		if(name == null) {
			throw new NullPointerException();
		}
		return bindings.containsKey(name);
	}
	public Object getAttribute(String name) {
		if(name == null) {
			throw new NullPointerException();
		}
		return bindings.get(name);
	}
	public void setAttribute(String name, Object value) {
		if(name == null) {
			throw new NullPointerException();
		}
		bindings.put(name, value);
	}
	public void removeAttribute(String name) {
		if(name == null) {
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
}
