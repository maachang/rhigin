package rhigin.lib.level.operator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.maachang.leveldb.LevelOption;
import org.maachang.leveldb.operator.LevelIndex;
import org.maachang.leveldb.operator.LevelIndex.LevelIndexIterator;
import org.maachang.leveldb.operator.LevelIndexOperator;
import org.maachang.leveldb.util.Converter;

import rhigin.lib.level.runner.LevelJsCloseable;
import rhigin.lib.level.runner.LevelJsException;
import rhigin.scripts.JsMap;
import rhigin.util.FixedArray;

/**
 * 検索対応オペレータ.
 */
@SuppressWarnings("rawtypes")
public abstract class SearchOperator implements Operator {
	protected String name;
	protected LevelJsCloseable closeable;
	
	/**
	 * LevelIndexOperatorを取得.
	 * @return
	 */
	protected abstract LevelIndexOperator _operator();
	
	/**
	 * データ追加・更新の実装.
	 * @param value 追加、更新要素を設定します.
	 * @param keyLen キー数を設定します.
	 * @param keys キー情報を設定します.
	 * @return
	 */
	protected abstract Object _put(Map value, int keyLen, Object[] keys);
	
	/**
	 * データ削除の実装.
	 * @param keyLen キー数を設定します.
	 * @params keys キーを設定します.
	 */
	protected abstract void _remove(int keyLen, Object[] keys);
	
	
	/**
	 * データ取得の実装.
	 * @param keyLen キー数を設定します.
	 * @param keys キーを設定します.
	 * @return Object データが返却されます.
	 */
	protected abstract Object _get(int keyLen, Object[] keys);
	
	/**
	 * データ取得.
	 * @param keyLen キー数を設定します.
	 * @param keys キーを設定します.
	 * @return Map データが返却されます.
	 */
	protected abstract boolean _contains(int keyLen, Object[] keys);
	
	/**
	 * カーソル取得.
	 * @param lock 読み込みロックガセットされます.
	 * @param desc 降順で取得する場合[true].
	 * @param keyLen キー数を設定します.
	 * @param keys キーを設定します.
	 * @return OperateIterator OperateIteratorが返却されます.
	 */
	protected abstract OperateIterator _iterator(Lock lock, boolean desc, int keyLen, Object[] keys);
	
	/**
	 * 範囲検索のカーソル取得.
	 * @param lock 読み込みロックガセットされます.
	 * @param desc 降順で取得する場合[true].
	 * @param keyLen キー数を設定します.
	 * @param keys キーを設定します.
	 * @return OperateIterator OperateIteratorが返却されます.
	 */
	protected abstract OperateIterator _range(Lock lock, boolean desc, int keyLen, Object[] keys);
	
	/**
	 * インデックスで検索、取得.
	 * @param lock 読み込みロックガセットされます.
	 * @param itr インデックスIteratorを設定します.
	 * @return
	 */
	protected abstract OperateIterator _index(Lock lock, LevelIndexIterator itr);
	
	/**
	 * Readロックオブジェクトを取得.
	 * @return
	 */
	protected Lock _r() {
		return _operator().getLock().readLock();
	}
	
	/**
	 * Writeロックオブジェクトを取得.
	 * @return
	 */
	protected Lock _w() {
		return _operator().getLock().writeLock();
	}
	
	/**
	 * オペレータのクローズ.
	 */
	@Override
	public void close() {
		_w().lock();
		try {
			_operator().close();
		} finally {
			_w().unlock();
		}
	}
	
	/**
	 * データ削除処理.
	 * @return boolean [true]の場合、データ削除成功です.
	 */
	@Override
	public boolean trancate() {
		_w().lock();
		try {
			return _operator().trancate();
		} finally {
			_w().unlock();
		}
	}
	
	/**
	 * オペレータ名を取得.
	 * @return String オペレータ名が返却されます.
	 */
	@Override
	public String getName() {
		_r().lock();
		try {
			return name;
		} finally {
			_r().unlock();
		}
	}
	
	/**
	 * オペレータタイプを取得.
	 * @return String オペレータタイプが返却されます.
	 */
	@Override
	public String getOperatorType() {
		_r().lock();
		try {
			return OperatorUtil.getOperatorType(_operator());
		} finally {
			_r().unlock();
		}
	}
	
