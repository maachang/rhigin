package rhigin.lib.level.operator;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;

import org.maachang.leveldb.Time12SequenceId;
import org.maachang.leveldb.operator.LevelIndex.LevelIndexIterator;
import org.maachang.leveldb.operator.LevelIndexOperator;
import org.maachang.leveldb.operator.LevelIterator;
import org.maachang.leveldb.operator.LevelLatLon;
import org.maachang.leveldb.types.TwoKey;
import org.maachang.leveldb.util.Converter;
import org.maachang.leveldb.util.GeoQuadKey;

import rhigin.lib.level.runner.LevelJsCloseable;
import rhigin.lib.level.runner.LevelJsException;
import rhigin.scripts.JavaScriptable;
import rhigin.scripts.JsMap;
import rhigin.util.FixedArray;

/**
 * 緯度経度オペレータ.
 */
@SuppressWarnings("rawtypes")
public class LatLonOperator extends SearchOperator {
	protected LevelLatLon base;
	protected int keyType;
	
	/**
	 * コンストラクタ.
	 * @param c Closeableオブジェクトを設定.
	 * @param n オペレータ名を設定.
	 * @param o オペレータを設定.
	 */
	public LatLonOperator(LevelJsCloseable c, String n, LevelLatLon o) {
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

	// キー情報を取得.
	private final Object[] _getKeys(final int keyLen, final Object[] keys) {
		Long qk = null;
		Object secKey = null;
		boolean seqId = false;
		// セカンドキーがシーケンスID発行の場合.
		if((seqId = base.isSecondKeyBySequenceId())) {
			if(keyLen == 1 && Converter.isNumeric(keys[0])) {
				qk = Converter.convertLong(keys[0]);
			} else if(keyLen == 2 && Converter.isFloat(keys[0]) && Converter.isFloat(keys[1])) {
				qk = GeoQuadKey.create(Converter.convertDouble(keys[0]), Converter.convertDouble(keys[1]));
			}
		}
		// クアッドキーが取得されていない場合.
		if(qk == null) {
			if(keyLen == 2 && Converter.isNumeric(keys[0]) && !Converter.isFloat(keys[0])) {
				qk = Converter.convertLong(keys[0]);
				secKey = keys[1];
			} else if(keyLen == 3 && Converter.isFloat(keys[0]) && Converter.isFloat(keys[1])) {
				qk = GeoQuadKey.create(Converter.convertDouble(keys[0]), Converter.convertDouble(keys[1]));
				secKey = keys[1];
			} else {
				throw new LevelJsException("The key specification is invalid.");
			}
		}
		// セカンドキーがシーケンスIDの場合.
		if(seqId) {
			if(secKey != null) {
				// 情報が指定されている場合はチェック.
				if(secKey instanceof String) {
					secKey = Time12SequenceId.toBinary((String)secKey);
				}
				if(!(secKey instanceof byte[]) || ((byte[])secKey).length != Time12SequenceId.ID_LENGTH) {
					throw new LevelJsException("Sequence ID specification is invalid.");
				}
			}
		// セカンドキーが指定された型の場合.
		} else {
			if(secKey == null) {
				throw new LevelJsException("Second key not set.");
			}
			// 情報が設定されている場合は、キータイプで変換.
			secKey = OperatorKeyType.convertKeyType(keyType, secKey);
		}
		return new Object[] {qk, secKey};
	}

	@Override
	protected Object _put(Map value, int keyLen, Object[] keys) {
		Object[] params = _getKeys(keyLen, keys);
		Object[] result = base.put((Long)params[0], params[1], value);
		if(!base.isSecondKeyBySequenceId()) {
			result[1] = OperatorKeyType.getRestoreKey(keyType, result[1]);
		}
		return new JavaScriptable.ReadArray(result);
	}

	@Override
	protected void _remove(int keyLen, Object[] keys) {
		Object[] params = _getKeys(keyLen, keys);
		base.remove((Long)params[0], params[1]);
	}

	@Override
	protected Object _get(int keyLen, Object[] keys) {
		Object[] params = _getKeys(keyLen, keys);
		return base.get((Long)params[0], params[1]);
	}

	@Override
	protected boolean _contains(int keyLen, Object[] keys) {
		Object[] params = _getKeys(keyLen, keys);
		return base.contains((Long)params[0], params[1]);
	}

	@Override
	protected OperateIterator _iterator(Lock lock, boolean desc, int keyLen, Object[] keys) {
		Object[] params = _getKeys(keyLen, keys);
		return new SearchIterator(lock, false, base.isSecondKeyBySequenceId(), keyType,
			base.snapshot(desc, (Long)params[0], params[1]));
	}

	@Override
	protected OperateIterator _range(Lock lock, boolean desc, int keyLen, Object[] keys) {
		// 範囲取得なので、降順・昇順の取得はできない.
		long qk;
		int distance;
		if(keyLen == 2 && Converter.isNumeric(keys[0]) && Converter.isNumeric(keys[1])) {
			qk = Converter.convertLong(keys[0]);
			distance = Converter.convertInt(keys[1]);
		} else if(keyLen == 3 && Converter.isFloat(keys[0]) && Converter.isFloat(keys[1]) && Converter.isNumeric(keys[2])) {
			qk = GeoQuadKey.create(Converter.convertDouble(keys[0]), Converter.convertDouble(keys[1]));
			distance = Converter.convertInt(keys[2]);
		} else {
			throw new LevelJsException("The key specification is invalid.");
		}
		return new SearchIterator(lock, true, base.isSecondKeyBySequenceId(), keyType,
			base.snapshot(qk, distance));
	}
	
	@Override
	protected OperateIterator _index(Lock lock, LevelIndexIterator itr, Comparable endKey, String[] columns) {
		return new IndexIterator(lock, base.isSecondKeyBySequenceId(), keyType, itr, endKey, columns);
	}
	
	// iteratorのキー変換.
	protected static final Object _resultKey(boolean seqFlg, int keyType, Object key) {
		if(key == null) {
			return null;
		}
		Long qk;
		Object secKey;
		if(key instanceof Object[]) {
			qk = (Long)((Object[])key)[0];
			secKey = ((Object[])key)[1];
		} else if(key instanceof TwoKey) {
			qk = (Long)((TwoKey)key).get(0);
			secKey = ((TwoKey)key).get(0);
		} else {
			throw new LevelJsException("Unknown key object type.");
		}
		double[] d = GeoQuadKey.latLon(qk);
		return new FixedArray(new Object[] {
			d[0], d[1], seqFlg ? secKey : OperatorKeyType.getRestoreKey(keyType, secKey)
		});
	}
	
	// 検索iterator.
	private static final class SearchIterator implements OperateIterator {
		private boolean searchFlag;
		private boolean sequenceFlag;
		private LevelIterator src;
		private int keyType;
		
		private Lock lock;
		private JsMap nextMap;
		
		public SearchIterator(Lock lk, boolean sf, boolean f, int k, LevelIterator it) {
			searchFlag = sf;
			sequenceFlag = f;
			src = it;
			keyType = k;
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
				return searchFlag ? false : src.isReverse();
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
				o = src.getKey();
			} finally {
				lock.unlock();
			}
			return LatLonOperator._resultKey(sequenceFlag, keyType, o);
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
				if(nextMap == null) {
					nextMap = new JsMap(src.next());
				} else {
					nextMap.set(src.next());
				}
				return nextMap;
			} finally {
				lock.unlock();
			}
		}
	}
	
	// indexIterator.
	private static final class IndexIterator implements OperateIterator {
		private boolean sequenceFlag;
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
		
		public IndexIterator(Lock lk, boolean f, int k, LevelIndexIterator s, Comparable e, String[] c) {
			sequenceFlag = f;
			reverse = s.isReverse();
			keyType = k;
			src = s;
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
				nextKey = LatLonOperator._resultKey(sequenceFlag, keyType, src.getKey());
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
