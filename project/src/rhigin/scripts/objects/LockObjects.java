package rhigin.scripts.objects;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.http.Http;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginInstanceObject;
import rhigin.scripts.RhiginInstanceObject.ObjectFunction;
import rhigin.util.FixedSearchArray;

/**
 * ロック管理オブジェクト.
 */
public class LockObjects {
	public static final String LOCK_OBJECT_NAME = "Lock";
	public static final String READ_WRITE_LOCK_OBJECT_NAME = "RwLock";
	

	// WeakReference で GCで削除された情報.
	private static final ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();

	// キー管理したWeakReference.
	private static final class KeyWeakReference<T> extends WeakReference<T> {
		private String key;

		public KeyWeakReference(T referent, String key) {
			super(referent);
			this.key = key;
		}

		public KeyWeakReference(T referent, String key, ReferenceQueue<? super T> q) {
			super(referent, q);
			this.key = key;
		}

		public String key() {
			return key;
		}
	}

	// 使用済みロックのクリーナー.
	// webサーバ実行時のみ動作する.
	private static CleanLocks cleanLocks = null;

	private static final class CleanLocks extends Thread {
		volatile boolean stopFlag = false;

		public CleanLocks() {
			this.setDaemon(true);
		}

		public void run() {
			while (!stopFlag) {
				try {
					while (!stopFlag) {
						execute();
						Thread.sleep(50);
					}
				} catch (Throwable t) {
					if (t instanceof ThreadDeath) {
						throw (ThreadDeath) t;
					}
				}
			}
		}

		// KeyWeakReference が GC で削除された場合の 空の value を削除.
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public long execute() throws Throwable {
			long ret = 0L;
			String k, h;
			KeyWeakReference<? extends Object> r;
			ConcurrentHashMap map;
			while ((r = (KeyWeakReference) refQueue.poll()) != null) {
				ret++;
				k = r.key();
				h = k.substring(0, 1);
				k = k.substring(1);
				if (LockObject.HEAD.equals(h)) {
					map = LockObject.locks;
				} else if (RwLockObject.HEAD.equals(h)) {
					map = RwLockObject.locks;
				} else {
					map = null;
				}
				if (map != null) {
					// 別の処理で新たに対象名でロック処理が利用されていない場合.
					r = (KeyWeakReference<? extends Object>) map.get(k);
					if (r != null && r.get() == null) {
						while (true) {
							if (!map.remove(k, r)) {
								Thread.sleep(5);
								// 別の処理で新たに対象名でロック処理が利用されている場合は削除しない.
								r = (KeyWeakReference<? extends Object>) map.get(k);
								if (r != null && r.get() == null) {
									continue;
								}
							}
							break;
						}
					}
				}
				Thread.sleep(5);
			}
			return ret;
		}
	}

	/**
	 * [js]ロックオブジェクト.
	 * 
	 * var lock = new Lock("test");
	 * lock.lock();
	 * try {
	 *   ...
	 * } finally {
	 *   lock.unlock();
	 * }
	 */
	private static class LockObject extends RhiginFunction {
		protected static final String HEAD = "#";
		private static final LockObject THIS = new LockObject();

		public static final LockObject getInstance() {
			return THIS;
		}

		// ロック管理オブジェクト.
		protected static final ConcurrentHashMap<String, KeyWeakReference<Object>> locks =
			new ConcurrentHashMap<String, KeyWeakReference<Object>>();

		// メソッド名群.
		private static final String[] FUNCTION_NAMES = new String[] {
			"lock"
			,"unlock"
		};

		// メソッド生成処理.
		private static final ObjectFunction FUNCTIONS = new ObjectFunction() {
			private FixedSearchArray<String> word = new FixedSearchArray<String>(FUNCTION_NAMES);
			public RhiginFunction create(int no, Object... params) {
				return new Execute(no, (Lock)params[0]);
			}
			public FixedSearchArray<String> getWord() {
				return word;
			}
		};

		@Override
		public String getName() {
			return LOCK_OBJECT_NAME;
		}

