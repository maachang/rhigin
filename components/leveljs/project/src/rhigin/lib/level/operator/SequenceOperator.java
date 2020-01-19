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
		final Object key = Converter.convertString(keys[0]);
		return new SearchIterator(lock, base.snapshot(desc, key), null);
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
	protected OperateIterator _index(Lock lock, LevelIndexIterator itr) {
		return new IndexIterator(lock, itr);
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
		
		public SearchIterator(Lock lk, LevelIterator it, String e) {
			src = it;
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
			lock.lock();
			try {
				return src.isReverse();
			} finally {
				lock.unlock();
			}
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
			return new JsMap((Map)ret);
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
			if(end != null && end.compareTo(nextKey) <= 0) {
				// 終端を検出.
				exitFlag = true;
			}
			return true;
		}
	}
	
	// indexIterator.
	private static final class IndexIterator implements OperateIterator {
		private LevelIndexIterator src;
		private Lock lock;
		
		public IndexIterator(Lock lk, LevelIndexIterator s) {
			src = s;
			lock = lk;
		}

		@Override
		public boolean hasNext() {
			lock.lock();
			try {
				return src.hasNext();
			} finally {
				lock.unlock();
			}
		}

		@Override
		public Map next() {
			lock.lock();
			try {
				return src.next();
			} finally {
				lock.unlock();
			}
		}

		@Override
		public void close() {
			lock.lock();
			try {
				src.close();
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
			lock.lock();
			try {
				return src.isReverse();
			} finally {
				lock.unlock();
			}
		}

		@Override
		public Object key() {
			Object o;
			lock.lock();
			try {
				o = src.getKey();
			} finally {
				lock.unlock();
			}
			if(o == null || !(o instanceof byte[])) {
				return new FixedArray(null);
			}
			return new FixedArray(Time12SequenceId.toString((byte[])o));
		}
	}
}
