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

	protected final void setXor128(Xor128 x) {
		xor128.set(x);
	}

	@Override
	public String getName() {
		return "random";
	}

	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		Xor128 r = xor128.get();
		if (r == null) {
			r = new Xor128(System.nanoTime());
			xor128.set(r);
		}
		return r.nextInt();
	}

	/**
	 * 初期化.
	 */
	public static final void init() {
		init(new Xor128(System.nanoTime()));
	}

	/**
	 * 初期化.
	 * 
	 * @param xor128
	 *            xor128乱数発生オブジェクトを設定します.
	 */
	public static final void init(Xor128 xor128) {
		RandomFunction.getInstance().setXor128(xor128);
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("random", scope, RandomFunction.getInstance());
	}
}
