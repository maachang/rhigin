package rhigin.lib.level.operator;

import java.util.Map;
import java.util.NoSuchElementException;

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
	
	public SequenceOperator(LevelJsCloseable c, String n, LevelSequence o) {
		this.closeable = c;
		this.base = o;
		this.name = n;
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
	protected OperateIterator _iterator(boolean desc, int keyLen, Object[] keys) {
		final Object key = Converter.convertString(keys[0]);
		return new SearchIterator(base.snapshot(desc, key), null);
	}

	@Override
	protected OperateIterator _range(boolean desc, int keyLen, Object[] keys) {
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
		return new SearchIterator(base.snapshot(desc, start), end);
	}
	
	@Override
	protected OperateIterator _index(LevelIndexIterator itr) {
		return new IndexIterator(itr);
	}
	
	// 検索iterator.
	private static final class SearchIterator implements OperateIterator {
		private LevelIterator src;
		private String end;
		
		private boolean exitFlag;
		private String beforeKey;
		private String nextKey;
		private Object nextValue;
		
		public SearchIterator(LevelIterator it, String e) {
			src = it;
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
			return new FixedArray(beforeKey);
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
		
		private boolean _next() {
			if(exitFlag || src.isClose()) {
				close();
				return false;
			} else if(nextKey != null && nextValue != null) {
				return true;
			}
			nextValue = src.next();
			nextKey = (String)src.getKey();
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
		public IndexIterator(LevelIndexIterator s) {
			src = s;
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
			Object o = src.getKey();
			if(o == null || !(o instanceof byte[])) {
				return new FixedArray(null);
			}
			return new FixedArray(Time12SequenceId.toString((byte[])o));
		}
	}
}
