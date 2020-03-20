package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.scripts.RhiginFunction;
import rhigin.util.Args;
import rhigin.util.Converter;
import rhigin.util.FixedKeyValues;

/**
 * [js]rhigin実行時の引数I/Oメソッド.
 */
public final class ArgsFunctions {
	
	// ArgsFunction.
	private static final class ArgsFunction extends RhiginFunction {
		private static final ArgsFunction THIS = new ArgsFunction();
	
		public static final ArgsFunction getInstance() {
			return THIS;
		}
	
		@Override
		public String getName() {
			return "args";
		}
	
		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				Object o = args[0];
				if (Converter.isNumeric(o)) {
					return Args.get()[Converter.convertInt(o)];
				} else {
					int len = args.length;
					String[] list = new String[len];
					for(int i = 0; i < len; i ++) {
						list[i] = "" + args[i];
					}
					return Args.getInstance().get(list);
				}
			}
			return Args.get().length;
		}
	}
	
	// ArgsValueFunction.
	private static final class ArgsValueFunction extends RhiginFunction {
		private static final ArgsValueFunction THIS = new ArgsValueFunction();
	
		public static final ArgsValueFunction getInstance() {
			return THIS;
		}
	
		@Override
		public String getName() {
			return "argsValue";
		}
	
		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				int len = args.length;
				String[] list = new String[len];
				for(int i = 0; i < len; i ++) {
					list[i] = "" + args[i];
				}
				return Args.getInstance().isValue(list);
			}
			return false;
		}
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("args", scope, ArgsFunction.getInstance());
		scope.put("argsValue", scope, ArgsValueFunction.getInstance());
	}
	
	/**
	 * FixedKeyValues に情報を追加.
	 * @param fkv
	 */
	public static final void regFunctions(FixedKeyValues<String, Object> fkv) {
		fkv.put("args", ArgsFunction.getInstance());
		fkv.put("argsValue", ArgsValueFunction.getInstance());
	}
}