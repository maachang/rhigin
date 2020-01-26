package rhigin.util;

/**
 * FixedKeyValue.
 */
public class FixedKeyValues<K, V> {
	private int baseLength = 32;
	private FixedSearchArray<K> keys = null;
	private Object[] values = null;
	private OList<Object> list = null;
	
	/**
	 * コンストラクタ.
	 */
	public FixedKeyValues() {
		
	}
	
	/**
	 * コンストラクタ.
	 * @param len
	 */
	public FixedKeyValues(int len) {
		baseLength = len;
	}
	
	/**
	 * インデックスキーを作成開始を設定します.
	 */
	public void startIndex() {
		list = new OList<Object>(baseLength);
		keys = null;
		values = null;
	}
	
	/**
	 * 追加されたキーをインデックス化.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean endIndex() {
		int len;
		if(list == null || (len = list.size()) == 0) {
			return false;
		}
		OList<Object> lst = list; list = null;
		keys = new FixedSearchArray<K>(len >> 1);
		values = new Object[len >> 1];
		Object[] kv = lst.toArray();
		for(int i = 0, j = 0; i < len; i += 2) {
			keys.add((K)kv[i], j);
			values[j ++] = kv[i+1];
		}
		return true;
	}
	
	/**
	 * インデックス化されているかチェック.
	 * @return
	 */
	public boolean isIndex() {
		return keys != null;
	}
	
	/**
	 * 追加処理.
	 * @param k
	 * @param v
	 * @return
	 */
	public boolean put(K k, V v) {
		if(list != null) {
			list.add(k);
			list.add(v);
			return true;
		}
		return false;
	}
	
	/**
	 * 取得処理.
	 * @param k
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public V get(K k) {
		if(keys != null) {
			int p = keys.search(k);
			if(p != -1) {
				return (V)values[p];
			}
		}
		return null;
	}
	
	/**
	 * 存在処理.
	 * @param k
	 * @return
	 */
	public boolean containsKey(K k) {
		if(keys != null) {
			return keys.search(k) != -1;
		}
		return false;
	}
	
	/**
	 * 登録データ数を取得.
	 * @return
	 */
	public int size() {
		if(keys != null) {
			return values.length;
		}
		return -1;
	}
	
	/**
	 * キー一覧を取得.
	 * @return
	 */
	public Object[] keys() {
		if(keys != null) {
			int len = values.length;
			Object[] ret = new Object[len];
			for(int i = 0; i < len; i ++) {
				ret[i] = keys.get(i);
			}
			return ret;
		}
		return null;
	}
}
