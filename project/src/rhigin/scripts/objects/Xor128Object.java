package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;
import rhigin.util.Converter;
import rhigin.util.Xor128;

/**
 * Xor128乱数発生.
 * 高速な処理に対して、精度の高い乱数を発生させます.
 */
public final class Xor128Object {
	private static final class seetFunction extends RhiginFunction {
		private final Xor128 xor128;
		seetFunction(Xor128 o) {
			xor128 = o;
		}
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args.length >= 1 && Converter.isNumeric(args[0])) {
				xor128.setSeet(Converter.convertLong(args[0]));
			}
			return Undefined.instance;
		}
		@Override
		public final String getName() { return "seet"; }
	};
	private static final class nextIntFunction extends RhiginFunction {
		private final Xor128 xor128;
		nextIntFunction(Xor128 o) {
			xor128 = o;
		}
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return xor128.nextInt();
		}
		@Override
		public final String getName() { return "nextInt"; }
	};
	private static final class Instance extends RhiginFunction {
		@Override
		public Scriptable construct(Context arg0, Scriptable arg1, Object[] arg2) {
			Xor128 xor128 = new Xor128();
			seetFunction f1 = new seetFunction(xor128);
			nextIntFunction f2 = new nextIntFunction(xor128);
			return new RhiginObject("Xor128", new RhiginFunction[] {f1, f2});
		}
		@Override
		public final String getName() { return "Xor128"; }
	};
	private static final Instance THIS = new Instance();
	public static final RhiginFunction getInstance() {
		return THIS;
	}
}
