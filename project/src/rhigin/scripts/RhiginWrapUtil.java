package rhigin.scripts;

import java.lang.reflect.Method;

import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import rhigin.RhiginException;
import rhigin.scripts.objects.JDateObject;
import rhigin.util.Converter;
import rhigin.util.FixedArray;

/**
 * javascriptからのjavaオブジェクトのラップ・アンラップ支援.
 */
public class RhiginWrapUtil {
	protected RhiginWrapUtil() {}
	
	/**
	 * Javaのパラメータとしてアンラップ.
	 * @param args
	 * @return
	 */
	public static final Object[] unwrapArgs(final Object[] args) {
		final int length = args == null ? 0 : args.length;
		if(length == 0) {
			return args;
		}
		for(int i = 0; i < length; i ++) {
			args[i] = unwrap(args[i]);
		}
		return args;
	}
	
	/**
	 * Javaのパラメータとしてアンラップ.
	 * @param off
	 * @param args
	 * @return
	 */
	public static final Object[] unwrapArgs(final int off, final Object[] args) {
		final int length = args == null ? 0 : args.length;
		if(length == 0) {
			return args;
		}
		if(off == 0) {
			for(int i = 0; i < length; i ++) {
				args[i] = unwrap(args[i]);
			}
			return args;
		}
		int cnt = 0;
		final Object[] pms = new Object[length - off];
		for(int i = off; i < length; i ++) {
			pms[cnt ++] = unwrap(args[i]);
		}
		return pms;
	}
	
	/**
	 * Javaのオブジェクトとしてアンラップ.
	 * @param value
	 * @return
	 */
	public static final Object unwrap(Object value) {
		if(value == null || Undefined.isUndefined(value)) {
			return value;
		} else if(value instanceof Wrapper) {
			return ((Wrapper)value).unwrap();
		} else if(value instanceof RhiginObjectWrapper) {
			return ((RhiginObjectWrapper)value).unwrap();
		} else if (value instanceof IdScriptableObject &&
			"Date".equals(((IdScriptableObject)value).getClassName())) {
			// NativeDate.
			try {
				// 現状リフレクションで直接取得するようにする.
				// 本来は ScriptRuntime.toNumber(NativeDate) で取得できるのだけど、
				// これは rhinoのContextの範囲内でないとエラーになるので.
				final Method md = value.getClass().getDeclaredMethod("getJSTimeValue");
				md.setAccessible(true);
				return JDateObject.newObject(Converter.convertLong(md.invoke(value)));
			} catch (Exception e) {
				throw new RhiginException(e);
			}
		}
		return value;
	}
	
	/**
	 * Javaオブジェクトからrhino向けにラップ.
	 * @param value
	 * @return
	 */
	public static final Object wrap(Object value) {
		return wrap(null, value);
	}
	
	/**
	 * Javaオブジェクトからrhino向けにラップ.
	 * @param result [0] == trueの場合、変換されました。
	 * @param value
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static final Object wrap(boolean[] result, Object value) {
		if(result != null) {
			result[0] = true;
		}
		Class c;
		if(value == null || Undefined.isUndefined(value)) {
			return value;
		} else if(value instanceof Wrapper) {
			value = ((Wrapper)value).unwrap();
		} else if(value instanceof RhiginObjectWrapper || value instanceof Scriptable) {
			return value;
		}
		c = value.getClass();
		if(c.isArray() || value instanceof FixedArray) {
			return new JavaScriptable.ReadArray(value);
		} else if(c.isPrimitive() ||
			value instanceof Exception ||
			value instanceof String || value instanceof Boolean || value instanceof Number ||
			c.getPackage().getName().startsWith(ExecuteScript.RHINO_JS_PACKAGE_NAME)) {
			return value;
		} else if (value instanceof java.util.Map) {
			return new JavaScriptable.GetMap((java.util.Map) value);
		} else if (value instanceof java.util.List) {
			return new JavaScriptable.GetList((java.util.List) value);
		} else if(value instanceof java.util.Date) {
			return JDateObject.newObject((java.util.Date)value);
		}
		if(result != null) {
			result[0] = false;
		}
		return value;
	}

}
