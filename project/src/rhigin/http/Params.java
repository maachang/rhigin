package rhigin.http;

import java.util.Set;

import rhigin.scripts.JavaScriptable;
import rhigin.util.AbstractKeyIterator;
import rhigin.util.ConvertGet;
import rhigin.util.ListMap;

/**
 * Httpパラメータ.
 */
@SuppressWarnings("rawtypes")
public class Params extends JavaScriptable.Map implements AbstractKeyIterator.Base<String>, ConvertGet<Object> {
	private ListMap map = new ListMap();

	@Override
	public Object get(Object name) {
		if(name != null) {
			return map.get("" + name);
		}
		return null;
	}

	@Override
	public boolean containsKey(Object name) {
		if(name != null) {
			return map.containsKey("" + name);
		}
		return false;
	}

	@Override
	public Object put(Object name, Object value) {
		if(name != null) {
			Object ret = map.put("" + name, value);
			if(ret != null) {
				return ret;
			}
		}
		return null;
	}

	@Override
	public Object remove(Object name) {
		if(name != null) {
			Object ret = map.remove("" + name);
			if(ret != null) {
				return ret;
			}
		}
		return null;
	}
	
	@Override
	public int size() {
		return map.size();
	}
	
	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public Object[] getIds() {
		String[] names = map.names();
		if(names == null) {
			return new Object[] {};
		}
		Object[] ret = new Object[names.length];
		System.arraycopy(names, 0 , ret, 0, names.length);
		return ret;
	}

	@Override
	public Object getOriginal(Object n) {
		return get(n);
	}

	@Override
	public String getKey(int no) {
		return (String)map.rawData().get(no)[0];
	}

	@Override
	public Set keySet() {
		return new AbstractKeyIterator.KeyIteratorSet<>(this);
	}
}
