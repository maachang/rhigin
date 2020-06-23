package rhigin.scripts;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.util.Converter;

/**
 * rhigin用Abstractメソッド.
 */
public abstract class AbstractRhiginFunction implements Function {
	public void clear() {
	}
	
	@Override
	public void delete(String arg0) {
	}

	@Override
	public void delete(int arg0) {
	}

	@Override
	public Object get(String arg0, Scriptable arg1) {
		return null;
	}

	@Override
	public Object get(int arg0, Scriptable arg1) {
		return null;
	}

	@Override
	public String getClassName() {
		return getName();
	}

	@Override
	public Object getDefaultValue(Class<?> clazz) {
		return (clazz == null || String.class.equals(clazz)) ? toString() : Undefined.instance;
	}

	@Override
	public Object[] getIds() {
		return ScriptConstants.BLANK_ARGS;
	}

	@Override
	public Scriptable getParentScope() {
		return null;
	}

	@Override
	public Scriptable getPrototype() {
		return null;
	}

	@Override
	public boolean has(String arg0, Scriptable arg1) {
		return false;
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		return false;
	}

	@Override
	public boolean hasInstance(Scriptable instance) {
		if(instance != null) {
			return this.getClassName().equals(instance.getClassName());
		}
		return false;
	}

	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {
	}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {
	}

	@Override
	public void setParentScope(Scriptable arg0) {
	}

	@Override
	public void setPrototype(Scriptable arg0) {
	}

	// Funtion呼び出し処理.
	// 処理の実装は jcall を継承して実装してください.
	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		try {
			return RhiginWrapUtil.wrapJavaObject(jcall(ctx, scope, thisObj, RhiginWrapUtil.unwrapArgs(args)));
		} catch(RhiginWrapException rwe) {
			throw rwe;
		} catch(Throwable t) {
			throw new RhiginWrapException(t);
		}
	}
	
	// new コンストラクタ呼び出し処理.
	// 処理の実装は jconstruct を継承して実装してください.
	@Override
	public final Scriptable construct(Context arg0, Scriptable arg1, Object[] arg2) {
		try {
			return jconstruct(arg0, arg1, RhiginWrapUtil.unwrapArgs(arg2));
		} catch(RhiginWrapException rwe) {
			throw rwe;
		} catch(Throwable t) {
			throw new RhiginWrapException(t);
		}
	}
	
	/**
	 * Function の内容を実装する場合は、こちらを実装してください.
	 */
	public abstract Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args);
	
	/**
	 * new (オブジェクト名) のようなオブジェクトを作成する場合には、こちらを実装します.
	 * また、戻り値は rhigin.scripts.objects.RhiginObject を利用すると、楽に作成できると思います.
	 */
	public Scriptable jconstruct(Context arg0, Scriptable arg1, Object[] arg2) {
		throw new RhiginWrapException("This method '" + getName() +
			"' does not support instantiation.");
	}

	@Override
	public String toString() {
		return "function " + getName() + "() {\n  [native code]\n}";
	}
	
	/**
	 * function名を設定します.
	 */
	public abstract String getName();
	
	/**
	 * 引数エラーを返却.
	 */
	protected Object argsException() {
		return argsException(null);
	}

	/**
	 * 引数エラーを返却.
	 */
	protected Object argsException(String objName) {
		if (objName == null) {
			throw new RhiginWrapException(500, "Insufficient arguments for " + getName() + ".");
		}
		throw new RhiginWrapException(500, "Insufficient arguments for " + objName + "." + getName() + ".");
	}
	
	/**
	 * boolean情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Boolean 情報が返却されます.
	 */
	protected Boolean getBoolean(Object n) {
		return Converter.convertBool(n);
	}

	/**
	 * int情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Integer 情報が返却されます.
	 */
	protected Integer getInt(Object n) {
		return Converter.convertInt(n);
	}

	/**
	 * long情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Long 情報が返却されます.
	 */
	protected Long getLong(Object n) {
		return Converter.convertLong(n);
	}

	/**
	 * float情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Float 情報が返却されます.
	 */
	protected Float getFloat(Object n) {
		return Converter.convertFloat(n);
	}

	/**
	 * double情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Double 情報が返却されます.
	 */
	protected Double getDouble(Object n) {
		return Converter.convertDouble(n);
	}

	/**
	 * String情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return String 情報が返却されます.
	 */
	protected String getString(Object n) {
		return Converter.convertString(n);
	}

	/**
	 * Date情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Date 情報が返却されます.
	 */
	protected java.sql.Date getDate(Object n) {
		return Converter.convertSqlDate(n);
	}

	/**
	 * Time情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Time 情報が返却されます.
	 */
	protected java.sql.Time getTime(Object n) {
		return Converter.convertSqlTime(n);
	}

	/**
	 * Timestamp情報を取得.
	 * 
	 * @parma n 対象の条件を設定します.
	 * @return Timestamp 情報が返却されます.
	 */
	protected java.sql.Timestamp getTimestamp(Object n) {
		return Converter.convertSqlTimestamp(n);
	}
}
