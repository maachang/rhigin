package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.Converter;
import rhigin.util.Xor128;

/**
 * [js]Xor128乱数発生.
 * 高速な処理に対して、精度の高い乱数を発生させます.
 * 
 * var r = new Xor128(nanoTime());
 * // r.setSeet(nanoTime());
 * var n = r.next();
 */
public final class Xor128Object {
	private static final class Execute extends RhiginFunction {
		private final int type;
		private final Xor128 xor128;
		Execute(int t, Xor128 o) {
			type = t;
			xor128 = o;
		}
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch(type) {
			case 0:
				if(args.length >= 1 && Converter.isNumeric(args[0])) {
					xor128.setSeet(Converter.convertLong(args[0]));
				}
				break;
			case 1:
				return xor128.nextInt();
			}
			return Undefined.instance;
		}
		@Override
		public final String getName() {
			switch(type) {
			case 0: return "seet";
			case 1: return "nextInt";
			}
			return "unknown";
		}
	};
	private static final class Instance extends RhiginFunction {
		@Override
		public Scriptable construct(Context ctx, Scriptable thisObj, Object[] args) {
			final Xor128 xor128 = new Xor128();
			if(args.length >= 1 && Converter.isNumeric(args[0])) {
				xor128.setSeet(Converter.convertLong(args[0]));
			}
			return new RhiginObject("Xor128", new RhiginFunction[] {
				new Execute(0, xor128), new Execute(1, xor128)
			});
		}
		@Override
		public final String getName() { return "Xor128"; }
	};
	private static final Instance THIS = new Instance();
	public static final RhiginFunction getInstance() {
		return THIS;
	}
	
	/**
	 * スコープにライブラリを登録.
	 * @param scope 登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("Xor128", scope, Xor128Object.getInstance());
	}
}
