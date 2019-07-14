package rhigin.scripts;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrapFactory;

@SuppressWarnings("rawtypes")
final class RhiginWrapFactory extends WrapFactory {
	private static final RhiginWrapFactory theInstance = new RhiginWrapFactory();
	private RhiginWrapFactory() {
		super.setJavaPrimitiveWrap(false);
	}
	static final WrapFactory getInstance() {
		return theInstance;
	}
	public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
									   Object javaObject, Class staticType) {
		final SecurityManager sm = System.getSecurityManager();
		final ClassShutter classShutter = RhiginClassShutter.getInstance();
		if (javaObject instanceof Map) {
			return new NativeJavaMap(scope, (Map)javaObject);
		} else if(javaObject instanceof List) {
			return new NativeJavaList(scope, (List)javaObject);
		} else if (javaObject instanceof ClassLoader) {
			return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
		} else {
			String name = null;
			if (javaObject instanceof Class) {
				name = ((Class) javaObject).getName();
			} else if (javaObject instanceof Member) {
				Member member = (Member) javaObject;
				if (sm != null && !Modifier.isPublic(member.getModifiers())) {
					return null;
				}
				name = member.getDeclaringClass().getName();
			}
			if (name != null) {
				if (!classShutter.visibleToScripts(name)) {
					return null;
				} else {
					return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
				}
			}
		}
		Class dynamicType = javaObject.getClass();
		String name = dynamicType.getName();
		if (!classShutter.visibleToScripts(name)) {
			Class type = null;
			if (staticType != null && staticType.isInterface()) {
				type = staticType;
			} else {
				while (dynamicType != null) {
					dynamicType = dynamicType.getSuperclass();
					name = dynamicType.getName();
					if (classShutter.visibleToScripts(name)) {
						type = dynamicType;
						break;
					}
				}
				assert type != null : "even java.lang.Object is not accessible?";
			}
			return new RhiginJavaObject(scope, javaObject, type);
		} else {
			return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
		}
	}
	// RhiginJavaObject.
	private static class RhiginJavaObject extends NativeJavaObject {
		private static final long serialVersionUID = 7074055700134775639L;
		RhiginJavaObject(Scriptable scope, Object obj, Class type) {
			super(scope, null, type);
			javaObject = obj;
		}
	}
	// Mapオブジェクト変換.
	@SuppressWarnings("unchecked")
	private static final class NativeJavaMap extends NativeJavaObject {
		private static final long serialVersionUID = 5049305964558857565L;
		private final Map srcMap;
		NativeJavaMap(Scriptable scope, Map map) {
			super(scope, map, Map.class);
			if(map == null) {
				throw new IllegalArgumentException();
			}
			srcMap = map;
		}
		public boolean has(String name, Scriptable start) {
			if(srcMap.containsKey(name)) {
			  return true;
			}
			return super.has(name, start);
		}  
		public Object get(String name, Scriptable start) {
			if(srcMap.containsKey(name)) {
				return srcMap.get(name);
			}
			return super.get(name, start);
		}
		public void put(String name, Scriptable start, Object value) {
			srcMap.put(name, value);
		}
		public void delete(String name) {
			srcMap.remove(name);
		}
		public Object[] getIds() {
			Object name;
			final Set set = new HashSet(srcMap.keySet());
			final Object[] ids = super.getIds();
			for(int i = 0; i < ids.length; i++) {
				if(set.contains(name = ids[i])) {
					set.add(name);
				}
			}
			return set.toArray(new Object[set.size()]);
		}
		public String getClassName() {
			return "javaMap";
		}
	}
	// Listオブジェクト変換.
	@SuppressWarnings("unchecked")
	private static final class NativeJavaList extends NativeJavaObject {
		private static final long serialVersionUID = -8289386466740351726L;
		private final List srcList;
		private ListPushFunction pushFunc = null;
		NativeJavaList(Scriptable scope, List list) {
			super(scope, list, List.class);
			if(list == null) {
				throw new IllegalArgumentException();
			}
			srcList = list;
		}
		public boolean has(int no, Scriptable start) {
			if(no >= 0 && srcList.size() > no) {
			  return true;
			}
			return super.has(no, start);
		}  
		public Object get(int no, Scriptable start) {
			if(no >= 0 && srcList.size() > no) {
				return srcList.get(no);
			}
			return super.get(no, start);
		}
		public void put(int no, Scriptable start, Object value) {
			final int len = (no - srcList.size()) + 1;
			if(len > 0) {
				for(int i = 0; i < len; i ++) {
					srcList.add(null);
				}
			}
			srcList.set(no, value);
		}
		public void delete(int no) {
			srcList.remove(no);
		}
		public boolean has(String name, Scriptable start) {
			if("length".equals(name) || "push".equals(name)) {
			  return true;
			}
			return super.has(name, start);
		}  
		public Object get(String name, Scriptable start) {
			if("length".equals(name)) {
				return srcList.size();
			} else if("push".equals(name)) {
				if(pushFunc == null) {
					pushFunc = new ListPushFunction(srcList);
				}
				return pushFunc;
			}
			return super.get(name, start);
		}
		public Object[] getIds() {
			int len = srcList.size();
			Object[] ret = new Object[len];
			for(int i = 0; i < len; i ++) {
				ret[i] = srcList.get(i);
			}
			return ret;
		}
		public String getClassName() {
			return "javaList";
		}
	}
	// List.pushファンクション用.
	@SuppressWarnings("unchecked")
	private static class ListPushFunction extends RhiginFunction {
		List srcList = null;
		ListPushFunction(List l) {
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
}
