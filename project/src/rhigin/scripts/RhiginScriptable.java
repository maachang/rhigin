package rhigin.scripts;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import rhigin.util.ArrayMap;

final class RhiginScriptable implements Scriptable {
	private static final String RHINO_JS_PACKAGE_NAME = "org.mozilla.javascript";
	private Map<Object, Object> _indexedProps;
	private RhiginContext context;
	private Scriptable prototype;
	private Scriptable parent;

	RhiginScriptable(RhiginContext c) {
		this(c, null);
	}

	RhiginScriptable(RhiginContext c, Map<Object, Object> indexedProps) {
		this.context = (c == null) ? new RhiginContext() : c;
		this._indexedProps = indexedProps;
	}

	public RhiginContext getContext() {
		return context;
	}
	
	private Map<Object, Object> getIndexProps() {
		if(_indexedProps == null) {
			//_indexedProps = new HashMap<Object, Object>();
			_indexedProps = new ArrayMap<Object, Object>();
		}
		return _indexedProps;
	}

	public String getClassName() {
		return "Global";
	}

	@SuppressWarnings("rawtypes")
	public Object get(String name, Scriptable start) {
		if (name.length() == 0) {
			if (getIndexProps().containsKey(name)) {
				return getIndexProps().get(name);
			} else {
				return NOT_FOUND;
			}
		} else {
			Class c;
			boolean[] has = new boolean[1];
			final Object value = context.getHasAttribute(has, name);
			if (value == null || value == Undefined.instance) {
				if(!has[0]) {
					return NOT_FOUND;
				}
				return value;
			} else if ((c = value.getClass()).isArray()) {
				return value;
			} else if (c.getPackage().getName().startsWith(RHINO_JS_PACKAGE_NAME)) {
				return value;
			} else {
				return Context.javaToJS(value, this);
			}
		}
	}

	public Object get(int index, Scriptable start) {
		if (getIndexProps().containsKey(index)) {
			return getIndexProps().get(index);
		} else {
			return NOT_FOUND;
		}
	}

	public boolean has(String name, Scriptable start) {
		if (name.length() == 0) {
			return getIndexProps().containsKey(name);
		} else {
			return context.hasAttribute(name);
		}
	}

	public boolean has(int index, Scriptable start) {
		return getIndexProps().containsKey(index);
	}

	public void put(String name, Scriptable start, Object value) {
		if (start == this) {
			if (name.length() == 0) {
				getIndexProps().put(name, value);
			} else {
				context.setAttribute(name, jsToJava(value));
			}
		} else {
			start.put(name, start, value);
		}
	}

	public void put(int index, Scriptable start, Object value) {
		if (start == this) {
			getIndexProps().put(index, value);
		} else {
			start.put(index, start, value);
		}
	}

	public void delete(String name) {
		if (name.length() == 0) {
			getIndexProps().remove(name);
		} else {
			context.removeAttribute(name);
		}
	}

	public void delete(int index) {
		getIndexProps().remove(index);
	}

	public Scriptable getPrototype() {
		return prototype;
	}

	public void setPrototype(Scriptable prototype) {
		this.prototype = prototype;
	}

	public Scriptable getParentScope() {
		return parent;
	}

	public void setParentScope(Scriptable parent) {
		this.parent = parent;
	}

	public Object[] getIds() {
		String[] keys = getAllKeys();
		int size = keys.length + getIndexProps().size();
		Object[] res = new Object[size];
		System.arraycopy(keys, 0, res, 0, keys.length);
		int i = keys.length;
		for (Object index : getIndexProps().keySet()) {
			res[i++] = index;
		}
		return res;
	}

	@SuppressWarnings("rawtypes")
	public Object getDefaultValue(Class typeHint) {
		for (int i = 0; i < 2; i++) {
			boolean tryToString;
			if (typeHint == ScriptRuntime.StringClass) {
				tryToString = (i == 0);
			} else {
				tryToString = (i == 1);
			}

			String methodName;
			Object[] args;
			if (tryToString) {
				methodName = "toString";
				args = ScriptRuntime.emptyArgs;
			} else {
				methodName = "valueOf";
				args = new Object[1];
				String hint;
				if (typeHint == null) {
					hint = "undefined";
				} else if (typeHint == ScriptRuntime.StringClass) {
					hint = "string";
				} else if (typeHint == ScriptRuntime.ScriptableClass) {
					hint = "object";
				} else if (typeHint == ScriptRuntime.FunctionClass) {
					hint = "function";
				} else if (typeHint == ScriptRuntime.BooleanClass || typeHint == Boolean.TYPE) {
					hint = "boolean";
				} else if (typeHint == ScriptRuntime.NumberClass || typeHint == ScriptRuntime.ByteClass
						|| typeHint == Byte.TYPE || typeHint == ScriptRuntime.ShortClass || typeHint == Short.TYPE
						|| typeHint == ScriptRuntime.IntegerClass || typeHint == Integer.TYPE
						|| typeHint == ScriptRuntime.FloatClass || typeHint == Float.TYPE
						|| typeHint == ScriptRuntime.DoubleClass || typeHint == Double.TYPE) {
					hint = "number";
				} else {
					throw Context.reportRuntimeError("Invalid JavaScript value of type " + typeHint.toString());
				}
				args[0] = hint;
			}
			Object v = ScriptableObject.getProperty(this, methodName);
			if (!(v instanceof Function))
				continue;
			Function fun = (Function) v;
			Context cx = ContextFactory.getGlobal().enterContext();
			try {
				v = fun.call(cx, fun.getParentScope(), this, args);
			} finally {
				Context.exit();
			}
			if (v != null) {
				if (!(v instanceof Scriptable)) {
					return v;
				}
				if (typeHint == ScriptRuntime.ScriptableClass || typeHint == ScriptRuntime.FunctionClass) {
					return v;
				}
				if (tryToString && v instanceof Wrapper) {
					// Let a wrapped java.lang.String pass for a primitive
					// string.
					Object u = ((Wrapper) v).unwrap();
					if (u instanceof String)
						return u;
				}
			}
		}
		String arg = (typeHint == null) ? "undefined" : typeHint.getName();
		throw Context.reportRuntimeError("Cannot find default value for object " + arg);
	}

	public boolean hasInstance(Scriptable instance) {
		Scriptable proto = instance.getPrototype();
		while (proto != null) {
			if (proto.equals(this))
				return true;
			proto = proto.getPrototype();
		}
		return false;
	}

	private String[] getAllKeys() {
		Object[] keys = context.getIds();
		String[] ret = new String[keys.length];
		System.arraycopy(keys, 0, ret, 0, keys.length);
		return ret;
	}

	// js用に変換されたjavaのオブジェクトをアンラップ.
	private static final Object jsToJava(Object jsObj) {
		if (jsObj instanceof Wrapper) {
			final Wrapper njb = (Wrapper) jsObj;
			if (njb instanceof NativeJavaClass) {
				return njb;
			}
			final Object obj = njb.unwrap();
			if (obj instanceof Number || obj instanceof String ||
				obj instanceof Boolean || obj instanceof Character) {
				return njb;
			} else {
				return obj;
			}
		} else {
			return jsObj;
		}
	}
}
