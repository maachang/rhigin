package rhigin.scripts;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Functionオブジェクトのキャッシュを保持.
 */
public class CacheFunction {
	private int length = ScriptConstants.DEF_CACHE_FUNCTION_LENGTH;
	private Map<String, Queue<AbstractRhiginFunction>> top = new ConcurrentHashMap<String, Queue<AbstractRhiginFunction>>();
	private static final CacheFunction SNGL = new CacheFunction();
	
	/**
	 * シングルトン.
	 * @return
	 */
	public static final CacheFunction getInstance() {
		return SNGL;
	}
	
	/**
	 * キャッシュクリア.
	 */
	public void clear() {
		top.clear();
	}
	
	/**
	 * キャッシュ最大数をセット.
	 * @param len
	 */
	public void setMaxCacheSize(int len) {
		if(len > 0) {
			if(len <= 5) {
				len = 5;
			}
			length = len;
		}
	}
	
	/**
	 * キャッシュサイズを取得.
	 * @return
	 */
	public int getMaxCacheSize() {
		return length;
	}
	
	/**
	 * 利用が終わったキャッシュをセット.
	 * @param name
	 * @param f
	 * @return
	 */
	public boolean push(String name, AbstractRhiginFunction f) {
		if(name == null || f == null) {
			return false;
		}
		Queue<AbstractRhiginFunction> q = top.get(name);
		if(q == null) {
			q = new ConcurrentLinkedQueue<AbstractRhiginFunction>();
		}
		if(q.size() > length) {
			return false;
		}
		f.clear();
		q.offer(f);
		return true;
	}
	
	/**
	 * 再利用可能なキャッシュを取得.
	 * @param name
	 * @return
	 */
	public AbstractRhiginFunction pop(String name) {
		if(name == null) {
			return null;
		}
		Queue<AbstractRhiginFunction> q = top.get(name);
		if(q == null) {
			return null;
		}
		return q.poll();
	}
}