	/**
	 * オブジェクトが利用可能かチェック.
	 * @return [true]の場合利用可能です.
	 */
	@Override
	public boolean isAvailable() {
		_r().lock();
		try {
			return !_operator().isClose();
		} finally {
			_r().unlock();
		}
	}
	
	/**
	 * 情報が空かチェック.
	 * 
	 * @return boolean [true]の場合、空です.
	 */
	@Override
	public boolean isEmpty() {
		_r().lock();
		try {
			return _operator().getLeveldb().isEmpty();
		} finally {
			_r().unlock();
		}
	}
	
	/**
	 * LevelModeを取得.
	 * @return
	 */
	@Override
	public OperatorMode getMode() {
		LevelOption op;
		_r().lock();
		try {
			op = _operator().getOption();
		} finally {
			_r().unlock();
		}
		return new OperatorMode(op);
	}
	
	/**
	 * WriteBatchモードを取得.
	 * @return
	 */
	public boolean isWriteBatch() {
		_r().lock();
		try {
			return _operator().isWriteBatch();
		} finally {
			_r().unlock();
		}
	}
	
	/**
	 * コミット処理.
	 * @return boolean
	 */
	public boolean commit() {
		_r().lock();
		try {
			if(_operator().isWriteBatch()) {
				_operator().commit();
				return true;
			}
			return false;
		} finally {
			_r().unlock();
		}
	}
	
	/**
	 * ロールバック処理.
	 * @return boolean
	 */
	public boolean rollback() {
		_r().lock();
		try {
			if(_operator().isWriteBatch()) {
				_operator().rollback();
				return true;
			}
			return false;
		} finally {
			_r().unlock();
		}
	}
	
	/**
	 * インデックスの追加.
	 * @param columnType カラムタイプを設定します.
	 * @param columnName カラム名を設定します.
	 *                   設定方法は、hoge.moge.abc や "hoge", "moge", "abc"のように階層設定可能.
	 */
	public void createIndex(String columnType, String... columnName) {
		if(columnName == null || columnName.length == 0) {
			throw new LevelJsException("Column name is not set.");
		}
		_w().lock();
		try {
			int type = LevelIndex.convertStringByColumnType(columnType);
			if(type != LevelIndex.INDEX_STRING &&
				type != LevelIndex.INDEX_INT &&
				type != LevelIndex.INDEX_LONG &&
				type != LevelIndex.INDEX_FLOAT &&
				type != LevelIndex.INDEX_DOUBLE) {
				throw new LevelJsException("Invalid setting for index column: " + columnType);
			}
			_operator().createIndex(type, columnName);
		} finally {
			_w().unlock();
		}
	}
	
	/**
	 * インデックスの削除.
	 * @param columnName カラム名を設定します.
	 *                   設定方法は、hoge.moge.abc や "hoge", "moge", "abc"のように階層設定可能.
	 */
	public void deleteIndex(String... columnName) {
		if(columnName == null || columnName.length == 0) {
			throw new LevelJsException("Column name is not set.");
		}
		_w().lock();
		try {
			_operator().deleteIndex(columnName);
		} finally {
			_w().unlock();
		}
	}
	
	/*
	 * インデックスの存在チェック.
	 * @param columnName カラム名を設定します.
	 *                   設定方法は、hoge.moge.abc や "hoge", "moge", "abc"のように階層設定可能.
	 */
	public boolean isIndex(String... columnName) {
		if(columnName == null || columnName.length == 0) {
			throw new LevelJsException("Column name is not set.");
		}
		_r().lock();
		try {
			return _operator().isIndex(columnName);
		} finally {
			_r().unlock();
		}
	}
	
	/**
	 * 登録インデックス数を取得.
	 * 
	 * @return int インデックス数を取得します.
	 */
	public int indexSize() {
		_r().lock();
		try {
			return _operator().indexSize();
		} finally {
			_r().unlock();
		}
	}
	
	/**
	 * インデックスカラム名群を取得.
	 * 
	 * @return List<String> インデックスカラム名群が返却されます.
	 */
	public List<String> indexs() {
		String[] ret;
		_r().lock();
		try {
			ret = _operator().indexColumns();
		} finally {
			_r().unlock();
		}
		return new FixedArray<String>(ret);
	}
	
