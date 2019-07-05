package rhigin.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("rawtypes")
public interface BlankMap extends Map {
	@Override
	default void clear() {
	}

	@Override
	default void putAll(Map toMerge) {
	}

	@Override
	default boolean containsValue(Object value) {
		return false;
	}

	@Override
	default Set entrySet() {
		return null;
	}

	@Override
	default Collection values() {
		return null;
	}

	@Override
	default Object put(Object name, Object value) {
		return null;
	}

	@Override
	default boolean containsKey(Object key) {
		return false;
	}

	@Override
	default Object get(Object key) {
		return null;
	}

	@Override
	default Object remove(Object key) {
		return null;
	}

	@Override
	default boolean isEmpty() {
		return true;
	}

	@Override
	default Set keySet() {
		return new HashSet();
	}

	@Override
	default int size() {
		return 0;
	}
}
