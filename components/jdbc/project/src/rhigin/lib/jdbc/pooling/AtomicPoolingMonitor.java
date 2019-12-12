package rhigin.lib.jdbc.pooling;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import rhigin.util.Flag;

/**
 * プーリングタイムアウトチェック.
 */
public class AtomicPoolingMonitor extends Thread {

	/** スレッド停止フラグ. **/
	private final Flag stopFlag = new Flag();

	/** 監視オブジェクト. **/
	private final Queue<AtomicPooling> pool = new ConcurrentLinkedQueue<AtomicPooling>();

	/**
	 * コンストラクタ.
	 */
	public AtomicPoolingMonitor() {
		this.setDaemon(true);
	}
	
	/**
	 * スレッド開始.
	 */
	public void startThread() {
		this.stopFlag.set(false);
		this.start();
	}

	/**
	 * スレッド停止.
	 */
	public void stopThread() {
		this.stopFlag.set(true);
	}

	/**
	 * スレッドが停止しているかチェック.
	 * 
	 * @return boolean [true]の場合、スレッド停止しています.
	 */
	public boolean isStop() {
		return stopFlag.get();
	}

	/**
	 * 監視対象のPoolingManagerをセット.
	 * 
	 * @param man 対象のプーリングマネージャを設定します.
	 */
	public void setPooling(AtomicPooling man) {
		pool.offer(man);
	}

	/**
	 * 監視対象のPoolingManagerをクリア.
	 * 
	 * @param man 対象のプーリングマネージャを設定します.
	 */
	public void clearPooling(AtomicPooling man) {
		Iterator<AtomicPooling> it = pool.iterator();
		while (it.hasNext()) {
			if (it.next() == man) {
				it.remove();
			}
		}
	}

	private static final long DEF_SLEEP = 50;
	private static final long NO_DATA_SLEEP = 100;

	/**
	 * スレッド実行.
	 */
	public void run() {

		ThreadDeath tdObject = null;
		boolean endFlag = false;

		AtomicPooling man;
		AtomicPoolConnection c;
		Iterator<AtomicPooling> mans;
		Iterator<SoftReference<AtomicPoolConnection>> conns;

		while (!stopFlag.get()) {
			try {
				// スレッド停止.
				if (endFlag) {
					break;
				}

				// 監視対象の情報が存在する場合.
				if (pool.size() > 0) {

					mans = pool.iterator();

					while (mans.hasNext()) {

						// 一定時間待機.
						Thread.sleep(DEF_SLEEP);

						// プーリングマネージャを取得.
						// オブジェクトが既に破棄されている場合は処理しない.
						if ((man = mans.next()).size() > 0 && !man.isDestroy()) {

							try {

								// プーリングオブジェクト群を処理.
								conns = man.pooling.iterator();

								// オブジェクトが既に破棄されている場合は処理しない.
								while (!man.isDestroy() && conns.hasNext()) {

									// 一定時間待機.
									Thread.sleep(DEF_SLEEP);

									// 定義されたコネクション条件のタイムアウトチェック.
									if ((c = (conns.next()).get()) == null) {
										conns.remove();
									} else if (c.lastTime() + man.timeout < System.currentTimeMillis()) {
										// タイムアウト値を越えた場合は、削除処理.
										conns.remove();
										c.destroy();
									}

								}

							} catch (Exception e) {
							}
						}

					}

				}

				// 一定時間待機.
				Thread.sleep(NO_DATA_SLEEP);

			} catch (InterruptedException ie) {
				endFlag = true;
			} catch (ThreadDeath td) {
				tdObject = td;
				endFlag = true;
			} catch (Throwable t) {
				// InterruptedException.
				// ThreadDeath
				// これ以外の例外は無視.
			}
		}
		// 情報破棄.
		pool.clear();
		// 後処理.
		stopFlag.set(true);
		if (tdObject != null) {
			throw tdObject;
		}
	}
}
