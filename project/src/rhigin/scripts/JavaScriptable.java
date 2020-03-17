package rhigin.scripts;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.util.AbstractEntryIterator;
import rhigin.util.AbstractKeyIterator;
import rhigin.util.ArrayMap;
import rhigin.util.Converter;
import rhigin.util.FixedArray;
import rhigin.util.OList;
import rhigin.util.ObjectList;

/**
 * javaオブジェクトを rhino の Scriptableに変更するオブジェクト.
 */
public class JavaScriptable {
	
	// Javascript用のMapオブジェクト変換.
	@SuppressWarnings("rawtypes")
	public static abstract class Map implements RhinoScriptable, java.util.Map {
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
			if ("length".equals(name) || JsMapFunction.name(name) != -1 || this.containsKey(name)) {
				return true;
			}
			return false;
		}

		@Override
		public Object _get(String name, Scriptable start) {
			int no;
			if("length".equals(name)) {
				return size();
			}
			if ((no = JsMapFunction.name(name)) != -1) {
				JsMapFunction f = new JsMapFunction();
				return f.type(this, no);
			} else if (this.containsKey(name)) {
				return this.get(name);
			}
			return Undefined.instance;
		}
		
		@Override
		public Object _get(int no, Scriptable start) {
			return Undefined.instance;
		}

		@Override
		public void _put(String name, Scriptable start, Object value) {
			this.put(name, value);
		}
		
		@Override
		public void _put(int no, Scriptable start, Object value) {
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
		public String toString() {
			return JsonOut.toString(this);
		}
	}

	// Javascript用のListオブジェクト変換.
	@SuppressWarnings("rawtypes")
	public static abstract class List extends AbstractList implements RhinoScriptable {
		@Override
		public abstract int size();

		@Override
		public abstract Object get(int no);

		@Override
		public abstract boolean add(Object o);
		
		@Override
		public abstract void add(int index, Object element);

		@Override
		public abstract Object set(int no, Object o);

		@Override
		public abstract Object remove(int no);

		@Override
		public boolean has(int no, Scriptable start) {
			if (no >= 0 && this.size() > no) {
				return true;
			}
			return false;
		}

		@Override
		public Object _get(int no, Scriptable start) {
			if (no >= 0 && this.size() > no) {
				return this.get(no);
			}
			return Undefined.instance;
		}

		@Override
		public void _put(int no, Scriptable start, Object value) {
			final int len = (no - this.size()) + 1;
			if (len > 0) {
				for (int i = 0; i < len; i++) {
					this.add(null);
				}
			}
			this.set(no, value);
		}
		
		@Override
		public void _put(String name, Scriptable start, Object value) {
		}
		
		@Override
		public void delete(int no) {
			this.remove(no);
		}

		@Override
		public boolean has(String name, Scriptable start) {
			if ("length".equals(name) || JsListFunction.name(name) != -1) {
				return true;
			}
			return false;
		}

		@Override
		public Object _get(String name, Scriptable start) {
			if ("length".equals(name)) {
				return this.size();
			}
			final int type = JsListFunction.name(name);
			if(type == -1) {
				return Undefined.instance;
			}
			JsListFunction f = new JsListFunction();
			return f.type(this, type);
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
		public String toString() {
			return JsonOut.toString(this);
		}
		
		/**
		 * ソート処理を実装.
		 */
		protected abstract void _sort();
	}
	
	// js 向けの iterator リストオブジェクト.
	public static interface JsIteratorList {
		public boolean hasNext();
		public Object next();
	}
	
	// js 向けの iterator
	@SuppressWarnings("rawtypes")
	public static final class JsIterator implements RhinoScriptable, Iterator {
		protected int listType;
		protected Object list;
		protected int nowCount;
		protected JsIteratorValue jsValue;
		
		public JsIterator(Object list) {
			if(list instanceof java.util.Iterator) {
				this.listType = 0; // iterator.
			} else if(list instanceof java.util.List) {
				this.listType = 1; // List.
			} else if(list.getClass().isArray()) {
				this.listType = 2; // Array.
			} else if(list instanceof JsIteratorList) {
				this.listType = 3; // jsIteratorList.
			} else {
				this.listType = -1; // unknown.
				throw new RhiginWrapException("Invalid object set.");
			}
			this.list = list;
			this.nowCount = 0;
			this.jsValue = new JsIteratorValue();
		}
		
