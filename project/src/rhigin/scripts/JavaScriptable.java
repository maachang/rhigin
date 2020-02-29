package rhigin.scripts;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.function.ToStringFunction;
import rhigin.util.ArrayMap;
import rhigin.util.BlankScriptable;
import rhigin.util.FixedArray;
import rhigin.util.ObjectList;

/**
 * javaオブジェクトを rhino の Scriptableに変更するオブジェクト.
 */
public class JavaScriptable {
	// Javascript用のMapオブジェクト変換.
	@SuppressWarnings("rawtypes")
	public static abstract class Map implements BlankScriptable, java.util.Map {
		protected final ToStringFunction.Execute toStringFunction = new ToStringFunction.Execute(this);

		/** 実装が必要な部分. **/
		
		@Override
		public abstract Object get(Object name);

		@Override
		public abstract boolean containsKey(Object name);

		@Override
		public abstract Object put(Object name, Object value);

		@Override
		public abstract Object remove(Object name);

		@Override
		public abstract Set keySet();
		
		@Override
		public abstract Set entrySet();
		
		@Override
		public abstract int size();
		
		@Override
		public boolean isEmpty() {
			return size() == 0;
		}
		
		/** 実装はどちらでも良い部分. **/
		
		@Override
		public void clear() {
		}
		
		@Override
		public boolean containsValue(Object arg0) {
			return false;
		}

		@Override
		public void putAll(java.util.Map arg0) {
		}

		@Override
		public Collection values() {
			return null;
		}
		
		/** Scriptable な部分. **/

		@Override
		public boolean has(String name, Scriptable start) {
			if (this.containsKey(name)) {
				return true;
			}
			return false;
		}

		@Override
		public Object get(String name, Scriptable start) {
			if (this.containsKey(name)) {
				return this.get(name);
			}
			return Undefined.instance;
		}

		@Override
		public void put(String name, Scriptable start, Object value) {
			this.put(name, value);
		}

		@Override
		public void delete(String name) {
			this.remove(name);
		}

		@Override
		public Object[] getIds() {
			if(this.size() == 0) {
				return ScriptConstants.BLANK_ARGS;
			}
			int cnt = 0;
			final int len = this.size();
			final Object[] ret = new Object[len];
			final Iterator it = this.keySet().iterator();
			while (it.hasNext()) {
				ret[cnt++] = it.next();
			}
			return ret;
		}

		@Override
		public String getClassName() {
			return "jmap";
		}

		@Override
		public Object getDefaultValue(Class<?> clazz) {
			return toStringFunction.getDefaultValue(clazz);
		}

		@Override
		public String toString() {
			return JsonOut.toString(this);
		}
	}

	// Javascript用のListオブジェクト変換.
	@SuppressWarnings("rawtypes")
	public static abstract class List extends AbstractList implements BlankScriptable {
		protected final ToStringFunction.Execute toStringFunction = new ToStringFunction.Execute(this);
		private JListPushFunction pushFunc = null;
		private JListSortFunction sortFunc = null;

		public abstract int size();

		public abstract Object get(int no);

		public abstract boolean add(Object o);

		public abstract Object set(int no, Object o);

		public abstract Object remove(int no);

		@Override
		public boolean has(int no, Scriptable start) {
			if (no >= 0 && this.size() > no) {
				return true;
			}
			return false;
		}

		@Override
		public Object get(int no, Scriptable start) {
			if (no >= 0 && this.size() > no) {
				return this.get(no);
			}
			return Undefined.instance;
		}

		@Override
		public void put(int no, Scriptable start, Object value) {
			final int len = (no - this.size()) + 1;
			if (len > 0) {
				for (int i = 0; i < len; i++) {
					this.add(null);
				}
			}
			this.set(no, value);
		}

		@Override
		public void delete(int no) {
			this.remove(no);
		}

		@Override
		public boolean has(String name, Scriptable start) {
			if ("length".equals(name) || "push".equals(name) || "sort".equals(name)) {
				return true;
			}
			return false;
		}

		@Override
		public Object get(String name, Scriptable start) {
			if ("length".equals(name)) {
				return this.size();
			} else if ("push".equals(name)) {
				if (pushFunc == null) {
					pushFunc = new JListPushFunction(this);
				}
				return pushFunc;
			} else if ("sort".equals(name)) {
				if(sortFunc == null) {
					sortFunc = new JListSortFunction(this);
				}
				return sortFunc;
			}
			return Undefined.instance;
		}

		@Override
		public Object[] getIds() {
			int len = this.size();
			Object[] ret = new Object[len];
			for (int i = 0; i < len; i++) {
				ret[i] = this.get(i);
			}
			return ret;
		}

		@Override
		public String getClassName() {
			return "jlist";
		}

