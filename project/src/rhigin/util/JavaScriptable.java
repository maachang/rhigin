package rhigin.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Iterator;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;

public class JavaScriptable {
	// Mapオブジェクト変換.
	public static abstract class Map implements BlankScriptable {
		public abstract Object[] getIds();
		public abstract Object get(Object name);
		public abstract boolean containsKey(Object name);
		public abstract Object put(Object name, Object value);
		public abstract Object remove(Object name);
		public boolean has(String name, Scriptable start) {
			if(this.containsKey(name)) {
				return true;
			}
			return false;
		}
		public Object get(String name, Scriptable start) {
			if(this.containsKey(name)) {
				return this.get(name);
			}
			return Undefined.instance;
		}
		public void put(String name, Scriptable start, Object value) {
			this.put(name, value);
		}
		public void delete(String name) {
			this.remove(name);
		}
		public String getClassName() {
			return "jmap";
		}
	}
	
	// Listオブジェクト変換.
	@SuppressWarnings("rawtypes")
	public static abstract class List extends AbstractList implements BlankScriptable {
		private JListPushFunction pushFunc = null;
		public abstract int size();
		public abstract Object get(int no);
		public abstract boolean add(Object o);
		public abstract Object set(int no, Object o);
		public abstract Object remove(int no);
		public boolean has(int no, Scriptable start) {
			if(no >= 0 && this.size() > no) {
				return true;
			}
			return false;
		}
		public Object get(int no, Scriptable start) {
			if(no >= 0 && this.size() > no) {
				return this.get(no);
			}
			return Undefined.instance;
		}
		public void put(int no, Scriptable start, Object value) {
			final int len = (no - this.size()) + 1;
			if(len > 0) {
				for(int i = 0; i < len; i ++) {
					this.add(null);
				}
			}
			this.set(no, value);
		}
		public void delete(int no) {
			this.remove(no);
		}
		public boolean has(String name, Scriptable start) {
			if("length".equals(name) || "push".equals(name)) {
			  return true;
			}
			return false;
		}
		public Object get(String name, Scriptable start) {
			if("length".equals(name)) {
				return this.size();
			} else if("push".equals(name)) {
				if(pushFunc == null) {
					pushFunc = new JListPushFunction(this);
				}
				return pushFunc;
			}
			return Undefined.instance;
		}
		public Object[] getIds() {
			int len = this.size();
			Object[] ret = new Object[len];
			for(int i = 0; i < len; i ++) {
				ret[i] = this.get(i);
			}
			return ret;
		}
		public String getClassName() {
			return "jlist";
		}
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
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args.length >= 1) {
				int len = args.length;
				for(int i = 0; i < len; i ++) {
					srcList.add(args[i]);
				}
			}
			return Undefined.instance;
		}
	}
	
	// Map実装用.
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static class GetMap extends JavaScriptable.Map {
		private final java.util.Map srcMap ;
		public GetMap(java.util.Map m) {
			srcMap = m;
		}
		@Override
		public Object[] getIds() {
			int cnt = 0;
			final int len = srcMap.size();
			final Object[] ret = new Object[len];
			final Iterator it = srcMap.keySet().iterator();
			while(it.hasNext()) {
				ret[cnt++] = it.next();
			}
			return ret;
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
	}
	
	// List実装用.
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static class GetList extends JavaScriptable.List {
		private final java.util.List srcList;
		public GetList(java.util.List l) {
			srcList = l;
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
	}
	
	// 読み込み専用配列取得用.
	public static class ReadArray extends JavaScriptable.List {
		private final Object array;
		public ReadArray(Object a) {
			array = a;
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
			return null;
		}
		@Override
		public Object remove(int no) {
			return null;
		}
	}
}