package rhigin.util;

import java.util.Arrays;

/**
 * オブジェクトリスト.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class OList<T> {
	private static final int DEF = 8;
	protected Object[] list;
	protected int length;
	protected int max;

	/**
	 * 要素を指定して、オブジェクトを作成.
	 * 
	 * @param args
	 *            対象の要素群を設定します.
	 */
	public static final OList getInstance(Object... args) {
		return new OList(args);
	}

	/**
	 * コンストラクタ.
	 */
	public OList() {
		this(DEF);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param buf
	 *            初期配列サイズを設定します.
	 */
	public OList(int buf) {
		if (buf < DEF) {
			buf = DEF;
		}
		max = buf;
		list = new Object[buf];
		length = 0;
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param 初期設定情報を設定します.
	 */
	public OList(Object... o) {
		if (o != null && o.length > 0) {
			int oLen = o.length;
			int len = oLen + (oLen >> 1);
			if (len < DEF) {
				len = DEF;
			}
			max = len;
			list = new Object[len];
			length = oLen;
			System.arraycopy(o, 0, list, 0, oLen);
		} else {
			max = DEF;
			list = new Object[max];
			length = 0;
		}
	}

	/**
	 * 情報クリア.
	 */
	public void clear() {
		list = new Object[max];
		length = 0;
	}

	/**
	 * 情報クリア.
	 * 
	 * @param buf
	 *            クリアーする場合の再生成サイズ指定をします.
	 */
	public void clear(int buf) {
		list = new Object[buf];
		length = 0;
	}

	/**
	 * 情報追加.
	 * 
	 * @param T
	 *            対象の要素を設定します.
	 */
	public void add(T n) {
		if (length + 1 >= list.length) {
			Object[] tmp = new Object[(length + (length >> 1)) + 4];
			// Object[] tmp = new Object[length << 1];
			System.arraycopy(list, 0, tmp, 0, length);
			list = tmp;
		}
		list[length++] = n;
	}

	/**
	 * すでに存在する位置の情報を再設定.
	 * 
	 * @param no
	 *            対象の項番を設定します.
	 * @param o
	 *            対象の要素を設定します.
	 * @return T 前の情報が返却されます.
	 */
	public T set(int no, T o) {
		if (no < 0 || no >= length) {
			return null;
		}
		T ret = (T) list[no];
		list[no] = o;
		return ret;
	}

	/**
	 * 指定位置の情報を取得.
	 * 
	 * @param no
	 *            対象の項番を設定します.
	 * @return T 対象の要素が返却されます.
	 */
	public T get(int no) {
		if (no < 0 || no >= length) {
			return null;
		}
		return (T) list[no];
	}

	/**
	 * 情報削除.
	 * 
	 * @param no
	 *            対象の項番を設定します.
	 * @return T 削除された情報が返却されます.
	 */
	public T remove(int no) {
		if (no < 0 || no >= length) {
			return null;
		}
		T ret = null;
		if (length == 1) {
			length = 0;
			ret = (T) list[0];
			list[0] = null;
		} else {
			// 厳密な削除.
			/*
			 * length -- ;
			 * for(int i = no ; i < length ; i ++) {
			 *     list[i] = list[i + 1];
			 * }
			 * list[length] = null;
			 */

			// 速度重視の削除.
			length--;
			ret = (T) list[no];
			list[no] = list[length];
			list[length] = null;
		}
		return ret;
	}

	/**
	 * 現在の情報数を取得.
	 * 
	 * @return size 対象のサイズが返却されます.
	 */
	public int size() {
		return length;
	}

	/**
	 * ソートを行います.
	 * 
	 * @return OList このオブジェクトが返却されます.
	 */
	public OList sort() {
		if (length > 0) {
			Arrays.sort(list, 0, length);
		}
		return this;
	}

	/**
	 * オブジェクト配列情報を取得.
	 * 
	 * @return Object[] 配列情報として取得します.
	 */
	public Object[] getArray() {
		Object[] ret = new Object[length];
		System.arraycopy(list, 0, ret, 0, length);
		return ret;
	}

	/**
	 * オブジェクト配列情報を取得.
	 * 
	 * @return Object[] 配列情報として取得します.
	 */
	public Object[] toArray() {
		return list;
	}

	/**
	 * 内容を文字列で取得.
	 * 
	 * @return String 文字列が返却されます.
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder("[");
		for (int i = 0; i < length; i++) {
			if (i != 0) {
				buf.append(", ");
			}
			buf.append(list[i]);
		}
		return buf.append("]").toString();
	}

	/**
	 * 検索処理. 
	 * ※この処理を利用する場合は、sortせずとも、処理が可能です.
	 * ただし、binarySearchと比べると、速度は遅くなります.
	 * 
	 * @param n
	 *            検索対象の要素を設定します.
	 * @return int 検索結果の位置が返却されます. [-1]の場合は情報は存在しません.
	 */
	public int search(T n) {
		int len = length;
		if (n == null) {
			for (int i = 0; i < len; i++) {
				if (list[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < len; i++) {
				if (n.equals(list[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * バイナリサーチ.
	 * ※この処理を利用する場合は、必ずsortされている必要があります.
	 * この場合はsearch処理と比べて、高速に動作します.
	 * 
	 * @param n
	 *            対象の要素を設定します.
	 * @return int 検索結果の位置が返却されます. [-1]の場合は情報は存在しません.
	 */
	public int binarySearch(Comparable n) {
		// この処理の呼び出しは、ソートされている必要がある.
		int low = 0;
		int high = length - 1;
		int mid, cmp;
		while (low <= high) {
			mid = (low + high) >>> 1;
			if ((cmp = ((Comparable) list[mid]).compareTo(n)) < 0) {
				low = mid + 1;
			} else if (cmp > 0) {
				high = mid - 1;
			} else {
				return mid; // key found
			}
		}
		return -1;
	}
}
