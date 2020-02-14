package rhigin.scripts.objects;

import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import rhigin.scripts.ExecuteScript;
import rhigin.scripts.JavaScriptable;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.scripts.RhiginObjectWrapper;
import rhigin.scripts.RhiginWrapUtil;
import rhigin.util.FastReflect;
import rhigin.util.FixedKeyValues;

/**
 * Javaリフレクションオブジェクト.
 */
public class JavaObject {
	public static final String OBJECT_NAME = "Java";
	
	// 取得されたオブジェクトを変換.
	private static final Object getObject(Object value) {
		boolean[] res = new boolean[1];
		value = RhiginWrapUtil.wrap(res, value);
		if(!res[0]) {
			return new JReflectObject(null, value);
		}
		return value;
	}
	
	// Javaリフレクション制御用オブジェクト.
	public static final class JRefrect extends RhiginFunction {
		private final int type;

		JRefrect(int t) {
			type = t;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch(type) {
			case 0: // create.
			{
				if(args == null || args.length == 0 || args[0] == null) {
					this.argsException(OBJECT_NAME);
				}
				Object o = null;
				int len = args.length;
				if(len == 1) {
					o = args[0] instanceof Class ?
						FastReflect.newInstance(null, (Class)args[0]) :
						FastReflect.newInstance("" + args[0]);
				} else {
					Object[] pms = RhiginWrapUtil.unwrapArgs(1, args);
					o = args[0] instanceof Class ?
						FastReflect.newInstance(null, (Class)args[0], pms) :
						FastReflect.newInstance(null, "" + args[0], pms);
				}
				return new JReflectObject(null, o);
			}
			case 1: // wrap.
			{
				if(args == null || args.length == 0 || args[0] == null) {
					this.argsException(OBJECT_NAME);
				}
				Object o = args[0];
				if(o instanceof Wrapper) {
					o = ((Wrapper)o).unwrap();
				} else if(o instanceof RhiginObjectWrapper) {
					o = ((RhiginObjectWrapper)o).unwrap();
				}
				if(o == null) {
					this.argsException(OBJECT_NAME);
				}
				return new JReflectObject(null, o);
			}
			case 2: // static.
			{
				if(args == null || args.length == 0 || args[0] == null) {
					this.argsException(OBJECT_NAME);
				} else if(args[0] instanceof Class) {
					return new JReflectObject((Class)args[0], null);
				}
				return new JReflectObject(FastReflect.getClass("" + args[0]), null);
			}
			}
			return Undefined.instance;
		}

		@Override
		public final String getName() {
			switch (type) {
			case 0:
				return "create";
			case 1:
				return "wrap";
			case 2:
				return "static";
			}
			return "";
		}
	};
	
	// javaオブジェクト.
	// リフレクションで処理するオブジェクトが返却されます.
	@SuppressWarnings("rawtypes")
	public static final class JReflectObject extends RhiginFunction implements RhiginObjectWrapper {
		private final JReflectFunction jfunc;
		private final Class clazz;
		private final Object target;
		
		JReflectObject(Class c, Object t) {
			clazz = c == null ? t.getClass() : c;
			target = t;
			jfunc = new JReflectFunction(clazz, target);
		}
		
		@Override
		public Object get(String arg0, Scriptable arg1) {
			if(arg0 != null) {
				final boolean staticFlag = target == null;
				if(FastReflect.isField(staticFlag, clazz, arg0)) {
					return getObject(FastReflect.getField(clazz, target, arg0));
				} else if(FastReflect.isMethod(staticFlag, clazz, arg0)) {
					return jfunc.set(arg0);
				}
			}
			return Undefined.instance;
		}
		
		@Override
		public boolean has(String arg0, Scriptable arg1) {
			if(arg0 != null) {
				final boolean staticFlag = target == null;
				return FastReflect.isField(staticFlag, clazz, arg0) ||
						FastReflect.isMethod(staticFlag, clazz, arg0);
			}
			return false;
		}
		
		@Override
		public void put(String arg0, Scriptable arg1, Object arg2) {
			if(arg0 != null) {
				final boolean staticFlag = target == null;
				if(FastReflect.isField(staticFlag, clazz, arg0)) {
					FastReflect.setField(clazz, target, arg0, RhiginWrapUtil.unwrap(arg2));
				}
			}
		}
		
		@Override
		public Object[] getIds() {
			final boolean staticFlag = target == null;
			final Set<String> ret = new HashSet<String>();
			FastReflect.fieldNames(ret, staticFlag, clazz);
			FastReflect.methodNames(ret, staticFlag, clazz);
			return ret.toArray();
		}
		
		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return target == null ? clazz : target;
		}
		
		@Override
		public final String getName() {
			return clazz.getName();
		}
		
		@Override
		public final String toString() {
			return target == null ? "" : target.toString();
		}
		
		public final Object unwrap() {
			return target;
		}
		
		public final boolean isStatic() {
			return target == null;
		}
	}
	
	// JavaFunctionオブジェクト.
	@SuppressWarnings("rawtypes")
	//public static final class JReflectFunction extends RhiginFunction implements RhiginObjectWrapper {
	public static final class JReflectFunction extends RhiginFunction {
		private final Class clazz;
		private final Object target;
		private String name;

		JReflectFunction(Class c, Object t) {
			clazz = c;
			target = t;
		}
		
		public JReflectFunction set(String n) {
			name = n;
			return this;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			int len = args == null ? 0 : args.length;
			if(len == 0) {
				return getObject(FastReflect.invoke(clazz, target, name));
			}
			Object[] pms = RhiginWrapUtil.unwrapArgs(args);
			return getObject(FastReflect.invoke(clazz, target, name, pms));
		}

		@Override
		public final String getName() {
			return clazz.getName() + "." + name;
		}
		
		@Override
		public final String toString() {
			return clazz.getName() + "." + name + "() {\n  [native code]\n}";
		}
		
		public final Object unwrap() {
			return target;
		}
		
		public final boolean isStatic() {
			return target == null;
		}
	};

	// シングルトン.
	private static final RhiginObject THIS = new RhiginObject(OBJECT_NAME, new RhiginFunction[] {
		new JRefrect(0), new JRefrect(1), new JRefrect(2)});

	public static final RhiginObject getInstance() {
		return THIS;
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put(OBJECT_NAME, scope, JavaObject.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(OBJECT_NAME, JavaObject.getInstance());
	}
}
