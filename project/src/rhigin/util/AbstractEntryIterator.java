package rhigin.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * KeySet, Iterator実装支援.
 */
public class AbstractEntryIterator {

	/**
	 * Map.keySet 実装で必要な基本オブジェクト.
	 * このオブジェクトを継承してkeySetメソッドを実装し、そこで
	 *   return AbstractEntryIterator.Set<>(this);
	 * のように返却します.
	 */
	public static interface Base<K, V> {
		/**
		 * キーを取得.
		 * 
		 * @param no
		 * @return
		 */
		public K getKey(int no);

		/**
		 * キー数を取得.
		 * 
		 * @return
		 */
		public int size();

		/**
		 * 要素を取得.
		 * @param no
		 * @return
		 */
		public V getValue(int no);
	}
	
	/**
	 * KeyValue.
	 */
	@SuppressWarnings("unchecked")
	private static class Entry<K, V> implements java.util.Map.Entry<K, V> {
		Object key;
		Object value;
		
		public Entry(K k, V v) {
			key = k;
			value = v;
		}

		@Override
		public K getKey() {
			return (K)key;
		}

		@Override
		public V getValue() {
			return (V)value;
		}

		@Override
		public V setValue(V arg0) {
			Object o = value;
			value = arg0;
			return (V)o;
		}
		
	}
	
	/**
	 * Entry用Iteartor.
	 */
	public static class Iterator<K, V> implements java.util.Iterator<java.util.Map.Entry<K, V>> {
		private Base<K, V> base;
		private int nowPos;
		private AbstractEntryIterator.Entry<K, V> target;

		protected Iterator(Base<K, V> base) {
			this.base = base;
			this.nowPos = -1;
		}

		protected boolean getNext() {
			if (target == null) {
				nowPos++;
				if (base.size() > nowPos) {
					target = new AbstractEntryIterator.Entry<K, V>(
						base.getKey(nowPos), base.getValue(nowPos));
					return true;
				}
				return false;
			}
			return true;
		}

		@Override
		public boolean hasNext() {
			return getNext();
		}

		@Override
		public java.util.Map.Entry<K, V> next() {
			if (getNext() == false) {
				throw new NoSuchElementException();
			}
			Entry<K, V> ret = target;
			target = null;
			return ret;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * EntrySet.
	 */
	public static class Set<K,V> implements java.util.Set<java.util.Map.Entry<K, V>> {
		private Base<K, V> base;

		public Set(Base<K, V> base) {
			this.base = base;
		}

		@Override
		public int size() {
			return base.size();
		}

		@Override
		public boolean isEmpty() {
			return base.size() == 0;
		}

		@Override
		public boolean contains(Object o) {
			if (o == null) {
				return false;
			}
			int len = base.size();
			for (int i = 0; i < len; i++) {
				if (o == base.getKey(i) || o.equals(base.getKey(i))) {
					return true;
				}
			}
			return false;
		}

		@Override
		public java.util.Iterator<java.util.Map.Entry<K, V>> iterator() {
			return new AbstractEntryIterator.Iterator<>(base);
		}

		@Override
		public Object[] toArray() {
			int len = base.size();
			Object[] ret = new Object[len];
			for (int i = 0; i < len; i++) {
				ret[i] = base.getKey(i);
			}
			return ret;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(T[] ret) {
			int len = base.size();
			if(ret.length < len) {
				ret = (T[])Array.newInstance(ret.getClass(), len);
			}
			for (int i = 0; i < len; i++) {
				ret[i] = (T)base.getKey(i);
			}
			return ret;
		}

		@Override
		public boolean remove(Object o) {
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return false;
		}

		@Override
		public void clear() {
		}

		@Override
		public boolean equals(Object o) {
			return this.equals(o);
		}

		@Override
		public int hashCode() {
			return -1;
		}

		@Override
		public boolean add(java.util.Map.Entry<K, V> arg0) {
			return false;
		}

		@Override
		public boolean addAll(Collection<? extends java.util.Map.Entry<K, V>> arg0) {
			return false;
		}
	}
}
