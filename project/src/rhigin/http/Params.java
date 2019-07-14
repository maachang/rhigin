package rhigin.http;

import rhigin.scripts.JavaScriptable;
import rhigin.util.BlankMap;
import rhigin.util.ConvertGet;
import rhigin.util.ListMap;

/**
 * Httpパラメータ.
 */
public class Params extends JavaScriptable.Map implements BlankMap, ConvertGet<Object> {
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
		try {
			String[] list = map.names();
			int len = list.length;
			StringBuilder buf = new StringBuilder("{");
			for(int i = 0; i < len; i ++) {
				if(i != 0) {
					buf.append(", ");
				}
				buf.append("\"").append(list[i]).append("\": \"").append(get(list[i])).append("\"");
			}
			return buf.append("}").toString();
		} catch(Exception e) {
		}
		return "";
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
}
