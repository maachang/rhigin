package rhigin.lib.level.operator;

import java.util.Map;

import org.maachang.leveldb.operator.LevelIndex.LevelIndexIterator;

import rhigin.lib.level.runner.OperatorKeyType;

import org.maachang.leveldb.operator.LevelIndexOperator;
import org.maachang.leveldb.operator.LevelLatLon;

/**
 * 緯度経度オペレータ.
 */
@SuppressWarnings("rawtypes")
public class LatLonOperator extends Operator {
	private LevelLatLon base;
	private int keyType;
	
	public LatLonOperator(LevelLatLon o) {
		this.base = o;
		keyType = OperatorKeyType.getKeyType(o.getOption());
	}

	@Override
	protected LevelIndexOperator _operator() {
		return base;
	}

	@Override
	protected Object _put(Map value, int keyLen, Object[] keys) {
		return null;
	}

	@Override
	protected void _remove(int keyLen, Object[] keys) {
	}

	@Override
	protected Object _get(int keyLen, Object[] keys) {
		return null;
	}

	@Override
	protected boolean _contains(int keyLen, Object[] keys) {
		return false;
	}

	@Override
	protected OperateIterator _iterator(boolean desc, int keyLen, Object[] keys) {
		return null;
	}

	@Override
	protected OperateIterator _search(boolean desc, int keyLen, Object[] keys) {
		return null;
	}
	
	@Override
	protected OperateIterator _index(LevelIndexIterator itr) {
		return null;
	}
}
