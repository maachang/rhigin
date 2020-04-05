package rhigin.scripts;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import rhigin.util.ArrayMap;

/**
 * Javascript用Map.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JsMap extends JavaScriptable.Map {
	private Map srcMap;

	public JsMap(final Object... args) {
		set(args);
	}
	
	public void set(final Object... args) {
		if(args == null || args.length == 0) {
			srcMap = new ArrayMap();
		} else if(args[0] instanceof Map) {
			srcMap = (Map)args[0];
		} else {
			srcMap = new ArrayMap();
			final int len = args == null ? 0 : args.length;
			for(int i = 0; i < len; i += 2) {
				srcMap.put(args[i], args[i+1]);
			}
		}
	}
	
	@Override
	public void clear() {
		srcMap.clear();
	}

	@Override
	public Object put(Object name, Object value) {
		if(name == null) {
			return null;
		}
		return srcMap.put(name, value);
	}

	@Override
	public boolean containsKey(Object key) {
		return srcMap.containsKey(key);
	}

	@Override
	public Object get(Object key) {
		return srcMap.get(key);
	}

	@Override
	public Object remove(Object key) {
		return srcMap.remove(key);
	}

	@Override
	public boolean isEmpty() {
		return srcMap.isEmpty();
	}

	@Override
	public void putAll(Map toMerge) {
		srcMap.putAll(toMerge);
	}

	@Override
	public boolean containsValue(Object value) {
		return srcMap.containsValue(value);
	}

	@Override
	public int size() {
		return srcMap.size();
	}

	@Override
	public String toString() {
		return JsonOut.toString(srcMap);
	}

	@Override
	public Collection values() {
		return srcMap.values();
	}

	@Override
	public Set keySet() {
		return srcMap.keySet();
	}

	@Override
	public Set entrySet() {
		return srcMap.entrySet();
	}
}
