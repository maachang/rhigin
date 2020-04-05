package rhigin.lib.level.operator;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;

import org.maachang.leveldb.operator.LevelIndex.LevelIndexIterator;
import org.maachang.leveldb.operator.LevelIndexOperator;
import org.maachang.leveldb.operator.LevelIterator;
import org.maachang.leveldb.operator.LevelMap;
import org.mozilla.javascript.Undefined;

import rhigin.lib.level.runner.LevelJsCloseable;
import rhigin.lib.level.runner.LevelJsException;
import rhigin.scripts.JsMap;
import rhigin.util.FixedArray;

/**
 * Objectオペレータ.
 */
@SuppressWarnings("rawtypes")
public class ObjectOperator extends SearchOperator {
	protected LevelMap base;
	protected int keyType;
	
	/**
	 * コンストラクタ.
	 * @param c Closeableオブジェクトを設定.
	 * @param n オペレータ名を設定.
	 * @param o オペレータを設定.
	 */
	public ObjectOperator(LevelJsCloseable c, String n, LevelMap o) {
		this.closeable = c;
		this.base = o;
		this.name = n;
		this.keyType = OperatorKeyType.getKeyType(o.getOption());
		// writeBatchモードの場合、クローズ処理に登録.
		if(o.isWriteBatch()) {
			c.reg(this);
		}
	}

	@Override
	protected LevelIndexOperator _operator() {
		return base;
	}

	@Override
	protected Object _put(Map value, int keyLen, Object[] keys) {
		final Object key = OperatorKeyType.convertKeyType(keyType, keys[0]);
		base.put(key, value);
		return Undefined.instance;
	}

	@Override
	protected void _remove(int keyLen, Object[] keys) {
		final Object key = OperatorKeyType.convertKeyType(keyType, keys[0]);
		base.remove(key);
	}

	@Override
	protected Object _get(int keyLen, Object[] keys) {
		final Object key = OperatorKeyType.convertKeyType(keyType, keys[0]);
		return base.get(key);
	}

	@Override
	protected boolean _contains(int keyLen, Object[] keys) {
		final Object key = OperatorKeyType.convertKeyType(keyType, keys[0]);
		return base.containsKey(key);
	}

