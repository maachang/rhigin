package rhigin.scripts;

import org.mozilla.javascript.Scriptable;

public interface BlankScriptable extends Scriptable {
	@Override
	default void delete(String arg0) {
	}

	@Override
	default void delete(int arg0) {
	}

	@Override
	default Object get(String arg0, Scriptable arg1) {
		return null;
	}

	@Override
	default Object get(int arg0, Scriptable arg1) {
		return null;
	}

	@Override
	default String getClassName() {
		return null;
	}

	@Override
	default Object getDefaultValue(Class<?> arg0) {
		return null;
	}

	@Override
	default Object[] getIds() {
		return null;
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
	default boolean has(String arg0, Scriptable arg1) {
		return false;
	}

	@Override
	default boolean has(int arg0, Scriptable arg1) {
		return false;
	}

	@Override
	default boolean hasInstance(Scriptable arg0) {
		return false;
	}

	@Override
	default void put(String arg0, Scriptable arg1, Object arg2) {
	}

	@Override
	default void put(int arg0, Scriptable arg1, Object arg2) {
	}

	@Override
	default void setParentScope(Scriptable arg0) {
	}

	@Override
	default void setPrototype(Scriptable arg0) {
	}
}
