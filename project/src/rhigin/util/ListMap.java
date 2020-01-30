package rhigin.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * ListMapオブジェクト.
 * 
 * BinarySearchを使って、データの追加、削除、取得を行います.
 * HashMapと比べると速度は１０倍ぐらいは遅いですが、リソースは
 * Listと同じぐらいしか食わないので、リソースを重視する場合は、
 * こちらを利用することをおすすめします.
 * 
 * java.util.Mapを利用したい場合で、速度を超重視しない場合は、
 * ArrayMapを利用.
 * 
 * また、AndroidのArrayMapをAndroidMapとして移植したが、格納数が
 * 1000ぐらいならばAndroidMapの方が速いので、こちらを利用する.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ListMap<K, V> implements ConvertGet<K> {
	private final OList<IndexKeyValue<K, V>> list;
	
	// Index用KeyValue.
	private static final class IndexKeyValue<K, V> implements Comparable<K> {
		K key;
		V value;
		public IndexKeyValue(K k, V v) {
			key = k;
			value = v;
		}
		@Override
		public int compareTo(K n) {
			return ((Comparable)key).compareTo(n);
		}
		
		@Override
		public String toString() {
			return new StringBuilder().append(key).append(": ").append(value).toString();
		}
	}
	
	/**
	 * コンストラクタ.
	 */
	public ListMap() {
		list = new OList<IndexKeyValue<K, V>>();
	}
	
	/**
	 * コンストラクタ.
	 * @param args args.length == 1 で args[0]にMap属性を設定すると、その内容がセットされます.
	 *     また、 key, value .... で設定することも可能です.
	 */
	public ListMap(final Object... args) {
		if(args.length == 1) {
			if(args[0] instanceof Map) {
				list = new OList<IndexKeyValue<K, V>>(((Map)args[0]).size());
				set(args[0]);
				return;
			} else if(Converter.isNumeric(args[0])) {
				list = new OList<IndexKeyValue<K, V>>(Converter.convertInt(args[0]));
				return;
			}
			throw new IllegalArgumentException("Key and Value need to be set.");
		}
		list = new OList<IndexKeyValue<K, V>>(args.length >> 1);
		set(args);
	}
	
	// バイナリサーチ.
	private final int _keyNo(final K n) {
		if(n != null) {
			final Object[] olst = list.toArray();
			int low = 0;
			int high = list.size() - 1;
			int mid, cmp;
			while (low <= high) {
				mid = (low + high) >>> 1;
				if ((cmp = ((Comparable)((IndexKeyValue<K, V>)olst[mid]).key).compareTo(n)) < 0) {
					low = mid + 1;
				} else if (cmp > 0) {
					high = mid - 1;
				} else {
					return mid; // key found
				}
			}
		}
		return -1;
	}

	/**
	 * データクリア.
	 */
	public final void clear() {
		list.clear();
	}
	
	/**
	 * データセット.
	 * @param key
	 * @param value
	 * @return
	 */
	public final V put(final K key, final V value) {
		if(key == null) {
			return null;
		} else if(list.size() == 0) {
			list.add(new IndexKeyValue<K, V>(key, value));
			return null;
		}
		int mid, cmp;
		int low = 0;
		int high = list.size() - 1;
		Object[] olst = list.toArray();
		mid = -1;
		while (low <= high) {
			mid = (low + high) >>> 1;
			if ((cmp = ((Comparable)((IndexKeyValue<K, V>)olst[mid]).key).compareTo(key)) < 0) {
				low = mid + 1;
			} else if (cmp > 0) {
				high = mid - 1;
			} else {
				// 一致条件が見つかった場合.
				final IndexKeyValue<K, V> o = (IndexKeyValue<K, V>)olst[mid];
				final Object ret = o.value;
				o.value = value;
				return (V)ret;
			}
		}
		// 一致条件が見つからない場合.
		mid = (((Comparable)((IndexKeyValue<K, V>)olst[mid]).key).compareTo(key) < 0) ? mid + 1 : mid;
		list.add(null);
		final int len = list.size();
		olst = list.toArray();
		System.arraycopy(olst, mid, olst, mid + 1, len - (mid + 1));
		olst[mid] = new IndexKeyValue<K, V>(key, value);
		return null;
	}
	
	/**
	 * データ設定.
	 * @param args args.length == 1 で args[0]にMap属性を設定すると、その内容がセットされます.
	 *     また、 key, value .... で設定することも可能です.
	 */
	public final void set(final Object... args) {
		if (args == null) {
			return;
		} else if(args.length == 1) {
			// mapの場合.
			if(args[0] instanceof Map) {
				Map mp = (Map)args[0];
				if (mp == null) {
					return;
				}
				K k;
				final Iterator it = mp.keySet().iterator();
				while (it.hasNext()) {
					k = (K)it.next();
					if(k == null) {
						continue;
					}
					put(k, (V)mp.get(k));
				}
			} else {
				throw new IllegalArgumentException("Key and Value need to be set.");
			}
		} else {
			// key, value ... の場合.
			final int len = args.length;
			for (int i = 0; i < len; i += 2) {
				put((K)args[i], (V)args[i + 1]);
			}
		}
		return;
	}

	/**
	 * データ取得.
	 * @param key
	 * @return
	 */
	public final V get(final K key) {
		final int no = _keyNo(key);
		if (no == -1) {
			return null;
		}
		return list.get(no).value;
	}

	/**
	 * データ確認.
	 * @param key
	 * @return
	 */
	public final boolean containsKey(final K key) {
		return _keyNo(key) != -1;
	}

	/**
	 * データ削除.
	 * @param key
	 * @return
	 */
	public final V remove(final K key) {
		final int no = _keyNo(key);
		if (no != -1) {
			Object ret = ((IndexKeyValue<K, V>) list.get(no)).value;
			int len = list.size() - 1;
			Object[] olst = list.toArray();
			System.arraycopy(olst, no + 1, olst, no, len - no);
			olst[len] = null;
			list.length --;
			return (V)ret;
		}
		return null;
	}
	
	/**
	 * データ数を取得.
	 * @return
	 */
	public final int size() {
		return list.size();
	}

	/**
	 * キー名一覧を取得.
	 * @return
	 */
	public final Object[] names() {
		final int len = list.size();
		final Object[] ret = new Object[len];
		for (int i = 0; i < len; i++) {
			ret[i] = list.get(i).key;
		}
		return ret;
	}
	
	/**
	 * 文字列として出力.
	 * @return
	 */
	@Override
	public String toString() {
		IndexKeyValue<K, V> kv;
		StringBuilder buf = new StringBuilder();
		int len = list.size();
		buf.append("{");
		for (int i = 0; i < len; i++) {
			kv = list.get(i);
			if (i != 0) {
				buf.append(", ");
			}
			buf.append("\"").append(kv.key)
				.append("\": \"").append(kv.value).append("\"");
		}
		return buf.append("}").toString();
	}
	
	/**
	 * 指定項番でキー情報を取得.
	 * @param no
	 * @return
	 */
	public final K keyAt(int no) {
		return list.get(no).key;
	}
	
	/**
	 * 指定項番で要素情報を取得.
	 * @param no
	 * @return
	 */
	public final V valueAt(int no) {
		return list.get(no).value;
	}

	/**
	 * オリジナル情報を取得.
	 */
	@Override
	public final V getOriginal(final K n) {
		return get(n);
	}
	
	// 簡易テスト(そのうち削除).
	/*
	private static final String randName(Xor128 rand, int len) {
		int n;
		int cnt = 0;
		byte[] ret = new byte[len * 4];
		for(int i = 0; i < len; i ++) {
			n = rand.nextInt();
			ret[cnt++] = (byte)((n & 0xff000000) >> 24);
			ret[cnt++] = (byte)((n & 0x00ff0000) >> 16);
			ret[cnt++] = (byte)((n & 0x0000ff00) >> 8);
			ret[cnt++] = (byte)((n & 0x000000ff) >> 0);
		}
		return Base64.encode(ret);
	}
	
	public static final void main(String[] args) {
		int len = 100000;
		Xor128 rand = new Xor128(System.nanoTime());
		String[] key, value;
		key = new String[len];
		value = new String[len];
		
		for(int i = 0; i < len; i ++) {
			key[i] = randName(rand, 10);
			value[i] = randName(rand, 10);
		}
		
		ListMap<String, String> listMap = new ListMap<String, String>();
		Map<String, String> hashMap = new HashMap<String, String>();
		for(int i = 0; i < len; i ++) {
			listMap.put(key[i], value[i]);
			hashMap.put(key[i], value[i]);
		}
		
		int cnt = 0;
		for(int i = 0; i < len; i ++) {
			if(listMap.get(key[i]).equals(hashMap.get(key[i]))) {
				cnt ++;
			}
		}
		System.out.println("cnt:" + cnt + " " + (cnt == len));
		
		for(int i = 0; i < len >> 1; i ++) {
			listMap.remove(key[i]);
		}
		
		System.out.println("size:" + listMap.size());
		
		cnt = 0;
		for(int i = 0; i < len; i ++) {
			if(hashMap.get(key[i]).equals(listMap.get(key[i]))) {
				cnt ++;
			}
		}
		System.out.println("cnt:" + cnt + " " + (cnt == len >> 1));
	}
	*/
}
