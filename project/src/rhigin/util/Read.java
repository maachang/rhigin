package rhigin.util;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

/**
 * 読み込み専用オブジェクト.
 */
public class Read {
	/**
	 * 読み込み専用のList.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final class Arrays extends AbstractList<Object> implements ConvertGet<Integer> {
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
			Object o = listMode ? ((List)srcList).get(index) :
				((Object[])srcList)[index];
			if(o instanceof Map) {
				return new Read.Maps((Map)o);
			} else if(o instanceof List) {
				return new Read.Arrays((List)o);
			}
			return o;
		}
		@Override
		public int size() {
			return listMode ? ((List)srcList).size() :
				((Object[])srcList).length;
		}
		@Override
		public Object getOriginal(Integer n) {
			if(n == null) {
				return get(-1);
			}
			return get(n);
		}
	}
	
	/**
	 * 読み込み専用のMap.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final class Maps implements BlankMap, ConvertGet<String> {
		Map<String,Object> srcMap = null;
		public Maps(Map<String,Object> srcMap) {
			this.srcMap = srcMap;
		}
		@Override
		public Object get(Object name) {
			if(name != null) {
				Object o = srcMap.get(name);
				if(o instanceof Map) {
					return new Read.Maps((Map)o);
				} else if(o instanceof List) {
					return new Read.Arrays((List)o);
				}
				return o;
			}
			return null;
		}
		@Override
		public boolean containsKey(Object name) {
			if(name != null) {
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
	}
}
