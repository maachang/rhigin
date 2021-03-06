package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginInstanceObject;
import rhigin.scripts.RhiginInstanceObject.ObjectFunction;
import rhigin.util.Converter;
import rhigin.util.FixedKeyValues;
import rhigin.util.FixedSearchArray;
import rhigin.util.Xor128;

/**
 * [js]Xor128乱数発生. 高速な処理に対して、精度の高い乱数を発生させます.
 * 
 * var r = new Xor128(nanoTime());
 * // r.setSeet(nanoTime());
 * 
 * var n = r.next();
 */
public final class Xor128Object {
	public static final String OBJECT_NAME = "Xor128";
	
	/**
	 * オブジェクトを作成.
	 * @param x
	 * @return
	 */
	public static final RhiginInstanceObject newObject(Xor128 x) {
		return new RhiginInstanceObject(OBJECT_NAME, FUNCTIONS, x);
	}
	
	private static final class InstanceObject extends RhiginFunction {
		private final int type;
		private final Xor128 xor128;

		InstanceObject(int t, Xor128 o) {
			type = t;
			xor128 = o;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch (type) {
			case 0:
				if (args.length >= 1 && Converter.isNumeric(args[0])) {
					xor128.setSeet(Converter.convertLong(args[0]));
				}
				break;
			case 1:
				return xor128.nextInt();
			}
			return argsError(args, args.length);
		}

		@Override
		public final String getName() {
			return FUNCTION_NAMES[type];
		}

		private final Object argsError(Object[] args, int len) {
			switch (type) {
			case 0:
				if (!(len >= 1 && Converter.isNumeric(args[0]))) {
					argsException(OBJECT_NAME);
				}
				break;
			}
			return Undefined.instance;
		}
	};
	
	// メソッド名群.
	private static final String[] FUNCTION_NAMES = new String[] {
		"seet"
		,"nextInt"
	};
	
	// メソッド生成処理.
	private static final ObjectFunction FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(FUNCTION_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new InstanceObject(no, (Xor128)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};
	
	// インスタンス生成用オブジェクト.
	private static final class Instance extends RhiginFunction {
		@Override
		public Scriptable jconstruct(Context ctx, Scriptable thisObj, Object[] args) {
			final Xor128 xor128 = new Xor128();
			if (args.length >= 1 && Converter.isNumeric(args[0])) {
				xor128.setSeet(Converter.convertLong(args[0]));
			}
			return newObject(xor128);
		}
		
		@Override
		public Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return Undefined.instance;
		}

		@Override
		public final String getName() {
			return OBJECT_NAME;
		}
	};

	private static final Instance THIS = new Instance();
	public static final RhiginFunction getInstance() {
		return THIS;
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put(OBJECT_NAME, scope, Xor128Object.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put(OBJECT_NAME, Xor128Object.getInstance());
	}
}
