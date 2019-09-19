package rhigin.util;

import java.util.Iterator;
import java.util.Map;

/**
 * ListMapオブジェクト. java.util.Mapを利用したい場合は ArrayMapを利用.
 */
public class ListMap implements ConvertGet<String> {
	private final OList<Object[]> list = new OList<Object[]>();

	public ListMap() {
	}

	public ListMap(final Map<String, Object> v) {
		set(v);
	}

	public ListMap(final Object... args) {
		set(args);
	}

	private final int getNo(final String name) {
		final int len = list.size();
		final Object[] o = list.toArray();
		for (int i = 0; i < len; i++) {
			if (name.equals(((Object[]) o[i])[0])) {
				return i;
			}
		}
		return -1;
	}

	public final void clear() {
		list.clear();
	}

	public final void set(final Object... args) {
		if (args == null) {
			return;
		}
		final int len = args.length;
		for (int i = 0; i < len; i += 2) {
			put((String) args[i], args[i + 1]);
		}
	}

	public final void set(final Map<String, Object> v) {
		if (v == null) {
			return;
		}
		String k;
		final Iterator<String> it = v.keySet().iterator();
		while (it.hasNext()) {
			k = it.next();
			put(k, v.get(k));
		}
	}

	public final Object put(final String key, final Object value) {
		final int no = getNo(key);
		if (no == -1) {
			list.add(new Object[] { key, value });
			return null;
		}
		final Object[] o = list.get(no);
		final Object ret = o[1];
		o[1] = value;
		return ret;
	}

	public final Object get(final String key) {
		final int len = list.size();
		final Object[] o = list.toArray();
		for (int i = 0; i < len; i++) {
			if (key.equals(((Object[]) o[i])[0])) {
				return ((Object[]) o[i])[1];
			}
		}
		return null;
	}

	public final boolean containsKey(final String key) {
		return getNo(key) != -1;
	}

	public final Object remove(final String key) {
		final int no = getNo(key);
		if (no != -1) {
			Object ret = ((Object[]) list.get(no))[1];
			list.remove(no);
			return ret;
		}
		return null;
	}

	public final int size() {
		return list.size();
	}

	public final String[] names() {
		final int len = list.size();
		final String[] ret = new String[len];
		for (int i = 0; i < len; i++) {
			ret[i] = (String) ((Object[]) list.get(i))[0];
		}
		return ret;
	}

	public final OList<Object[]> rawData() {
		return list;
	}

	// original 取得.
	@Override
	public final Object getOriginal(final String n) {
		return get(n);
	}
}
