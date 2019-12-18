package rhigin.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 読み込み専用オブジェクト.
 */
public class Read {
	/**
	 * 読み込み専用のList.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final class Arrays extends AbstractList implements ConvertGet<Integer> {
		private boolean listMode = false;
		private Object srcList = null;

		public Arrays(List<Object> srcList) {
			this.srcList = srcList;
			this.listMode = true;
		}

		public Arrays(Object[] srcList) {
			this.srcList = srcList;
			this.listMode = false;
		}

		@Override
		public Object get(int index) {
			Object o = listMode ? ((List) srcList).get(index) : ((Object[]) srcList)[index];
			if (o instanceof Map) {
				return new Read.Maps((Map) o);
			} else if (o instanceof List) {
				return new Read.Arrays((List) o);
			}
			return o;
		}

		@Override
		public int size() {
			return listMode ? ((List) srcList).size() : ((Object[]) srcList).length;
		}

		@Override
		public Object getOriginal(Integer n) {
			if (n == null) {
				return get(-1);
			}
			return get(n);
		}

		@Override
		public boolean add(Object o) {
			return false;
		}

		@Override
		public Object set(int no, Object o) {
			return null;
		}

		@Override
		public Object remove(int no) {
			return null;
		}
	}

	/**
	 * 読み込み専用のMap.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final class Maps implements Map<String, Object>, ConvertGet<String> {
		Map<String, Object> srcMap = null;

		public Maps(Map<String, Object> srcMap) {
			this.srcMap = srcMap;
		}

		@Override
		public Object get(Object name) {
			if (name != null) {
				Object o = srcMap.get(name);
				if (o instanceof Map) {
					return new Read.Maps((Map) o);
				} else if (o instanceof List) {
					return new Read.Arrays((List) o);
				}
				return o;
			}
			return null;
		}

		@Override
		public boolean containsKey(Object name) {
			if (name != null) {
				return srcMap.containsKey(name);
			}
			return false;
		}

		@Override
		public int size() {
			return srcMap.size();
		}

		@Override
		public String toString() {
			return srcMap.toString();
		}

		@Override
		public Object getOriginal(String n) {
			return get(n);
		}

		@Override
		public Object put(String name, Object value) {
			return null;
		}

		@Override
		public Object remove(Object name) {
			return null;
		}

		@Override
		public void clear() {
		}

		@Override
		public boolean containsValue(Object arg0) {
			return srcMap.containsValue(arg0);
		}

		@Override
		public Set<Entry<String, Object>> entrySet() {
			return srcMap.entrySet();
		}

		@Override
		public boolean isEmpty() {
			return srcMap.isEmpty();
		}

		@Override
		public Set<String> keySet() {
			return srcMap.keySet();
		}

		@Override
		public void putAll(Map<? extends String, ? extends Object> arg0) {
		}

		@Override
		public Collection<Object> values() {
			return srcMap.values();
		}
	}
}
