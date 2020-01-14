package rhigin.lib.level.operator;

import java.util.Map;

import org.maachang.leveldb.operator.LevelQueue;

import rhigin.lib.level.runner.LevelMode;

/**
 * キューオペレータ.
 */
public class QueueOperator {
	private LevelQueue queue;
	
	/**
	 * コンストラクタ.
	 * @param q
	 */
	public QueueOperator(LevelQueue q) {
		queue = q;
	}
	
	/**
	 * オブジェクトが利用可能かチェック.
	 * @return [true]の場合利用可能です.
	 */
	public boolean isAvailable() {
		return !queue.isClose();
	}
	
	/**
	 * キューに情報を追加.
	 * @param o
	 * @return
	 */
	public boolean offer(Object o) {
		if(o instanceof Map) {
			queue.add(o);
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
		return (Map)queue.get();
	}
	
	/**
	 * 情報が空かチェック.
	 * 
	 * @return boolean [true]の場合、空です.
	 */
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	/**
	 * LevelModeを取得.
	 * @return
	 */
	public LevelMode getMode() {
		return new LevelMode(queue.getOption());
	}
}
