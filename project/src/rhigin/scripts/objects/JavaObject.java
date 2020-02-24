package rhigin.scripts.objects;

import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
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
	
	/**
	 * JReflectObjectのインスタンス生成.
	 * @param clazz Classオブジェクトか、Stringでクラス名をパッケージ名を含めて設定します.
	 * @param args コンストラクタパラメータを設定します.
	 * @return JReflectObject javascriptから呼び出せるJavaのリフレクションオブジェクトが返却されます.
	 */
	@SuppressWarnings("rawtypes")
	public static final JReflectObject newInstance(Object clazz, Object... args) {
		if(clazz == null || !(clazz instanceof Class || clazz instanceof String)) {
			throw new RhiginException("The specified class must be a Class or String object.");
		}
		Object o = null;
		int len = args == null ? 0 : args.length;
		if(len == 1) {
			o = clazz instanceof Class ?
				FastReflect.newInstance(null, (Class)clazz) :
				FastReflect.newInstance(null, (String)clazz);
		} else {
			o = args[0] instanceof Class ?
				FastReflect.newInstance(null, (Class)clazz, args) :
				FastReflect.newInstance(null, (String)clazz, args);
		}
		return new JReflectObject(null, o);
	}
	
	/**
	 * JavaオブジェクトをJavascriptで利用できるオブジェクトをJReflectObject にラップします.
	 * @param object 対象のJavaオブジェクトを設定します.
	 * @return JReflectObject javascriptから呼び出せるJavaのリフレクションオブジェクトが返却されます.
	 */
	public static final JReflectObject wrapObject(Object object) {
		if(object == null) {
			throw new RhiginException("The object to wrap is 'null'.");
		}
		return new JReflectObject(null, object);
	}
	
	/**
	 * StaticなJavaオブジェクトをJavascriptで利用できるオブジェクトをJReflectObject にラップします.
	 * @param clazz Classオブジェクトか、Stringでクラス名をパッケージ名を含めて設定します.
	 * @return JReflectObject javascriptから呼び出せるJavaのリフレクションオブジェクトが返却されます.
	 */
	@SuppressWarnings("rawtypes")
	public static final JReflectObject wrapStatic(Object clazz) {
		if(clazz == null || !(clazz instanceof Class || clazz instanceof String)) {
			throw new RhiginException("The specified class must be a Class or String object.");
		} else if(clazz instanceof Class) {
			return new JReflectObject((Class)clazz, null);
		}
		return new JReflectObject(FastReflect.getClass((String)clazz), null);
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
				int len = args.length;
				if(len == 1) {
					return args[0] instanceof Class ?
						newInstance((Class)args[0]) :
						newInstance(args[0]);
				}
				Object[] pms = new Object[len-1];
				System.arraycopy(args, 1, pms, 0, len - 1);
				return args[0] instanceof Class ?
					newInstance((Class)args[0], pms) :
					newInstance(args[0], pms);
			}
			case 1: // wrap.
			{
				if(args == null || args.length == 0 || args[0] == null) {
					this.argsException(OBJECT_NAME);
				}
				return wrapObject(args[0]);
			}
			case 2: // static.
			{
				if(args == null || args.length == 0 || args[0] == null) {
					this.argsException(OBJECT_NAME);
				} else if(args[0] instanceof Class) {
					return wrapStatic((Class)args[0]);
				}
				return wrapStatic(args[0]);
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
			return getObject(FastReflect.invoke(clazz, target, name, args));
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