		@Override
		public boolean has(String name, Scriptable parent) {
			return JsIteratorFunction.name(name) != -1;
		}

		@Override
		public Object _get(String name, Scriptable parent) {
			final int type = JsIteratorFunction.name(name);
			if(type == -1) {
				return Undefined.instance;
			}
			JsIteratorFunction f = new JsIteratorFunction();
			return f.type(this, type);
		}

		@Override
		public Object _get(int no, Scriptable parent) {
			return Undefined.instance;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Object[] getIds() {
			switch(listType) {
			case 0: // iterator.
			{
				OList ret = new OList();
				Iterator it = (Iterator)list;
				while(it.hasNext()) {
					ret.add(it.next());
				}
				jsValue.done();
				return ret.getArray();
			}
			case 1: // list.
			{
				int cnt = 0;
				java.util.List lst = (java.util.List)list;
				int len = lst.size() - nowCount;
				Object[] ret = new Object[len];
				for(int i = nowCount; i < len; i ++) {
					ret[cnt ++] = lst.get(i);
				}
				jsValue.done();
				nowCount = lst.size();
				return ret;
			}
			case 2: // array.
			{
				int cnt = 0;
				int len = Array.getLength(list) - nowCount;
				Object[] ret = new Object[len];
				for(int i = nowCount; i < len; i ++) {
					ret[cnt ++] = Array.get(list, i);
				}
				jsValue.done();
				nowCount = Array.getLength(list);
				return ret;
			}
			case 3: // JsIteratorList.
			{
				OList ret = new OList();
				Iterator it = (Iterator)list;
				while(it.hasNext()) {
					ret.add(it.next());
				}
				jsValue.done();
				return ret.getArray();
			}
			}
			return ScriptConstants.BLANK_ARGS;
		}
		
		@Override
		public boolean hasNext() {
			switch(listType) {
			case 0: // iterator.
			{
				return ((Iterator)list).hasNext();
			}
			case 1: // list.
			{
				return ((java.util.List)list).size() > nowCount + 1;
			}
			case 2: // array.
			{
				return Array.getLength(list)> nowCount + 1;
			}
			case 3: // JsIteratorList.
			{
				return ((JsIteratorList)list).hasNext();
			}
			}
			return false;
		}
		
		@Override
		public JsIteratorValue next() {
			switch(listType) {
			case 0: // iterator.
			{
				Iterator it = (Iterator)list;
				if(it.hasNext()) {
					jsValue.value(it.next());
				} else {
					jsValue.done();
				}
				break;
			}
			case 1: // list.
			{
				java.util.List lst = (java.util.List)list;
				if(lst.size() > nowCount + 1) {
					jsValue.value(lst.get(nowCount ++));
				} else {
					jsValue.done();
				}
				break;
			}
			case 2: // array.
			{
				if(Array.getLength(list)> nowCount + 1) {
					jsValue.value(Array.get(list, nowCount ++));
				} else {
					jsValue.done();
				}
				break;
			}
			case 3: // JsIteratorList.
			{
				JsIteratorList it = (JsIteratorList)list;
				if(it.hasNext()) {
					jsValue.value(it.next());
				} else {
					jsValue.done();
				}
				break;

			}
			}
			return jsValue;
		}
		
		@Override
		public String toString() {
			return JsonOut.toString(this);
		}

		@Override
		public void _put(String name, Scriptable obj, Object value) {
		}

		@Override
		public void _put(int no, Scriptable obj, Object value) {
		}
	}
	
