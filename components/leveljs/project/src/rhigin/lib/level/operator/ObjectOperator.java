package rhigin.lib.level.operator;

import java.util.Map;
import java.util.NoSuchElementException;

import org.maachang.leveldb.operator.LevelIndex.LevelIndexIterator;
import org.maachang.leveldb.operator.LevelIndexOperator;
import org.maachang.leveldb.operator.LevelIterator;
import org.maachang.leveldb.operator.LevelMap;

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
	
	public ObjectOperator(LevelJsCloseable c, String n, LevelMap o) {
		this.closeable = c;
		this.base = o;
		this.name = n;
		this.keyType = OperatorKeyType.getKeyType(o.getOption());
	}

	@Override
	protected LevelIndexOperator _operator() {
		return base;
	}

	@Override
	protected Object _put(Map value, int keyLen, Object[] keys) {
		final Object key = OperatorKeyType.convertKeyType(keyType, keys[0]);
		base.put(key, value);
		return null;
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
	protected OperateIterator _iterator(boolean desc, int keyLen, Object[] keys) {
		final Object key = OperatorKeyType.convertKeyType(keyType, keys[0]);
		return new SearchIterator(base.snapshot(desc, key), keyType, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected OperateIterator _range(boolean desc, int keyLen, Object[] keys) {
		if(keyLen < 2) {
			throw new LevelJsException("Two keys are required. \"0\" start key \"1\" end key");
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
		return new SearchIterator(base.snapshot(desc, start), keyType, end);
	}
	
	@Override
	protected OperateIterator _index(LevelIndexIterator itr) {
		return new IndexIterator(keyType, itr);
	}
	
	// 検索iterator.
	private static final class SearchIterator implements OperateIterator {
		private LevelIterator src;
		private int keyType;
		private Comparable end;
		
		private boolean exitFlag;
		private Object beforeKey;
		private Object nextKey;
		private Object nextValue;
		
		public SearchIterator(LevelIterator it, int k, Comparable e) {
			src = it;
			keyType = k;
			end = e;
			exitFlag = false;
		}
		
		/**
		 * クローズ処理.
		 */
		public void close() {
			src.close();
		}
		
		/**
		 * クローズ済みかチェック.
		 * @return
		 */
		public boolean isClose() {
			return src.isClose();
		}
		
		/**
		 * 降順か取得.
		 * @return
		 */
		public boolean isDesc() {
			return src.isReverse();
		}
		
		/**
		 * キー名を取得.
		 * @return Object キー名が返却されます.
		 */
		public Object key() {
			return new FixedArray(OperatorKeyType.getRestoreKey(keyType, beforeKey));
		}

		@Override
		public boolean hasNext() {
			return _next();
		}

		@Override
		public Map next() {
			if(nextKey == null && nextValue == null) {
				if(!_next()) {
					throw new NoSuchElementException();
				}
			}
			beforeKey = nextKey;
			Object ret = nextValue;
			nextKey = null; nextValue = null;
			return new JsMap((Map)ret);
		}
		
		
		@SuppressWarnings("unchecked")
		private boolean _next() {
			if(exitFlag || src.isClose()) {
				close();
				return false;
			} else if(nextKey != null && nextValue != null) {
				return true;
			}
			nextValue = src.next();
			nextKey = src.getKey();
			if(end != null && end.compareTo(nextKey) < 0) {
				// 終端を検出.
				exitFlag = true;
			}
			return true;
		}
	}
	
	// indexIterator.
	private static final class IndexIterator implements OperateIterator {
		LevelIndexIterator src;
		int keyType;
		public IndexIterator(int type, LevelIndexIterator s) {
			src = s;
			keyType = type;
		}

		@Override
		public boolean hasNext() {
			return src.hasNext();
		}

		@Override
		public Map next() {
			return src.next();
		}

		@Override
		public void close() {
			src.close();
		}

		@Override
		public boolean isClose() {
			return src.isClose();
		}

		@Override
		public boolean isDesc() {
			return src.isReverse();
		}

		@Override
		public Object key() {
			return new FixedArray(OperatorKeyType.getRestoreKey(keyType, src.getKey()));
		}
	}
}
