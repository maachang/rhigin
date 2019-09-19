package rhigin.scripts;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * rhiginThreadPool.
 */
public class RhiginThreadPool {
	protected RhiginThreadPool() {
	}

	private static final RhiginThreadPool THIS = new RhiginThreadPool();

	public static final RhiginThreadPool getInstance() {
		return THIS;
	}

	// スレッドプーリングサービズ.
	private ScheduledExecutorService service = null;

	/**
	 * デフォルトサイズで、スレッドプールを作成.
	 */
	public final void newThreadPool() {
		// CPU数に合わせて設定.
		newThreadPool(-1);
	}

	/**
	 * スレッドプールを作成.
	 * 
	 * @param len
	 *            作成するスレッドプール数を設定します.
	 */
	public final void newThreadPool(int len) {
		if (service == null) {
			if (len <= 0) {
				// CPU数に合わせて設定.
				len = java.lang.Runtime.getRuntime().availableProcessors();
			}
			service = Executors.newScheduledThreadPool(len);
		}
	}

	/**
	 * スレッドプールオブジェクトを取得.
	 * 
	 * @return
	 */
	public final ScheduledExecutorService getService() {
		return service;
	}
}
