package rhigin.lib.level.operator;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.maachang.leveldb.operator.LevelQueue;

import rhigin.scripts.JsMap;

/**
 * キューオペレータ.
 */
public class QueueOperator implements Operator {
	private LevelQueue queue;
	private String name;
	
	// rwlock.
	private final ReadWriteLock rw = new ReentrantReadWriteLock();
	
	/**
	 * コンストラクタ.
	 * @param q
	 */
	public QueueOperator(String n, LevelQueue q) {
		queue = q;
		name = n;
	}
	
	/**
	 * オペレータのクローズ.
	 */
	@Override
	public void close() {
		rw.writeLock().lock();
		try {
			queue.close();
		} finally {
			rw.writeLock().unlock();
		}
	}
	
	/**
	 * データ削除処理.
	 * @return boolean [true]の場合、データ削除成功です.
	 */
	@Override
	public boolean trancate() {
		rw.writeLock().lock();
		try {
			return queue.trancate();
		} finally {
			rw.writeLock().unlock();
		}
	}
	
	/**
	 * キューに情報を追加.
	 * @param o
	 * @return
	 */
	public boolean offer(Object o) {
		if(o instanceof Map) {
			rw.readLock().lock();
			try {
				queue.add(o);
			} finally {
				rw.readLock().unlock();
			}
			return true;
		}
		return false;
	}
	
	/**
	 * キューから情報を取得.
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Map pop() {
		Object ret;
		rw.readLock().lock();
		try {
			ret = queue.get();
		} finally {
			rw.readLock().unlock();
		}
		if(ret == null) {
			return null;
		}
		return new JsMap((Map)ret);
	}
	
	/**
	 * オペレータ名を取得.
	 * @return String オペレータ名が返却されます.
	 */
	@Override
	public String getName() {
		rw.readLock().lock();
		try {
			return name;
		} finally {
			rw.readLock().unlock();
		}
	}
	
	/**
	 * オペレータタイプを取得.
	 * @return String オペレータタイプが返却されます.
	 */
	@Override
	public String getOperatorType() {
		rw.readLock().lock();
		try {
			return OperatorUtil.getOperatorType(queue);
		} finally {
			rw.readLock().unlock();
		}
	}
	
	/**
	 * オブジェクトが利用可能かチェック.
	 * @return [true]の場合利用可能です.
	 */
	@Override
	public boolean isAvailable() {
		rw.readLock().lock();
		try {
			return !queue.isClose();
		} finally {
			rw.readLock().unlock();
		}
	}
	
	/**
	 * 情報が空かチェック.
	 * 
	 * @return boolean [true]の場合、空です.
	 */
	@Override
	public boolean isEmpty() {
		rw.readLock().lock();
		try {
			return queue.isEmpty();
		} finally {
			rw.readLock().unlock();
		}
	}
	
	/**
	 * LevelModeを取得.
	 * @return
	 */
	@Override
	public OperatorMode getMode() {
		rw.readLock().lock();
		try {
			return new OperatorMode(queue.getOption());
		} finally {
			rw.readLock().unlock();
		}
	}
}
