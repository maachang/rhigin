package rhigin.scripts.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginThreadPool;
import rhigin.util.AtomicNumber;
import rhigin.util.Converter;

/**
 * スレッド関連のメソッド.
 * setImmediate, setTimeout, setInterval
 * 
 * 実装したが、この機能は当面は利用しない。
 * そもそも、node.jsでは、非同期処理を多様するが、
 * rhiginでは「js実行」はスレッド上で行ってるので必要がない.
 * 
 * なので、これらの機能もあえて使う必要はない.
 */
public class ThreadFunction {
	protected static final Object[] BLANK_ARGS = new Object[0];
	private static final AtomicNumber ids = new AtomicNumber(0);
	private static final Map<String, ScheduledFuture<?>> idMap = new ConcurrentHashMap<String, ScheduledFuture<?>>();
	
	private static final String getId() {
		int ret = ids.inc();
		if(ids.get() >= 0x7fffffff) {
			ids.set(0);
		}
		String code = Integer.toString(ret, 16);
		return new StringBuilder().
			append(Long.toString(System.currentTimeMillis(), 16)).
			append("-").append("00000000".substring(code.length())).append(code).
			toString();
	}
	
	private static final RhiginFunction setImmediate = new RhiginFunction() {
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args.length >= 1 && args[0] instanceof Function) {
				Function f = (Function)args[0];
				RhiginThreadPool.getInstance().getService().submit(new Runnable() {
					@Override
					public void run() {
						try {
							f.call(ctx, scope, null, BLANK_ARGS);
						} catch(Throwable t) {}
					}
				});
			}
			return Undefined.instance;
		}
		@Override
		public final String getName() { return "setImmediate"; }
	};
	
	private static final RhiginFunction setTimeout = new RhiginFunction() {
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args.length >= 2 && args[0] instanceof Function && Converter.isFloat(args[1])) {
				Function f = (Function)args[0];
				long time = Converter.convertLong(args[1]);
				String id = getId();
				ScheduledFuture<?> sf = RhiginThreadPool.getInstance().getService().schedule(new Runnable() {
					@Override
					public void run() {
						try {
							f.call(ctx, scope, null, BLANK_ARGS);
						} catch(Throwable t) {
						} finally {
							idMap.remove(id);
						}
					}
				}, time, TimeUnit.MILLISECONDS);
				idMap.put(id, sf);
				return id;
			}
			return Undefined.instance;
		}
		@Override
		public final String getName() { return "setTimeout"; }
	};
	
	private static final RhiginFunction clearTimeout = new RhiginFunction() {
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args.length >= 1) {
				String id = ""+args[0];
				ScheduledFuture<?> sf = idMap.get(id);
				if(sf != null) {
					sf.cancel(false);
					idMap.remove(id);
				}
			}
			return Undefined.instance;
		}
		@Override
		public final String getName() { return "clearTimeout"; }
	};
	
	private static final RhiginFunction setInterval = new RhiginFunction() {
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args.length >= 2 && args[0] instanceof Function && Converter.isFloat(args[1])) {
				Function f = (Function)args[0];
				long time = Converter.convertLong(args[1]);
				String id = getId();
				ScheduledFuture<?> sf = RhiginThreadPool.getInstance().getService().scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						try {
							f.call(ctx, scope, null, BLANK_ARGS);
						} catch(Throwable t) {
						} finally {
							idMap.remove(id);
						}
					}
				}, time, time, TimeUnit.MILLISECONDS);
				idMap.put(id, sf);
				return id;
			}
			return Undefined.instance;
		}
		@Override
		public final String getName() { return "setInterval"; }
	};
	
	private static final RhiginFunction clearInterval = new RhiginFunction() {
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args.length >= 1) {
				String id = ""+args[0];
				ScheduledFuture<?> sf = idMap.get(id);
				if(sf != null) {
					sf.cancel(false);
					idMap.remove(id);
				}
			}
			return Undefined.instance;
		}
		@Override
		public final String getName() { return "clearInterval"; }
	};
	
	public static final void set(Scriptable scope) {
		scope.put("setImmediate", scope, setImmediate);
		scope.put("setTimeout", scope, setTimeout);
		scope.put("clearTimeout", scope, clearTimeout);
		scope.put("setInterval", scope, setInterval);
		scope.put("clearInterval", scope, clearInterval);
	}
}