	// indexの要素変換.
	protected static final Object trimIndexKey(LevelIndex idx, Object key) {
		if(key == null) {
			return null;
		}
		switch(idx.getColumnType()) {
		case LevelIndex.INDEX_STRING:
			return Converter.convertString(key);
		case LevelIndex.INDEX_INT:
			if(Converter.isNumeric(key)) {
				return Converter.convertInt(key);
			}
			break;
		case LevelIndex.INDEX_LONG:
			if(Converter.isNumeric(key)) {
				return Converter.convertLong(key);
			}
			break;
		case LevelIndex.INDEX_FLOAT:
			if(Converter.isNumeric(key)) {
				return Converter.convertFloat(key);
			}
			break;
		case LevelIndex.INDEX_DOUBLE:
			if(Converter.isNumeric(key)) {
				return Converter.convertDouble(key);
			}
			break;
		}
		throw new LevelJsException("Search index format does not match.");
	}
	
	/**
	 * インデックスで検索、取得.
	 * @param desc [true]を設定した場合、降順で検索します.
	 * @param key 検索キーを設定します.
	 *            [null]を設定した場合、検索キーなしで昇順で取得します.
	 * @param columnName カラム名を設定します.
	 *                   設定方法は、hoge.moge.abc や "hoge", "moge", "abc"のように階層設定可能.
	 * @return
	 */
	public OperateIterator index(boolean desc, Object key, String... columnName) {
		if(columnName == null || columnName.length == 0) {
			throw new LevelJsException("Column name is not set.");
		}
		OperateIterator ret;
		_r().lock();
		try {
			final LevelIndex idx = _operator().getLevelIndex(columnName);
			if((key = trimIndexKey(idx, key)) == null) {
				ret = _index(_r(), idx.getSortIndex(desc));
			} else {
				ret = _index(_r(), idx.get(desc, key));
			}
			if(ret != null) {
				closeable.reg(ret);
			}
		} finally {
			_r().unlock();
		}
		return ret;
	}
	
	/**
	 * データ追加・更新.
	 * @param params keys.... value で設定します.
	 * @return 返却情報が存在する場合は、情報が返却されます.
	 */
	public Object put(Object... params) {
		int len = params == null ? 0 : params.length;
		if(len <= 1) {
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
			final Object[] keys = new Object[len];
			System.arraycopy(params, 0, keys, 0, len);
			return _put((Map)v, len, keys);
		} finally {
			_r().unlock();
		}
	}
	
	/**
	 * データ削除.
	 * @params keys キーを設定します.
	 */
	public void remove(Object... keys) {
		if(keys == null || keys.length == 0) {
			throw new LevelJsException("Search key information is not set.");
		}
		_r().lock();
		try {
			_remove(keys.length, keys);
		} finally {
			_r().unlock();
		}
	}
	
	/**
	 * データ取得.
	 * @param keys キーを設定します.
	 * @return Map データが返却されます.
	 */
	public Map get(Object... keys) {
		if(keys == null || keys.length == 0) {
			throw new LevelJsException("Search key information is not set.");
		}
		Object ret;
		_r().lock();
		try {
			ret = _get(keys.length, keys);
			if(ret == null) {
				return null;
			}
		} finally {
			_r().unlock();
		}
		return new JsMap((Map)ret);
	}
	
	/**
	 * データ取得.
	 * @param keys キーを設定します.
	 * @return boolean [true] の場合、存在します.
	 */
	public boolean contains(Object... keys) {
		if(keys == null || keys.length == 0) {
			throw new LevelJsException("Search key information is not set.");
		}
		_r().lock();
		try {
			return _contains(keys.length, keys);
		} finally {
			_r().unlock();
		}
	}
	
	/**
	 * カーソルの取得.
	 * @param keys キーを設定します.
	 * @return OperateIterator OperateIteratorが返却されます.
	 */
	public OperateIterator cursor(boolean desc, Object... keys) {
		_r().lock();
		try {
			final OperateIterator ret = _iterator(_r(), desc, keys == null ? 0 : keys.length, keys);
			if(ret != null) {
				closeable.reg(ret);
			}
			return ret;
		} finally {
			_r().unlock();
		}
	}
	
	/**
	 * 範囲検索のカーソル取得.
	 * @param keys キーを設定します.
	 * @return OperateIterator OperateIteratorが返却されます.
	 */
	public OperateIterator range(boolean desc, Object... keys) {
		_r().lock();
		try {
			final OperateIterator ret = _range(_r(), desc, keys == null ? 0 : keys.length, keys);
			if(ret != null) {
				closeable.reg(ret);
			}
			return ret;
		} finally {
			_r().unlock();
		}
	}
}
