package rhigin.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * ArrayMap.
 */
@SuppressWarnings("rawtypes")
public class ArrayMap implements AbstractKeyIterator.Base<String>, Map<String, Object>, ConvertGet<String> {
	private ListMap list;

	/**
	 * コンストラクタ.
	 */
	public ArrayMap() {
		list = new ListMap();
	}

	/**
	 * コンストラクタ.
	 */
	public ArrayMap(ListMap list) {
		if (list == null) {
			list = new ListMap();
		}
		this.list = list;
	}

	/**
	 * コンストラクタ.
	 */
	public ArrayMap(final Map<String, Object> v) {
		list = new ListMap();
		list.set(v);
	}

	/**
	 * コンストラクタ.
	 */
	public ArrayMap(final Object... args) {
		list = new ListMap();
		list.set(args);
	}

	/**
	 * クリア.
	 */
	public void clear() {
		list.clear();
	}

	public Object put(String name, Object value) {
		if (name == null || value == null) {
			return null;
		}
		return list.put(name.toString(), value);
	}

	public boolean containsKey(Object key) {
		if (key == null) {
			return false;
		}
		return list.containsKey(key.toString());
	}

	public Object get(Object key) {
		if (key == null) {
			return null;
		}
		return list.get(key.toString());
	}

	public Object remove(Object key) {
		if (key == null) {
			return null;
		}
		return list.remove(key.toString());
	}

	public boolean isEmpty() {
		return list.size() == 0;
	}

	public void putAll(Map toMerge) {
		if (toMerge == null) {
			return;
		}
		Iterator it = toMerge.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			Object value = toMerge.get(key);
			if (key != null && value != null) {
				put(key.toString(), value);
			}
		}
	}

	public boolean containsValue(Object value) {
		OList<Object[]> n = list.rawData();
		if (value == null) {
			int len = n.size();
			for (int i = 0; i < len; i++) {
				if (n.get(i)[1] == null) {
					return true;
				}
			}
		} else {
			int len = n.size();
			for (int i = 0; i < len; i++) {
				if (value.equals(n.get(i)[1])) {
					return true;
				}
			}
		}
		return false;
	}

	public Set<Entry<String,Object>> entrySet() {
		return null;
	}

	public int size() {
		return list.size();
	}

	public String toString() {
		Object[] v;
		OList<Object[]> n = list.rawData();
		StringBuilder buf = new StringBuilder();
		int len = n.size();
		buf.append("{");
		for (int i = 0; i < len; i++) {
			v = n.get(i);
			if (i != 0) {
				buf.append(",");
			}
			buf.append("\"").append(v[0]).append("\": \"").append(v[1]).append("\"");
		}
		return buf.append("}").toString();
	}

	public Collection<Object> values() {
		ArrayList<Object> ret = new ArrayList<Object>();
		OList<Object[]> n = list.rawData();
		int len = n.size();
		for (int i = 0; i < len; i++) {
			ret.add(n.get(i)[1]);
		}
		return ret;
	}

	public ListMap getListMap() {
		return list;
	}

	public Set<String> keySet() {
		return new AbstractKeyIterator.KeyIteratorSet<>(this);
	}

	// original 取得.
	@Override
	public Object getOriginal(String n) {
		return get(n);
	}

	@Override
	public String getKey(int no) {
		return (String) list.rawData().get(no)[0];
	}

	public void set(final Map<String, Object> v) {
		list.set(v);
	}

	public void set(final Object... args) {
		list.set(args);
	}
}
