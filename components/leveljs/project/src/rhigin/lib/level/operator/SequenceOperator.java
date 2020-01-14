package rhigin.lib.level.operator;

import java.util.Map;

import org.maachang.leveldb.operator.LevelIndex.LevelIndexIterator;
import org.maachang.leveldb.operator.LevelIndexOperator;
import org.maachang.leveldb.operator.LevelSequence;

/**
 * シーケンスオペレータ.
 */
@SuppressWarnings("rawtypes")
public class SequenceOperator extends Operator {
	private LevelSequence base = null;
	
	public SequenceOperator(LevelSequence o) {
		this.base = o;
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
