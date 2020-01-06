package rhigin.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * KeySet, Iterator実装支援.
 */
public class AbstractKeyIterator {

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
	}

	/**
	 * Key用Iteartor.
	 */
	public static class Iterator<K> implements java.util.Iterator<K> {
		private Base<K> base;
		private int nowPos;
		private K target;

		protected Iterator(Base<K> base) {
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
	 * KeySet.
	 */
	public static class Set<K> implements java.util.Set<K> {
		private Base<K> base;

		public Set(Base<K> base) {
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
			return new Iterator<K>(base);
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
}
