package rhigin.scripts;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import rhigin.util.AndroidMap;

/**
 * Javascript用Map.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JsMap extends JavaScriptable.Map {
	private Map srcMap;
	/**
	 * コンストラクタ.
	 */
	public JsMap() {
		srcMap = new AndroidMap();
	}

	/**
	 * コンストラクタ.
	 */
	public JsMap(final Map v) {
		srcMap = v;
	}

	/**
	 * コンストラクタ.
	 */
	public JsMap(final Object... args) {
		srcMap = new AndroidMap();
		final int len = args == null ? 0 : args.length;
		for(int i = 0; i < len; i += 2) {
			srcMap.put(args[i], args[i+1]);
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
