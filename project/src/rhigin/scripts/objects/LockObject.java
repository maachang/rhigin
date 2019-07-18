package rhigin.scripts.objects;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;

/**
 * [js]ロックオブジェクト.
 * 
 * var lock = new Lock("test");
 * lock.lock();
 * try {
 *  ...
 * } finally {
 *   lock.unlock();
 * }
 */
public class LockObject extends RhiginFunction {
	private static final LockObject THIS = new LockObject();
	public static final LockObject getInstance() {
		return THIS;
	}
	
	private final Map<String, WeakReference<Lock>> locks = new ConcurrentHashMap<String, WeakReference<Lock>>();
	
	@Override
	public String getName() {
		return "Lock";
	}
	
	@Override
	public Scriptable construct(Context ctx, Scriptable thisObject, Object[] args) {
		Lock lock = null;
		if(args.length > 0) {
			String key = "" + args[0];
			WeakReference<Lock> w = locks.get(key);
			if(w == null || (lock = w.get()) == null) {
				lock = new ReentrantLock();
				locks.put(key, new WeakReference<Lock>(lock));
			}
		} else {
			lock = new ReentrantLock();
		}
		return new RhiginObject("Lock", new RhiginFunction[] {new Execute(0, lock), new Execute(1, lock)});
	}
	
	// ロック・アンロック処理.
	private static final class Execute extends RhiginFunction {
		final int type;
		final Lock object;
		Execute(int t, Lock o) {
			type = t;
			object = o;
		}
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(type == 0) {
				object.lock();
			} else {
				object.unlock();
			}
			return Undefined.instance;
		}
		@Override
		public final String getName() { return type == 0 ? "lock" : "unlock"; }
	};
}