	// js用 Iterator value.
	@SuppressWarnings("rawtypes")
	public static final class JsIteratorValue extends JavaScriptable.Map
		implements AbstractKeyIterator.Base, AbstractEntryIterator.Base {
		protected static final Object[] IDS = new Object[] {
			"value", "done"
		};
		protected Object value;
		protected boolean done;
		
		JsIteratorValue() {}
		
		public void value(Object v) {
			value = v;
			done = false;
		}
		
		public void done() {
			value = Undefined.instance;
			done = true;
		}

		@Override
		public Object[] getIds() {
			return IDS;
		}

		@Override
		public Object get(Object name) {
			if(IDS[0].equals(name)) {
				if(done) {
					return Undefined.instance;
				}
				return value;
			} else if(IDS[1].equals(name)) {
				return done;
			}
			return null;
		}

		@Override
		public boolean containsKey(Object name) {
			int len = IDS.length;
			for(int i = 0; i < len; i ++) {
				if(IDS[i].equals(name)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Object remove(Object name) {
			return null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Set keySet() {
			return new AbstractKeyIterator.Set(this);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Set entrySet() {
			return new AbstractEntryIterator.Set(this);
		}
		
		@Override
		public int size() {
			return IDS.length;
		}

		@Override
		public Object getKey(int no) {
			return IDS[no];
		}

		@Override
		public Object getValue(int no) {
			return get(IDS[no]);
		}

		@Override
		public void _put(String name, Scriptable obj, Object value) {
		}

		@Override
		public void _put(int no, Scriptable obj, Object value) {
		}

		@Override
		public Object put(Object name, Object value) {
			return null;
		}
	}
	
	// js 向けの iterator メソッド.
	public static final class JsIteratorFunction extends RhiginFunction {
		protected int type;
		protected JsIterator jsIterator;
		
		private static final String[] NAMES = new String[] {
			"next"
		};
		
		JsIteratorFunction() {
		}
		
		@Override
		public void clear() {
			type = -1;
			jsIterator = null;
		}
		
		public JsIteratorFunction type(JsIterator jsIterator, int t) {
			this.jsIterator = jsIterator;
			type = t;
			return this;
		}
		
		public static final int name(String name) {
			if(name == null || name.isEmpty()) {
				return -1;
			}
			final int ret = Arrays.binarySearch(NAMES, name);
			return ret >= 0 ? ret : -1;
		}
		
		@Override
		public String getName() {
			return NAMES[type];
		}
		
		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch(type) {
			case 0: // next.
				return jsIterator.next();
			}
			return Undefined.instance;
		}
	}
	
	// Map 用のjavascriptファンクション用.
	private static final class JsMapFunction extends RhiginFunction {
		int type;
		JavaScriptable.Map srcMap;
		
		private static final String[] NAMES = new String[] {
			"keys"
			,"toString"
			,"values"
		};
		
		JsMapFunction() {
		}
		
		@Override
		public final void clear() {
			srcMap = null;
			type = -1;
		}
		
		public final JsMapFunction type(JavaScriptable.Map m, int t) {
			srcMap = m;
			type = t;
			return this;
		}

		public static final int name(String name) {
			if(name == null || name.isEmpty()) {
				return -1;
			}
			final int ret = Arrays.binarySearch(NAMES, name);
			return ret >= 0 ? ret : -1;
		}

		@Override
		public final String getName() {
			return NAMES[type];
		}

		@Override
		@SuppressWarnings("rawtypes")
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch(type) {
			case 0: // keys.
			{
				return new JsIterator(srcMap.keySet().iterator());
			}
			case 1: // toString.
			{
				return JsonOut.toString(srcMap);
			}
			case 2: // values.
			{
				return new JsIterator(new JsIteratorList() {
					final Iterator it = srcMap.entrySet().iterator();
					@Override
					public boolean hasNext() {
						return it.hasNext();
					}
					@Override
					public Object next() {
						Entry e = (Entry)it.next();
						return e.getValue();
					}
				});
			}
			
			}
			return Undefined.instance;
		}
	}
	
	// List 用のjavascriptファンクション用.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final class JsListFunction extends RhiginFunction {
		int type;
		JavaScriptable.List srcList;
		
		private static final String[] NAMES = new String[] {
			"concat"
			,"entries"
			,"every"
			,"fill"
			,"filter"
			,"find"
			,"findIndex"
			,"flat"
			,"flatMap"
			,"forEach"
			,"includes"
			,"indexOf"
			,"join"
			,"keys"
			,"lastIndexOf"
			,"map"
			,"pop"
			,"push"
			,"reduce"
			,"reduceRight"
			,"reverse"
			,"shift"
			,"some"
			,"sort"
			,"splice"
			,"toString"
			,"unshift"
			,"values"
		};

		JsListFunction() {
		}
		
		@Override
		public final void clear() {
			srcList = null;
			type = -1;
		}
		
		public final JsListFunction type(JavaScriptable.List l, int t) {
			srcList = l;
			type = t;
			return this;
		}

		public static final int name(String name) {
			if(name == null || name.isEmpty()) {
				return -1;
			}
			final int ret = Arrays.binarySearch(NAMES, name);
			return ret >= 0 ? ret : -1;
		}

		@Override
		public final String getName() {
			return NAMES[type];
		}
		
		// 指定値がマイナスの場合はlength - 指定値を返却する.
		private static final int _position(java.util.List srcList, int p) {
			if(p < 0) {
				p = srcList.size() - p;
				return p < 0 ? 0 : p;
			}
			return p >= srcList.size() ? 0 : p;
		}
		
		// 戻り値のBoolean判定.
		private static final boolean _bool(Object o) {
			if(o == null || Undefined.isUndefined(o) || (o instanceof String && ((String)o).isEmpty())) {
				return false;
			}
			return Converter.convertBool(o);
		}
		
		// every処理.
		private static final boolean _every(java.util.List srcList, Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args != null && args.length >= 1 && args[0] instanceof Function) {
				Scriptable parent = thisObj;
				if(args.length >= 2 && args[1] instanceof Scriptable) {
					parent = (Scriptable)args[1];
				}
				Object res;
				Function f = (Function)args[0];
				int len = srcList.size();
				Object[] fArgs = new Object[] {null, -1, srcList};
				for(int i = 0; i < len; i ++) {
					fArgs[0] = srcList.get(i);
					fArgs[1] = i;
					res = f.call(ctx, scope, parent, fArgs);
					if(_bool(res) == false) {
						return false;
					}
				}
			}
			return true;
		}
		
		// filter処理.
		private static final java.util.List _filter(java.util.List srcList, Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args != null && args.length >= 1 && args[0] instanceof Function) {
				Scriptable parent = thisObj;
				if(args.length >= 2 && args[1] instanceof Scriptable) {
					parent = (Scriptable)args[1];
				}
				Object res;
				java.util.List ret = new ObjectList();
				Function f = (Function)args[0];
				int len = srcList.size();
				Object[] fArgs = new Object[] {null, -1, srcList};
				for(int i = 0; i < len; i ++) {
					fArgs[0] = srcList.get(i);
					fArgs[1] = i;
					res = f.call(ctx, scope, parent, fArgs);
					if(_bool(res) == true) {
						ret.add(srcList.get(i));
					}
				}
				return ret;
			}
			return new ObjectList();
		}
		
		// find処理.
		private static final int _find(java.util.List srcList, Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args != null && args.length >= 1 && args[0] instanceof Function) {
				Scriptable parent = thisObj;
				if(args.length >= 2 && args[1] instanceof Scriptable) {
					parent = (Scriptable)args[1];
				}
				Object res;
				Function f = (Function)args[0];
				int len = srcList.size();
				Object[] fArgs = new Object[] {null, -1, srcList};
				for(int i = 0; i < len; i ++) {
					fArgs[0] = srcList.get(i);
					fArgs[1] = i;
					res = f.call(ctx, scope, parent, fArgs);
					if(_bool(res) == true) {
						return i;
					}
				}
			}
			return -1;
		}
		
