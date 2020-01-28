package rhigin.util;

import java.util.HashMap;
import java.util.Map;

/**
 * LRUキャッシュ.
 */
@SuppressWarnings("unchecked")
public class LruCache<K, V> {
	public static final class Element {
		public Object key;
		public Object value;
		protected Element before;
		protected Element next;

		public String toString() {
			return new StringBuilder().append(key).append(": ").append(value).toString();
		}
	};

	private Element top;
	private Element last;
	private Map<K, Element> list;
	private int maxSize;

	/**
	 * コンストラクタ.
	 *
	 * @param size
	 *            最大数を設定.
	 */
	public LruCache(int size) {
		top = null;
		last = null;
		if(size > 1000) {
			// サイズが大きい場合は、HashMapを利用.
			list = new HashMap<K, Element>(size + 1, 1.0f);
		} else {
			// サイズが少ない場合は、AndroidのArrayMapを利用.
			list = new AndroidMap<K, Element>(size + 1);
		}
		maxSize = size;
	}

	/**
	 * クリア処理.
	 */
	public void clear() {
		top = null;
		last = null;
		list = null;
	}

	/**
	 * 最大管理データ数を超えた後の削除データの後処理を行う.
	 * 
	 * @param key
	 *            削除対象のキーが設定されます.
	 * @param value
	 *            削除対象の要素が設定されます.
	 */
	protected void maxDataByRemove(K key, V value) {

	}

	// 新規データをセット.
	private final void newTop(Element e) {

		// topにセット.
		e.before = null;
		e.next = top;
		if (top != null) {
			top.before = e;
		}
		top = e;

		// 今回処理した条件がラストの場合.
		if (e.next == null) {
			last = e;
		}

		// 既に満タンな場合.
		if (maxSize == list.size()) {
			Element end = last;
			last = last.before;
			last.next = null;
			list.put((K) e.key, e);

			list.remove((K) end.key);
			maxDataByRemove((K) end.key, (V) end.value);

			// 空きがある.
		} else {
			list.put((K) e.key, e);
		}
	}

	// 既存データの上書き.
	private final void useTop(Element s, Element d) {

		// topにセット.
		d.before = null;
		d.next = top;
		if (top != null) {
			top.before = d;
		}
		top = d;

		// リストセット.
		list.put((K) d.key, d);

		// 今回処理した条件がラストの場合.
		if (d.next == null) {
			last = d;
		}

		// 削除対象の要素をクリア.
		Element before = s.before;
		Element next = s.next;
		if (before != null) {
			before.next = next;

			// 今回処理した条件がラストの場合.
			if (before.next == null) {
				last = before;
			}
		}
		if (next != null) {
			next.before = before;

			// 今回処理した条件がラストの場合.
			if (next.next == null) {
				last = next;
			}
		}
	}

	// topに更新.
	private final void updateTop(Element e) {
		if (e.before == null) {
			return;
		}

		// 対象の情報をクリア.
		Element before = e.before;
		Element next = e.next;
		before.next = next;
		if (next != null) {
			next.before = before;

			// 今回処理した条件がラストの場合.
			if (next.next == null) {
				last = next;
			}
			// 今回処理した条件がラストの場合.
		} else if (before.next == null) {
			last = before;
		}

		// topにセット.
		e.before = null;
		e.next = top;
		if (top != null) {
			top.before = e;
		}
		top = e;

		// 今回処理した条件がラストの場合.
		if (e.next == null) {
			last = e;
		}
	}

	// 対象要素を削除.
	private final void removeElement(Element e) {
		// 削除対象の要素をクリア.
		Element before = e.before;
		Element next = e.next;

		// 今回の処理で、データがゼロ件になる場合.
		if (before == null && next == null) {
			top = null;
			last = null;
		} else {
			// 下の条件が存在する場合.
			if (before != null) {
				// 下の条件を削除.
				before.next = next;

				// 今回処理した条件がラストの場合.
				if (before.next == null) {
					last = before;
				}
			}
			// 上の条件が存在する場合.
			if (next != null) {
				// 上の条件を削除.
				next.before = before;

				// 今回処理した条件がラストの場合.
				if (next.next == null) {
					last = next;
				}

				// 今回処理した条件がTOPの場合.
				if (next.before == null) {
					top = next;
				}
			}
		}
	}

	/**
	 * データセット.
	 * 
	 * @param key
	 *            追加対象のキーを設定します.
	 * @param value
	 *            追加対象の要素を設定します.
	 * @return V 置き換えられた要素が設定されます.
	 */
	public V put(K key, V value) {
		if (key == null) {
			return null;
		}

		Element e = new Element();
		e.key = key;
		e.value = value;

		Element ret = list.get(key);
		if (ret == null) {
			newTop(e);
			return null;
		}

		useTop(ret, e);

		return (V) ret.value;
	}

	/**
	 * データ取得.
	 * 
	 * @param key
	 *            対象のキーを設定します.
	 * @return V 要素が返却されます.
	 */
	public V get(K key) {
		if (key == null) {
			return null;
		}

		Element ret = list.get(key);
		if (ret == null) {
			return null;
		}

		updateTop(ret);
		return (V) ret.value;
	}

	/**
	 * データ削除.
	 * 
	 * @param key
	 *            対象のキーを設定します.
	 * @return V 要素が返却されます.
	 */
	public V remove(K key) {
		if (key == null) {
			return null;
		}

		Element ret = list.remove(key);
		if (ret == null) {
			return null;
		}

		removeElement(ret);
		return (V) ret.value;
	}

	/**
	 * データ存在確認.
	 * 
	 * @param key
	 *            対象のキーを設定します.
	 * @return booelan [true]の場合、存在します.
	 */
	public boolean contains(K key) {
		if (key == null) {
			return false;
		}
		return list.containsKey(key);
	}

	/**
	 * データ数を取得.
	 * 
	 * @return int データ数が返却されます.
	 */
	public int size() {
		return list.size();
	}

	/**
	 * 管理データ長を取得.
	 * 
	 * @return int 管理データ長が返却されます.
	 */
	public int getMaxLSize() {
		return maxSize;
	}

	/**
	 * 要素一覧を取得.
	 * 
	 * @return Object[] 要素一覧が返却されます.
	 */
	public Object[] getElements() {
		int len = list.size();
		Object[] ret = new Object[len];
		int cnt = 0;
		Element n = top;
		while (n != null) {
			ret[cnt++] = n.value;
			n = n.next;
		}
		return ret;
	}

	/**
	 * 文字出力.
	 */
	public String toString() {
		Element n = top;
		StringBuilder buf = new StringBuilder();
		while (n != null) {
			buf.append(n).append("\n");
			n = n.next;
		}
		buf.append(" last:").append((last == null) ? "null" : last).append("\n");
		return buf.toString();
	}
}