		// new Object() で呼ばれる.
		@Override
		public Scriptable jconstruct(Context ctx, Scriptable thisObject, Object[] args) {
			Lock lock = null;
			Object value = null;
			if (args.length > 0) {
				String key = "" + args[0];
				KeyWeakReference<Object> w = locks.get(key);
				if (w == null || (value = w.get()) == null) {
					lock = new ReentrantLock();
					if (Http.isWebServerMode()) {
						locks.put(key, new KeyWeakReference<Object>(lock, HEAD + key, refQueue));
					} else {
						locks.put(key, new KeyWeakReference<Object>(lock, key));
					}
				} else {
					lock = (Lock) value;
				}
			} else {
				lock = new ReentrantLock();
			}
			return new RhiginInstanceObject(LOCK_OBJECT_NAME, FUNCTIONS, lock);
		}
		
		@Override
		public Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return Undefined.instance;
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
			public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
				if (type == 0) {
					object.lock();
				} else {
					object.unlock();
				}
				return Undefined.instance;
			}

			@Override
			public final String getName() {
				return FUNCTION_NAMES[type];
			}
		};
	}

	/**
	 * [js]read / write ロックオブジェクト.
	 * 
	 * var rwLock = new RwLock("test");
	 * 
	 * // read lock unlock. lock.rlock(); try { ... } finally { lock.rulock(); }
	 * 
	 * // write lock unlock. lock.wlock(); try { ... } finally { lock.wulock(); }
	 */
	private static final class RwLockObject extends RhiginFunction {
		protected static final String HEAD = "@";
		private static final RwLockObject THIS = new RwLockObject();

		public static final RwLockObject getInstance() {
			return THIS;
		}

		// ロック管理オブジェクト.
		protected static final ConcurrentHashMap<String, KeyWeakReference<Object>> locks = new ConcurrentHashMap<String, KeyWeakReference<Object>>();

		@Override
		public String getName() {
			return READ_WRITE_LOCK_OBJECT_NAME;
		}
		
		// メソッド名群.
		private static final String[] FUNCTION_NAMES = new String[] {
			"wlock"
			,"wulock"
			,"rlock"
			,"rulock"
		};

		// メソッド生成処理.
		private static final ObjectFunction FUNCTIONS = new ObjectFunction() {
			private FixedSearchArray<String> word = new FixedSearchArray<String>(FUNCTION_NAMES);
			public RhiginFunction create(int no, Object... params) {
				return new Execute(no, (ReadWriteLock)params[0]);
			}
			public FixedSearchArray<String> getWord() {
				return word;
			}
		};

		// new Object() で呼ばれる.
		@Override
		public Scriptable jconstruct(Context ctx, Scriptable thisObject, Object[] args) {
			ReadWriteLock lock = null;
			Object value = null;
			if (args.length > 0) {
				String key = "" + args[0];
				KeyWeakReference<Object> w = locks.get(key);
				if (w == null || (value = w.get()) == null) {
					lock = new ReentrantReadWriteLock();
					if (Http.isWebServerMode()) {
						locks.put(key, new KeyWeakReference<Object>(lock, HEAD + key, refQueue));
					} else {
						locks.put(key, new KeyWeakReference<Object>(lock, key));
					}
				} else {
					lock = (ReadWriteLock) value;
				}
			} else {
				lock = new ReentrantReadWriteLock();
			}
			return new RhiginInstanceObject(READ_WRITE_LOCK_OBJECT_NAME, FUNCTIONS, lock);
		}
		
		@Override
		public Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return Undefined.instance;
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
			public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
				switch (type) {
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
				return FUNCTION_NAMES[type];
			}
		};
	}

	/**
	 * 初期化処理.
	 */
	public static final void init() {
		if (Http.isWebServerMode()) {
			// Webサーバ起動時のみ、クリーナは実行される.
			cleanLocks = new CleanLocks();
			cleanLocks.start();
		}
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put(LOCK_OBJECT_NAME, scope, LockObject.getInstance());
		scope.put(READ_WRITE_LOCK_OBJECT_NAME, scope, RwLockObject.getInstance());
	}
}
