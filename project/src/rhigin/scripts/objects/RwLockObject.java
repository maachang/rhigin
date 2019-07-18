package rhigin.scripts.objects;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;

/**
 * [js]read / write ロックオブジェクト.
 * 
 * var rwLock = new RwLock("test");
 * 
 * // read lock unlock.
 * lock.rlock();
 * try {
 *  ...
 * } finally {
 *   lock.rulock();
 * }
 * 
 * // write lock unlock.
 * lock.wlock();
 * try {
 *  ...
 * } finally {
 *   lock.wulock();
 * }
 */
public class RwLockObject extends RhiginFunction {
	private static final RwLockObject THIS = new RwLockObject();
	public static final RwLockObject getInstance() {
		return THIS;
	}
	
	private final Map<String, WeakReference<ReadWriteLock>> locks = new ConcurrentHashMap<String, WeakReference<ReadWriteLock>>();
	
	@Override
	public String getName() {
		return "RwLock";
	}
	
	@Override
	public Scriptable construct(Context ctx, Scriptable thisObject, Object[] args) {
		ReadWriteLock lock = null;
		if(args.length > 0) {
			String key = "" + args[0];
			WeakReference<ReadWriteLock> w = locks.get(key);
			if(w == null || (lock = w.get()) == null) {
				lock = new ReentrantReadWriteLock();
				locks.put(key, new WeakReference<ReadWriteLock>(lock));
			}
		} else {
			lock = new ReentrantReadWriteLock();
		}
		return new RhiginObject("RwLock", new RhiginFunction[] {
			new Execute(0, lock),
			new Execute(1, lock),
			new Execute(2, lock),
			new Execute(3, lock)
		});
	}
	
	// ロック・アンロック処理.
	private static final class Execute extends RhiginFunction {
		final int type;
		final ReadWriteLock object;
		Execute(int t, ReadWriteLock o) {
			type = t;
			object = o;
		}
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch(type) {
			case 0:
				object.writeLock().lock();
				break;
			case 1:
				object.writeLock().unlock();
				break;
			case 2:
				object.readLock().lock();
				break;
			case 3:
				object.readLock().unlock();
				break;
			}
			return Undefined.instance;
		}
		@Override
		public final String getName() {
			switch(type) {
			case 0:
				return "wlock";
			case 1:
				return "wulock";
			case 2:
				return "rlock";
			case 3:
				return "rulock";
			}
			return "unknown";
		}
	};
}