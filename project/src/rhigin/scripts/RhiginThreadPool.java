package rhigin.scripts;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * rhiginThreadPool.
 */
public class RhiginThreadPool {
	protected RhiginThreadPool() {}
	private static final RhiginThreadPool THIS = new RhiginThreadPool();
	public static final RhiginThreadPool getInstance() {
		return THIS;
	}
	
	private static final int DEF_POOL_LENGTH = 5;
	private ScheduledExecutorService service = null;
	
	/**
	 * デフォルトサイズで、スレッドプールを作成.
	 */
	public final void newThreadPool() {
		newThreadPool(DEF_POOL_LENGTH);
	}
	
	/**
	 * スレッドプールを作成.
	 * @param len　作成するスレッドプール数を設定します.
	 */
	public final void newThreadPool(int len) {
		if(service == null) {
			service = Executors.newScheduledThreadPool(len);
		}
	}
	
	/**
	 * スレッドプールオブジェクトを取得.
	 * @return
	 */
	public final ScheduledExecutorService getService() {
		return service;
	}
}
