package rhigin.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * ArrayMap.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ArrayMap implements Map<String, Object>, ConvertGet<String> {
	private ListMap list;

	/**
	 * コンストラクタ.
	 */
	public ArrayMap() {
		this(null);
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

	public Set entrySet() {
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
			buf.append("\"").append(v[0]).append("\": \"").append(v[1])
					.append("\"");
		}
		return buf.append("}").toString();
	}

	public Collection values() {
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

	private static class ArrayMapIterator implements Iterator<String> {
		private ListMap list;
		private int nowPos;
		private String target;

		protected ArrayMapIterator(ListMap list) {
			this.list = list;
			this.nowPos = -1;
		}

		private boolean getNext() {
			if (target == null) {
				nowPos++;
				if (list.size() > nowPos) {
					target = (String)list.rawData().get(nowPos)[0];
					return true;
				}
				return false;
			}
			return true;
		}

		public boolean hasNext() {
			return getNext();
		}

		public String next() {
			if (getNext() == false) {
				throw new NoSuchElementException();
			}
			String ret = target;
			target = null;
			return ret;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	static class ArrayMapSet implements Set<String> {
		private ListMap list;

		protected ArrayMapSet(ListMap list) {
			this.list = list;
		}

		public int size() {
			return list.size();
		}

		public boolean isEmpty() {
			return list.size() == 0;
		}

		public boolean contains(Object o) {
			if (o == null) {
				return false;
			}
			return list.containsKey(o.toString());
		}

		public Iterator<String> iterator() {
			return new ArrayMapIterator(list);
		}

		public Object[] toArray() {
			int len = list.size();
			Object[] ret = new Object[len];
			OList<Object[]> n = list.rawData();
			for (int i = 0; i < len; i++) {
				ret[i] = n.get(i)[0];
			}
			return ret;
		}

		@SuppressWarnings("hiding")
		public <Object> Object[] toArray(Object[] a) {
			return null;
		}

		public boolean add(String e) {
			return false;
		}

		public boolean remove(Object o) {
			return false;
		}

		public boolean containsAll(Collection<?> c) {
			return false;
		}

		public boolean addAll(Collection<? extends String> c) {
			return false;
		}

		public boolean retainAll(Collection<?> c) {
			return false;
		}

		public boolean removeAll(Collection<?> c) {
			return false;
		}

		public void clear() {
			list.clear();
		}

		public boolean equals(Object o) {
			return this.equals(o);
		}

		public int hashCode() {
			return -1;
		}
	}

	public Set keySet() {
		return new ArrayMapSet(list);
	}

	// original 取得.
	@Override
	public Object getOriginal(String n) {
		return get(n);
	}
}
