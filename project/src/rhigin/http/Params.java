package rhigin.http;

import java.util.Map;
import java.util.Set;

import rhigin.scripts.JavaScriptable;
import rhigin.util.AndroidMap;
import rhigin.util.ConvertGet;

/**
 * Httpパラメータ.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Params extends JavaScriptable.Map implements ConvertGet<Object> {
	private Map map;

	public Params() {
		this.map = new AndroidMap();
	}

	public Params(Map map) {
		this.map = map;
	}

	@Override
	public Object get(Object name) {
		if (name != null) {
			return map.get("" + name);
		}
		return null;
	}

	@Override
	public boolean containsKey(Object name) {
		if (name != null) {
			return map.containsKey("" + name);
		}
		return false;
	}

	@Override
	public Object put(Object name, Object value) {
		if (name != null) {
			return map.put("" + name, value);
		}
		return null;
	}

	@Override
	public Object remove(Object name) {
		if (name != null) {
			return map.remove("" + name);
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
		if (map instanceof AndroidMap) {
			AndroidMap m = (AndroidMap)map;
			int len = m.size();
			if(len == 0) {
				return new Object[] {};
			}
			Object[] ret = new Object[len];
			for(int i = 0; i < len; i ++) {
				ret[i] = m.keyAt(i);
			}
			return ret;
		} else {
			return map.keySet().toArray();
		}
	}

	@Override
	public Object getOriginal(Object n) {
		return get(n);
	}

	@Override
	public Set keySet() {
		return map.keySet();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}
}
