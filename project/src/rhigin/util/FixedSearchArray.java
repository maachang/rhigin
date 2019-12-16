package rhigin.util;

import java.util.Arrays;

import rhigin.RhiginException;

/**
 * 検索リスト.
 * 
 * バイナリサーチで、リスト情報を高速検索.
 */
public class FixedSearchArray<K> {
	
	// 検索キー.
	@SuppressWarnings("rawtypes")
	private static class SearchKey<K> implements Comparable {
		public final K key;
		public final int no;
		public SearchKey(K k, int n) {
			key = k;
			no = n;
		}

		@Override
		@SuppressWarnings("unchecked")
		public final int compareTo(final Object o) {
			if(o instanceof SearchKey) {
				return ((Comparable)key).compareTo(((SearchKey)o).key);
			}
			return ((Comparable)key).compareTo(o);
		}
		
		@Override
		public final boolean equals(final Object o) {
			if(o instanceof SearchKey) {
				return key.equals(((SearchKey)o).key);
			}
			return key.equals(o);
		}
	}
	
	// 検索キーで検索.
	@SuppressWarnings("rawtypes")
	private final int searchKey(SearchKey[] keys, K target) {
		int ret = binarySearch(keys, target);
		if(ret == -1) {
			return -1;
		}
		return keys[ret].no;
	}

	// バイナリサーチ.
	@SuppressWarnings("rawtypes")
	private final int binarySearch(SearchKey[] keys, K n) {
		int low = 0;
		int high = keys.length - 1;
		int mid, cmp;
		while (low <= high) {
			mid = (low + high) >>> 1;
			if ((cmp = keys[mid].compareTo(n)) < 0) {
				low = mid + 1;
			} else if (cmp > 0) {
				high = mid - 1;
			} else {
				return mid; // key found
			}
		}
		return -1;
	}
	
	private SearchKey<K>[] keys = null;
	private int count = 0;
	
	protected FixedSearchArray() {}
	
	/**
	 * コンストラクタ.
	 * @param len 排列数を設定します.
	 */
	@SuppressWarnings("unchecked")
	public FixedSearchArray(int len) {
		keys = new SearchKey[len];
	}
	
	/**
	 * リスト追加.
	 * @param key
	 * @param no
	 * @return
	 */
	public FixedSearchArray<K> add(K key, int no) {
		if(count >= keys.length) {
			throw new RhiginException("max arrays:" + keys.length);
		}
		keys[count ++] = new SearchKey<K>(key, no);
		if(count >= keys.length) {
			Arrays.sort(keys);
		}
		return this;
	}
	
	/**
	 * 追加したリストが全件設定されているかチェック.
	 * この内容がtrueで無い場合は、検索出来ません.
	 * @return
	 */
	public boolean isFix() {
		return count == keys.length;
	}
	
	/**
	 * 検索処理.
	 * @param target
	 * @return
	 */
	public int search(K target) {
		if(count == keys.length) {
			return searchKey(keys, target);
		}
		return -1;
	}
}
