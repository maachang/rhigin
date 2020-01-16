package rhigin.lib.level.operator;

import java.util.Map;

import org.maachang.leveldb.operator.LevelQueue;

import rhigin.scripts.JsMap;

/**
 * キューオペレータ.
 */
public class QueueOperator implements Operator {
	private LevelQueue queue;
	private String name;
	
	/**
	 * コンストラクタ.
	 * @param q
	 */
	public QueueOperator(String n, LevelQueue q) {
		queue = q;
		name = n;
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
		Object o = queue.get();
		if(o == null) {
			return null;
		}
		return new JsMap((Map)o);
	}
	
	/**
	 * オペレータ名を取得.
	 * @return String オペレータ名が返却されます.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * オペレータタイプを取得.
	 * @return String オペレータタイプが返却されます.
	 */
	public String getOperatorType() {
		return OperatorUtil.getOperatorType(queue);
	}
	
	/**
	 * オブジェクトが利用可能かチェック.
	 * @return [true]の場合利用可能です.
	 */
	public boolean isAvailable() {
		return !queue.isClose();
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
	public OperatorMode getMode() {
		return new OperatorMode(queue.getOption());
	}
}