		// flat処理.
		private static final void _flat(java.util.List out, int now, int max, java.util.List nowList) {
			Object o;
			int len = nowList.size();
			for(int i = 0; i < len; i ++) {
				o = nowList.get(i);
				if(o instanceof java.util.List) {
					if(now + 1 >= max) {
						_flat(out, now + 1, max, (java.util.List)o);
					} else {
						out.add(o);
					}
				} else {
					out.add(o);
				}
			}
		}

		// forEach処理.
		private static final void _forEach(java.util.List srcList, Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args != null && args.length >= 1 && args[0] instanceof Function) {
				Scriptable parent = thisObj;
				if(args.length >= 2 && args[1] instanceof Scriptable) {
					parent = (Scriptable)args[1];
				}
				Function f = (Function)args[0];
				int len = srcList.size();
				Object[] fArgs = new Object[] {null, -1, srcList};
				for(int i = 0; i < len; i ++) {
					fArgs[0] = srcList.get(i);
					fArgs[1] = i;
					f.call(ctx, scope, parent, fArgs);
				}
			}
		}

		// map処理.
		private static final java.util.List _map(java.util.List srcList, Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args != null && args.length >= 1 && args[0] instanceof Function) {
				Scriptable parent = thisObj;
				if(args.length >= 2 && args[1] instanceof Scriptable) {
					parent = (Scriptable)args[1];
				}
				Object res;
				java.util.List ret = new ObjectList();
				Function f = (Function)args[0];
				int len = srcList.size();
				Object[] fArgs = new Object[] {null, -1, srcList};
				for(int i = 0; i < len; i ++) {
					fArgs[0] = srcList.get(i);
					fArgs[1] = i;
					res = f.call(ctx, scope, parent, fArgs);
					ret.add(res);
				}
				return ret;
			}
			return new ObjectList();
		}
		
