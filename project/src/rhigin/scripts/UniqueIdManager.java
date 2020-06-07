package rhigin.scripts;

import rhigin.util.RandomUUID;
import rhigin.util.UniqueId;

/**
 * UniqueIdオブジェクトの管理.
 */
public class UniqueIdManager {
	private UniqueIdManager() {}
	
	// uniqueId管理.
	private static final ThreadLocal<UniqueId> local = new ThreadLocal<UniqueId>();
	private static final int RANDOM_COUNT = 8192 - 1;

	/**
	 * スレッド別に作成.
	 * @return UniqueId スレッド別のUniqueIdが返却されます.
	 */
	public static final UniqueId get() {
		UniqueId ret = local.get();
		if (ret == null) {
			RandomUUID uuid = new RandomUUID();
			uuid.getId((int) (System.nanoTime() & RANDOM_COUNT));
			ret = new UniqueId(uuid);
			local.set(ret);
		}
		return ret;
	}
}