		@Override
		public Object getDefaultValue(Class<?> clazz) {
			return toStringFunction.getDefaultValue(clazz);
		}

		@Override
		public String toString() {
			return JsonOut.toString(this);
		}
		
		/**
		 * ソート処理を実装.
		 */
		protected abstract void _sort();
	}

	// List.pushファンクション用.
	private static final class JListPushFunction extends RhiginFunction {
		JavaScriptable.List srcList = null;

		JListPushFunction(JavaScriptable.List l) {
			srcList = l;
		}

		@Override
		public String getName() {
			return "push";
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				int len = args.length;
				for (int i = 0; i < len; i++) {
					srcList.add(args[i]);
				}
			}
			return Undefined.instance;
		}
	}
	
	// List.sortファンクション用.
	private static final class JListSortFunction extends RhiginFunction {
		JavaScriptable.List srcList = null;
		JListSortFunction(JavaScriptable.List l) {
			srcList = l;
		}

		@Override
		public String getName() {
			return "sort";
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			srcList._sort();
			return Undefined.instance;
		}
	}

	// Map実装用.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static class GetMap extends JavaScriptable.Map {
		private final java.util.Map srcMap;

		public GetMap() {
			srcMap = new ArrayMap();
		}
		
		public GetMap(java.util.Map m) {
			srcMap = m;
		}

		@Override
		public void clear() {
			srcMap.clear();
		}

		@Override
		public Object get(Object name) {
			return srcMap.get(name);
		}

		@Override
		public boolean containsKey(Object name) {
			return srcMap.containsKey(name);
		}

		@Override
		public Object put(Object name, Object value) {
			return srcMap.put(name, value);
		}

		@Override
		public Object remove(Object name) {
			return srcMap.remove(name);
		}
		
		@Override
		public int size() {
			return srcMap.size();
		}

		@Override
		public Set keySet() {
			return srcMap.keySet();
		}

		@Override
		public boolean isEmpty() {
			return srcMap.isEmpty();
		}

		@Override
		public Collection<Object> values() {
			return srcMap.values();
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
		public void putAll(java.util.Map arg0) {
			srcMap.putAll(arg0);
		}
		
		@Override
		public String getClassName() {
			return "WrapMap";
		}
		
		public java.util.Map rawData() {
			return srcMap;
		}
	}

	// List実装用.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class GetList extends JavaScriptable.List {
		private final java.util.List srcList;

		public GetList() {
			srcList = new ObjectList();
		}
		
		public GetList(java.util.List l) {
			if(l instanceof FixedArray) {
				srcList = new ObjectList(((FixedArray)l).rawData());
			} else {
				srcList = l;
			}
		}

		@Override
		public void clear() {
			srcList.clear();
		}

		@Override
		public int size() {
			return srcList.size();
		}

		@Override
		public Object get(int no) {
			return srcList.get(no);
		}

		@Override
		public boolean add(Object o) {
			return srcList.add(o);
		}

		@Override
		public Object set(int no, Object o) {
			return srcList.set(no, o);
		}

		@Override
		public Object remove(int no) {
			return srcList.remove(no);
		}

		@Override
		public boolean isEmpty() {
			return srcList.isEmpty();
		}

		@Override
		public String getClassName() {
			return "WrapList";
		}
		
		@Override
		protected void _sort() {
			Collections.sort(srcList);
		}
	}

	// 読み込み専用配列取得用.
	public static class ReadArray extends JavaScriptable.List implements RhiginObjectWrapper {
		private boolean objeactArrayFlag = false;
		private Object array;

		@SuppressWarnings("rawtypes")
		public ReadArray(Object a) {
			if(a == null) {
				objeactArrayFlag = true;
				array = new Object[0];
			} else if(a instanceof FixedArray) {
				objeactArrayFlag = true;
				array = ((FixedArray)a).rawData();
			} else if(a.getClass().isArray()) {
				if(a instanceof Object[]) {
					objeactArrayFlag = true;
				}
				array = a;
			} else {
				objeactArrayFlag = true;
				array = new Object[] {a};
			}
		}

		@Override
		public int size() {
			return Array.getLength(array);
		}

		@Override
		public Object get(int no) {
			return Array.get(array, no);
		}

		@Override
		public boolean add(Object o) {
			return false;
		}

		@Override
		public Object set(int no, Object o) {
			Object ret = Array.get(array, no);
			Array.set(array, no, o);
			return ret;
		}

		@Override
		public Object remove(int no) {
			return null;
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public String getClassName() {
			return "ReadArray";
		}
		
		@Override
		public Object unwrap() {
			return array;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void _sort() {
			if(!objeactArrayFlag) {
				Collections.sort(this);
			} else {
				java.util.Arrays.sort((Object[])this.array);
			}
		}
	}
}