		// reduce処理.
		private static final Object _reduce(java.util.List srcList, Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args != null && args.length >= 1 && args[0] instanceof Function) {
				if(srcList.size() == 0) {
					return Undefined.instance;
				}
				int p = 1;
				Object initVal = srcList.get(0);
				if(args.length >= 2) {
					initVal = args[1];
					p = 0;
				}
				Object res = null;
				Function f = (Function)args[0];
				int len = srcList.size();
				Object[] fArgs = new Object[] {initVal, null, -1, srcList};
				for(int i = p; i < len; i ++) {
					fArgs[1] = srcList.get(i);
					fArgs[2] = i;
					res = f.call(ctx, scope, thisObj, fArgs);
					fArgs[0] = res;
				}
				return res;
			}
			return Undefined.instance;
		}
		
		// reduceRight処理.
		private static final Object _reduceRight(java.util.List srcList, Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args != null && args.length >= 1 && args[0] instanceof Function) {
				if(srcList.size() == 0) {
					return Undefined.instance;
				}
				int len = srcList.size() - 1;
				Object initVal = srcList.get(len);
				if(args.length >= 2) {
					initVal = args[1];
					len += 1;
				}
				Object res = null;
				Function f = (Function)args[0];
				Object[] fArgs = new Object[] {initVal, null, -1, srcList};
				for(int i = len-1; i >= 0; i --) {
					fArgs[1] = srcList.get(i);
					fArgs[2] = i;
					res = f.call(ctx, scope, thisObj, fArgs);
					fArgs[0] = res;
				}
				return res;
			}
			return Undefined.instance;
		}

		// some処理.
		private static final boolean _some(java.util.List srcList, Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args != null && args.length >= 1 && args[0] instanceof Function) {
				Scriptable parent = thisObj;
				if(args.length >= 2 && args[1] instanceof Scriptable) {
					parent = (Scriptable)args[1];
				}
				Object res;
				Function f = (Function)args[0];
				int len = srcList.size();
				Object[] fArgs = new Object[] {null, -1, srcList};
				for(int i = 0; i < len; i ++) {
					fArgs[0] = srcList.get(i);
					fArgs[1] = i;
					res = f.call(ctx, scope, parent, fArgs);
					if(_bool(res) == true) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch(type) {
			case 0: // concat.
			{
				int len = srcList.size();
				java.util.List ret = new ObjectList();
				for(int i = 0; i < len; i ++) {
					ret.add(srcList.get(i));
				}
				if (args != null && args.length >= 1) {
					int j, lenJ;
					len = args.length;
					Object o = null;
					java.util.List lst = null;
					for(int i = 0; i < len; i ++) {
						o = args[i];
						if(o instanceof java.util.List) {
							lst = (java.util.List)o;
							lenJ = lst.size();
							for(j = 0; j < lenJ; j ++) {
								ret.add(lst.get(j));
							}
						} else {
							ret.add(o);
						}
					}
				}
				return ret;
			}
			case 1: // entries.
			{
				return new JsIterator(new JsIteratorList() {
					final java.util.List list = srcList;
					int count = 0;
					@Override
					public boolean hasNext() {
						return count < list.size();
					}
					@Override
					public Object next() {
						return new JavaScriptable.ReadArray(
							new Object[] {count, list.get(count ++)});
					}
				});
			}
			case 2: // every.
			{
				return _every(srcList, ctx, scope, thisObj, args);
			}
			case 3: // fill.
			{
				if (args != null && args.length >= 1) {
					int len = srcList.size();
					Object value = args[0];
					int start = args.length >= 2 && Converter.isNumeric(args[1]) ?
						_position(srcList, Converter.convertInt(args[1])) : 0;
					int end = args.length >= 3 && Converter.isNumeric(args[2]) ?
						_position(srcList, Converter.convertInt(args[2])) : len;
					for(int i = start; i < end; i ++) {
						if(i >= 0 && i < len) {
							srcList.set(i, value);
						}
					}
				}
				break;
			}
			case 4: // filter.
			{
				return _filter(srcList, ctx, scope, thisObj, args);
			}
			case 5: // find.
			{
				return _find(srcList, ctx, scope, thisObj, args);
			}
			case 6: // findIndex.
			{
				int n = _find(srcList, ctx, scope, thisObj, args);
				if(n == -1) {
					return Undefined.instance;
				}
				return srcList.get(n);
			}
			case 7: // flat.
			{
				int next = 1;
				if(args != null && args.length >= 1 && Converter.isNumeric(args[0])) {
					next = Converter.convertInt(args[0]);
					if(next <= 0) {
						next = 1;
					}
				}
				java.util.List ret = new ObjectList();
				_flat(ret, 0, next, srcList);
				return ret;
			}
			case 8: // flatMap.
			{
				java.util.List map = _map(srcList, ctx, scope, thisObj, args);
				java.util.List ret = new ObjectList();
				_flat(ret, 0, 1, map);
				return ret;
			}
			case 9: // forEach.
			{
				_forEach(srcList, ctx, scope, thisObj, args);
				break;
			}
			case 10: // includes.
			{
				if(args != null && args.length >= 1) {
					Object val = args[0];
					int p = 0;
					if(args.length >= 2 && Converter.isNumeric(args[1])) {
						p = _position(srcList, Converter.convertInt(args[1]));
					}
					int len = srcList.size();
					if(val == null) {
						for(int i = p; i < len; i ++) {
							if(srcList.get(i) == null) {
								return true;
							}
						}
					} else if(Undefined.isUndefined(val)) {
						for(int i = p; i < len; i ++) {
							if(Undefined.isUndefined(srcList.get(i))) {
								return true;
							}
						}
					} else {
						for(int i = p; i < len; i ++) {
							if(val.equals(srcList.get(i))) {
								return true;
							}
						}
					}
				}
				return false;
			}
			case 11: // indexOf.
			{
				if (args != null && args.length >= 1) {
					int p = 0;
					Object value = args[0];
					if(args.length >= 2 && Converter.isNumeric(args[1])) {
						p = _position(srcList, Converter.convertInt(args[1]));
					}
					int len = srcList.size();
					if(value == null) {
						for(int i = p; i < len; i ++) {
							if(srcList.get(i) == null) {
								return i;
							}
						}
					} else if(Undefined.isUndefined(value)) {
						for(int i = p; i < len; i ++) {
							if(Undefined.isUndefined(srcList.get(i))) {
								return i;
							}
						}
					} else {
						for(int i = p; i < len; i ++) {
							if(value.equals(srcList.get(i))) {
								return i;
							}
						}
					}
				}
				return -1;
			}
			case 12: // join.
			{
				StringBuilder buf = new StringBuilder();
				int len = srcList.size();
				String c = args != null && args.length >= 1 ? "" + args[0] : "";
				for(int i = 0; i < len; i ++) {
					if(i != 0) {
						buf.append(c);
					}
					buf.append(srcList.get(i));
				}
				return buf.toString();
			}
			case 13: // keys.
			{
				return new JsIterator(new JsIteratorList() {
					final java.util.List list = srcList;
					int count = 0;
					@Override
					public boolean hasNext() {
						return count < list.size();
					}
					@Override
					public Object next() {
						return count ++;
					}
				});

			}
			case 14: // lastIndexOf.
			{
				if (args != null && args.length >= 1) {
					int p = srcList.size();
					Object value = args[0];
					if(args.length >= 2 && Converter.isNumeric(args[1])) {
						p = _position(srcList, Converter.convertInt(args[1]));
					}
					if(value == null) {
						for(int i = p - 1; i >= 0; i --) {
							if(srcList.get(i) == null) {
								return i;
							}
						}
					} else if(Undefined.isUndefined(value)) {
						for(int i = p - 1; i >= 0; i --) {
							if(Undefined.isUndefined(srcList.get(i))) {
								return i;
							}
						}
					} else {
						for(int i = p - 1; i >= 0; i --) {
							if(value.equals(srcList.get(i))) {
								return i;
							}
						}
					}
				}
				return -1;
			}
			case 15: // map.
			{
				return _map(srcList, ctx, scope, thisObj, args);
			}
			case 16: // pop.
			{
				return srcList.size() == 0 ? Undefined.instance : srcList.remove(srcList.size() - 1);
			}
			case 17: // push.
			{
				if (args != null && args.length >= 1) {
					int len = args.length;
					for (int i = 0; i < len; i++) {
						srcList.add(args[i]);
					}
				}
				return srcList.size();
			}
			case 18: // reduce.
			{
				return _reduce(srcList, ctx, scope, thisObj, args);
			}
			case 19: // reduceRight.
			{
				return _reduceRight(srcList, ctx, scope, thisObj, args);
			}
			case 20: // reverse.
			{
				int n;
				Object o;
				int len = srcList.size();
				int hlen = len >> 1;
				for(int i = 0; i < hlen; i ++) {
					n = len - (i + 1);
					o = srcList.get(i);
					srcList.set(i, srcList.get(n));
					srcList.set(n, o);
				}
				return srcList;
			}
			case 21: // shift.
			{
				return srcList.size() == 0 ? Undefined.instance : srcList.remove(0);
			}
			case 22: // some.
			{
				return _some(srcList, ctx, scope, thisObj, args);
			}
			case 23: // sort.
			{
				srcList._sort();
				break;
			}
			case 24: // splice.
			{
				java.util.List ret = new ObjectList();
				if(args != null && args.length >= 2 &&
					Converter.isNumeric(args[0]) && Converter.isNumeric(args[1])) {
					int no = _position(srcList, Converter.convertInt(args[0]));
					int len = _position(srcList, Converter.convertInt(args[1]));
					for(int i = 0; i < len; i ++) {
						ret.add(srcList.remove(no));
					}
					len = args.length - 2;
					for(int i = 0; i < len; i ++) {
						srcList.add(no, args[args.length - (i+1)]);
					}
				}
				return ret;
			}
			case 25: // toString.
			{
				return JsonOut.toString(srcList);
			}
			case 26: // unshift.
			{
				if(args != null && args.length >= 1) {
					int len = args.length;
					for(int i = len-1; i >= 0; i --) {
						srcList.add(0, args[i]);
					}
				}
				return srcList.size();
			}
			case 27: // values.
			{
				return new JsIterator(new JsIteratorList() {
					final java.util.List list = srcList;
					int count = 0;
					@Override
					public boolean hasNext() {
						return count < list.size();
					}
					@Override
					public Object next() {
						return list.get(count ++);
					}
				});
			}
			
			}
			return Undefined.instance;
		}
	}


	// [js]Map.
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

	// [js]List.
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
		public void add(int no, Object o) {
			srcList.add(no, o);
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

	// [js]配列.
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
		public void add(int no, Object o) {
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