	@Override
	protected OperateIterator _iterator(Lock lock, boolean desc, int keyLen, Object[] keys) {
		if(keyLen == 0) {
			return new SearchIterator(lock, base.snapshot(desc, null), keyType, null);
		}
		final Object key = OperatorKeyType.convertKeyType(keyType, keys[0]);
		return new SearchIterator(lock, base.snapshot(desc, key), keyType, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected OperateIterator _range(Lock lock, boolean desc, int keyLen, Object[] keys) {
		if(keyLen < 2) {
			throw new LevelJsException("Two keys are required: \"0\" start key \"1\" end key");
		}
		Comparable start = (Comparable)OperatorKeyType.convertKeyType(keyType, keys[0]);
		Comparable end = (Comparable)OperatorKeyType.convertKeyType(keyType, keys[1]);
		
		// startよりendの方が小さい場合.
		Comparable c;
		if(start.compareTo(end) > 0) {
			c = start;
			start = end;
			end = c;
		}
		// 降順の場合.
		if(desc) {
			c = start;
			start = end;
			end = c;
		}
		return new SearchIterator(lock, base.snapshot(desc, start), keyType, end);
	}
	
	@Override
	protected OperateIterator _index(Lock lock, LevelIndexIterator itr, Comparable endKey, String[] columns) {
		return new IndexIterator(lock, keyType, itr, endKey, columns);
	}
	
	// 検索iterator.
	private static final class SearchIterator implements OperateIterator {
		private LevelIterator src;
		private boolean reverse;
		private int keyType;
		private Comparable end;
		
		private boolean exitFlag;
		private Object beforeKey;
		private Object nextKey;
		private Object nextValue;
		
		private Lock lock;
		private JsMap nextMap;
		
		public SearchIterator(Lock lk, LevelIterator it, int k, Comparable e) {
			src = it;
			reverse = it.isReverse();
			keyType = k;
			end = e;
			exitFlag = false;
			lock = lk;
		}
		
		/**
		 * クローズ処理.
		 */
		public void close() {
			lock.lock();
			try {
				src.close();
			} finally {
				lock.unlock();
			}
		}
		
		/**
		 * クローズ済みかチェック.
		 * @return
		 */
		public boolean isClose() {
			lock.lock();
			try {
				return src.isClose();
			} finally {
				lock.unlock();
			}
		}
		
		/**
		 * 降順か取得.
		 * @return
		 */
		public boolean isDesc() {
			return reverse;
		}
		
		/**
		 * キー名を取得.
		 * @return Object キー名が返却されます.
		 */
		public Object key() {
			Object o;
			lock.lock();
			try {
				o = OperatorKeyType.getRestoreKey(keyType, beforeKey);
			} finally {
				lock.unlock();
			}
			return new FixedArray(o);
		}

		@Override
		public boolean hasNext() {
			lock.lock();
			try {
				return _next();
			} finally {
				lock.unlock();
			}
		}

		@Override
		public Map next() {
			Object ret;
			lock.lock();
			try {
				if(nextKey == null && nextValue == null) {
					if(!_next()) {
						throw new NoSuchElementException();
					}
				}
				beforeKey = nextKey;
				ret = nextValue;
				nextKey = null; nextValue = null;
			} finally {
				lock.unlock();
			}
			if(nextMap == null) {
				nextMap = new JsMap(ret);
			} else {
				nextMap.set(ret);
			}
			return nextMap;
		}
		
		
		@SuppressWarnings("unchecked")
		private boolean _next() {
			if(exitFlag || src.isClose() || !src.hasNext()) {
				close();
				return false;
			} else if(nextKey != null && nextValue != null) {
				return true;
			}
			nextValue = src.next();
			nextKey = src.getKey();
			// range検索の場合.
			if(end != null &&
				((!reverse && end.compareTo(nextKey) <= 0) ||
				(reverse && end.compareTo(nextKey) >= 0))) {
				// 終端を検出.
				exitFlag = true;
			}
			return true;
		}
	}
	
	// indexIterator.
	private static final class IndexIterator implements OperateIterator {
		private LevelIndexIterator src;
		private int keyType;
		private Comparable end;
		private String[] columns;
		
		private boolean exitFlag;
		private Object beforeKey;
		private Object nextKey;
		private Object nextValue;
		
		private Lock lock;
		private boolean reverse;
		private JsMap nextMap;
		
		public IndexIterator(Lock lk, int type, LevelIndexIterator s, Comparable e, String[] c) {
			src = s;
			reverse = s.isReverse();
			keyType = type;
			end = e;
			columns = c;
			lock = lk;
			exitFlag = false;
		}

		@Override
		public void close() {
			lock.lock();
			try {
				src.close();
				exitFlag = true;
			} finally {
				lock.unlock();
			}
		}

		@Override
		public boolean isClose() {
			lock.lock();
			try {
				return src.isClose();
			} finally {
				lock.unlock();
			}
		}

		@Override
		public boolean isDesc() {
			return reverse;
		}

		@Override
		public Object key() {
			return new FixedArray(beforeKey);
		}

		@Override
		public boolean hasNext() {
			lock.lock();
			try {
				return _next();
			} finally {
				lock.unlock();
			}
		}

		@Override
		public Map next() {
			Object ret;
			lock.lock();
			try {
				if(nextKey == null && nextValue == null) {
					if(!_next()) {
						throw new NoSuchElementException();
					}
				}
				beforeKey = nextKey;
				ret = nextValue;
				nextKey = null; nextValue = null;
			} finally {
				lock.unlock();
			}
			if(nextMap == null) {
				nextMap = new JsMap(ret);
			} else {
				nextMap.set(ret);
			}
			return nextMap;
		}
		
		@SuppressWarnings("unchecked")
		private boolean _next() {
			while(true) {
				if(exitFlag || src.isClose() || !src.hasNext()) {
					close();
					return false;
				} else if(nextKey != null && nextValue != null) {
					return true;
				}
				nextValue = src.next();
				final Object checkKey = SearchOperator._indexColumns(nextValue, columns);
				if(checkKey == null) {
					continue;
				}
				nextKey = OperatorKeyType.getRestoreKey(keyType, src.getKey());
				// range検索の場合.
				if(end != null &&
					((!reverse && end.compareTo(checkKey) <= 0) ||
					(reverse && end.compareTo(checkKey) >= 0))) {
					// 終端を検出.
					exitFlag = true;
				}
				return true;
			}
		}
	}
}
