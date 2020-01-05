package rhigin.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * KeySet, Iterator実装支援.
 */
public class AbstractKeyIterator<K> {

	/**
	 * Map.keySet 実装で必要な基本オブジェクト. このオブジェクトを継承してkeySetメソッドを実装し、そこで return
	 * KeyIteratorSet<String>(this); のように返却します.
	 */
	public static interface Base<K> {
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
		default Object getValue(int no) {
			return null;
		}
	}

	/**
	 * Key用Iteartor.
	 */
	public static class KeyIterator<K> implements Iterator<K> {
		private Base<K> base;
		private int nowPos;
		private K target;

		protected KeyIterator(Base<K> base) {
			this.base = base;
			this.nowPos = -1;
		}

		protected boolean getNext() {
			if (target == null) {
				nowPos++;
				if (base.size() > nowPos) {
					target = base.getKey(nowPos);
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
		public K next() {
			if (getNext() == false) {
				throw new NoSuchElementException();
			}
			K ret = target;
			target = null;
			return ret;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * KeyValue.
	 */
	@SuppressWarnings("unchecked")
	private static class KeyValueEntry<K, V> implements Entry<K, V> {
		Object key;
		Object value;
		
		public KeyValueEntry(K k, V v) {
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
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static class EntryIterator implements Iterator {
		private Base base;
		private int nowPos;
		private KeyValueEntry target;

		protected EntryIterator(Base base) {
			this.base = base;
			this.nowPos = -1;
		}

		protected boolean getNext() {
			if (target == null) {
				nowPos++;
				if (base.size() > nowPos) {
					target = new KeyValueEntry(
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
		public KeyValueEntry next() {
			if (getNext() == false) {
				throw new NoSuchElementException();
			}
			KeyValueEntry ret = target;
			target = null;
			return ret;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * KeySet.
	 */
	public static class KeyIteratorSet<K> implements Set<K> {
		private Base<K> base;

		public KeyIteratorSet(Base<K> base) {
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
		public Iterator<K> iterator() {
			return new KeyIterator<K>(base);
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

		@Override
		@SuppressWarnings("hiding")
		public <Object> Object[] toArray(Object[] a) {
			return null;
		}

		@Override
		public boolean add(K e) {
			return false;
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
		public boolean addAll(Collection<? extends K> c) {
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
	}
	
	/**
	 * EntrySet.
	 */
	@SuppressWarnings("rawtypes")
	public static class EntryIteratorSet implements Set {
		private Base base;

		public EntryIteratorSet(Base base) {
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
		public Iterator iterator() {
			return new EntryIterator(base);
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

		@Override
		public boolean add(Object e) {
			return false;
		}

		@Override
		public boolean remove(Object o) {
			return false;
		}

		@Override
		public boolean containsAll(Collection c) {
			return false;
		}

		@Override
		public boolean addAll(Collection c) {
			return false;
		}

		@Override
		public boolean retainAll(Collection c) {
			return false;
		}

		@Override
		public boolean removeAll(Collection c) {
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
		public java.lang.Object[] toArray(java.lang.Object[] arg0) {
			return null;
		}
	}
}
