package rhigin.lib.level.operator;

import java.util.Iterator;
import java.util.Map;

/**
 * Operator用のIterator.
 */
@SuppressWarnings("rawtypes")
public interface OperateIterator extends Iterator<Map> {
	/**
	 * クローズ処理.
	 */
	public void close();

	/**
	 * クローズ済みかチェック.
	 * @return
	 */
	public boolean isClose();
	
	/**
	 * 降順か取得.
	 * @return
	 */
	public boolean isDesc();
	
	/**
	 * キー情報を取得.
	 * @return
	 */
	public Object key();
}
