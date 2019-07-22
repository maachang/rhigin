package rhigin.http;

import java.util.Map;
import java.util.Set;

import rhigin.scripts.JavaScriptable;
import rhigin.util.AbstractKeyIterator;
import rhigin.util.ArrayMap;
import rhigin.util.ConvertGet;

/**
 * Httpパラメータ.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Params extends JavaScriptable.Map implements AbstractKeyIterator.Base<String>, ConvertGet<Object> {
	private Map map;
	private Object[] keyList = null;
	
	public Params() {
		this(new ArrayMap());
	}
	
	public Params(Map map) {
		this.map = map;
	}

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
			keyList = null;
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
			keyList = null;
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
		if(map instanceof ArrayMap) {
			String[] names = ((ArrayMap) map).getListMap().names();
			if(names == null) {
				return new Object[] {};
			}
			Object[] ret = new Object[names.length];
			System.arraycopy(names, 0 , ret, 0, names.length);
			return ret;
		} else {
			if(keyList == null) {
				keyList = map.keySet().toArray();
			}
			if(keyList == null || keyList.length == 0) {
				return new Object[] {};
			}
			Object[] ret = new Object[keyList.length];
			System.arraycopy(keyList, 0 , ret, 0, keyList.length);
			return ret;
		}
	}

	@Override
	public Object getOriginal(Object n) {
		return get(n);
	}

	@Override
	public String getKey(int no) {
		if(map instanceof ArrayMap) {
			return (String)((ArrayMap) map).getListMap().rawData().get(no)[0];
		} else {
			if(keyList == null) {
				keyList = map.keySet().toArray();
			}
			return (String)keyList[no];
		}
	}

	@Override
	public Set keySet() {
		return new AbstractKeyIterator.KeyIteratorSet<>(this);
	}
}
