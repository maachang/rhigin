package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.scripts.RhiginFunction;
import rhigin.util.Xor128;

/**
 * [js]乱数発生.
 */
public class RandomFunction extends RhiginFunction {
	private static final RandomFunction THIS = new RandomFunction();
	public static final RandomFunction getInstance() {
		return THIS;
	}
	// threadローカルでXor128を管理.
	private final ThreadLocal<Xor128> xor128 = new ThreadLocal<Xor128>();
	public final void setXor128(Xor128 x) {
		xor128.set(x);
	}
	
	@Override
	public String getName() {
		return "random";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		Xor128 r = xor128.get();
		if(r == null) {
			r = new Xor128(System.nanoTime());
			xor128.set(r);
		}
		return r.nextInt();
	}

}
