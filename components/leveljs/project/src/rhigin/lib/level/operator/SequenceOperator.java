package rhigin.lib.level.operator;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;

import org.maachang.leveldb.Time12SequenceId;
import org.maachang.leveldb.operator.LevelIndex.LevelIndexIterator;
import org.maachang.leveldb.operator.LevelIndexOperator;
import org.maachang.leveldb.operator.LevelIterator;
import org.maachang.leveldb.operator.LevelSequence;

import rhigin.lib.level.runner.LevelJsCloseable;
import rhigin.lib.level.runner.LevelJsException;
import rhigin.scripts.JsMap;
import rhigin.util.Converter;
import rhigin.util.FixedArray;

/**
 * シーケンスオペレータ.
 */
@SuppressWarnings("rawtypes")
public class SequenceOperator extends SearchOperator {
	private LevelSequence base = null;
	
	/**
	 * コンストラクタ.
	 * @param c Closeableオブジェクトを設定.
	 * @param n オペレータ名を設定.
	 * @param o オペレータを設定.
	 */
	public SequenceOperator(LevelJsCloseable c, String n, LevelSequence o) {
		this.closeable = c;
		this.base = o;
		this.name = n;
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
		if(keyLen == 0) {
			return base.add(value);
		}
		return base.put(keys[0], value);
	}

	@Override
	protected void _remove(int keyLen, Object[] keys) {
		base.remove(keys[0]);
	}

	@Override
	protected Object _get(int keyLen, Object[] keys) {
		return base.get(keys[0]);
	}

	@Override
	protected boolean _contains(int keyLen, Object[] keys) {
		return base.contains(keys[0]);
	}

	@Override
	protected OperateIterator _iterator(Lock lock, boolean desc, int keyLen, Object[] keys) {
		if(keyLen > 0) {
			final Object key = Converter.convertString(keys[0]);
			return new SearchIterator(lock, base.snapshot(desc, key), null);
		} else {
			return new SearchIterator(lock, base.snapshot(desc), null);
		}
	}

	@Override
	protected OperateIterator _range(Lock lock, boolean desc, int keyLen, Object[] keys) {
		if(keyLen < 2) {
			throw new LevelJsException("Two keys are required. \"0\" start key \"1\" end key");
		}
		String start = Converter.convertString(keys[0]);
		String end = Converter.convertString(keys[1]);
		
		// startよりendの方が小さい場合.
		String c;
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
		return new SearchIterator(lock, base.snapshot(desc, start), end);
	}
	
	@Override
	protected OperateIterator _index(Lock lock, LevelIndexIterator itr, Comparable endKey, String[] columns) {
		if(endKey == null) {
			return new IndexIterator(lock, itr, null, null);
		} else if(endKey instanceof String) {
			return new IndexIterator(lock, itr, (String)endKey, columns);
		} else {
			throw new LevelJsException("Range end key specification must be a string.");
		}
	}
	
	@Override
	public Object put(Object... params) {
		int len = params == null ? 0 : params.length;
		if(len == 0) {
			throw new LevelJsException("Key element information is not set.");
		}
		_r().lock();
		try {
			len --;
			final Object v = params[len];
			if(!(v instanceof Map)) {
				// valueがMapで無い場合はエラー.
				throw new LevelJsException("Element information must be set in Map format.");
			}
			if(len > 1) {
				final Object[] keys = new Object[len];
				System.arraycopy(params, 0, keys, 0, len);
				return _put((Map)v, len, keys);
			} else {
				return _put((Map)v, 0, null);
			}
		} finally {
			_r().unlock();
		}
	}

	
	// 検索iterator.
	private static final class SearchIterator implements OperateIterator {
		private LevelIterator src;
		private String end;
		
		private boolean exitFlag;
		private String beforeKey;
		private String nextKey;
		private Object nextValue;
		
		private Lock lock;
		private boolean reverse;
		private JsMap nextMap;
		
		public SearchIterator(Lock lk, LevelIterator it, String e) {
			src = it;
			reverse = it.isReverse();
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
				o = beforeKey;
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
		
		private boolean _next() {
			if(exitFlag || src.isClose() || !src.hasNext()) {
				close();
				return false;
			} else if(nextKey != null && nextValue != null) {
				return true;
			}
			nextValue = src.next();
			nextKey = (String)src.getKey();
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
		private Comparable end;
		private String[] columns;
		
		private boolean exitFlag;
		private String beforeKey;
		private String nextKey;
		private Object nextValue;
		
		private Lock lock;
		private boolean reverse;
		private JsMap nextMap;
		
		public IndexIterator(Lock lk, LevelIndexIterator s, String e, String[] c) {
			src = s;
			reverse = s.isReverse();
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
				Object o = src.getKey();
				if(o == null || !(o instanceof byte[])) {
					nextKey = null;
				} else {
					nextKey = Time12SequenceId.toString((byte[])o);
				}
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
