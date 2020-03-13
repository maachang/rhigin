package rhigin.scripts;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

/**
 * Rhino用Scriptable.
 */
public interface RhinoScriptable extends Scriptable {
	@Override
	default void delete(String name) {
	}

	@Override
	default void delete(int no) {
	}

	@Override
	default Object get(String name, Scriptable obj) {
		return RhiginWrapUtil.wrapJavaObject(_get(name, obj));
	}

	@Override
	default Object get(int no, Scriptable obj) {
		return RhiginWrapUtil.wrapJavaObject(_get(no, obj));
	}
	
	/**
	 * 名前でオブジェクトを索引.
	 * @param name
	 * @param parent
	 * @return
	 */
	Object _get(String name, Scriptable parent);
	
	/**
	 * 番号でオブジェクトを索引.
	 * @param no
	 * @param parent
	 * @return
	 */
	Object _get(int no, Scriptable parent);

	@Override
	default String getClassName() {
		return "";
	}

	@Override
	default Object getDefaultValue(Class<?> clazz) {
		return (clazz == null || String.class.equals(clazz)) ? this.toString() : Undefined.instance;
	}

	@Override
	default Object[] getIds() {
		return ScriptConstants.BLANK_ARGS;
	}

	@Override
	default Scriptable getParentScope() {
		return null;
	}

	@Override
	default Scriptable getPrototype() {
		return null;
	}

	@Override
	default boolean has(String name, Scriptable obj) {
		return false;
	}

	@Override
	default boolean has(int no, Scriptable obj) {
		return false;
	}

	@Override
	default boolean hasInstance(Scriptable instance) {
		if(instance != null) {
			return this.getClassName() == instance.getClassName();
		}
		return false;
	}

	@Override
	default void put(String name, Scriptable obj, Object value) {
	}

	@Override
	default void put(int no, Scriptable obj, Object value) {
	}

	@Override
	default void setParentScope(Scriptable obj) {
	}

	@Override
	default void setPrototype(Scriptable obj) {
	}
}
