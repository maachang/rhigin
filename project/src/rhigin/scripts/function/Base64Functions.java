package rhigin.scripts.function;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhigin.RhiginException;
import rhigin.scripts.RhiginFunction;
import rhigin.util.Base64;

/**
 * [js]base64ファンクション.
 */
public class Base64Functions {
	// base64デコード.
	private static class AtobFunction extends RhiginFunction {
		private static final AtobFunction THIS = new AtobFunction();

		public static final AtobFunction getInstance() {
			return THIS;
		}

		@Override
		public String getName() {
			return "atob";
		}

		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				if (args.length >= 2) {
					boolean binaryFlag = (args[1] instanceof Boolean) ? (boolean) args[1] : false;
					if (binaryFlag) {
						return Base64.decode("" + args[0]);
					}
				}
				return Base64.atob("" + args[0]);
			}
			return argsException();
		}
	}

	// Base64エンコード.
	private static final class BtoaFunction extends RhiginFunction {
		private static final BtoaFunction THIS = new BtoaFunction();

		public static final BtoaFunction getInstance() {
			return THIS;
		}

		@Override
		public String getName() {
			return "btoa";
		}

		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length >= 1) {
				if (args[0] instanceof byte[]) {
					return Base64.encode((byte[]) args[0]);
				} else if (args[0] instanceof InputStream) {
					try {
						int len;
						byte[] bin = new byte[1024];
						InputStream in = (InputStream) args[0];
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						while ((len = in.read(bin)) != -1) {
							out.write(bin, 0, len);
						}
						out.flush();
						bin = out.toByteArray();
						out.close();
						out = null;
						in = null;
						return Base64.encode(bin);
					} catch (Exception e) {
						throw new RhiginException(500, e);
					}
				}
				return Base64.btoa("" + args[0]);
			}
			return argsException();
		}
	}

	/**
	 * スコープにライブラリを登録.
	 * 
	 * @param scope
	 *            登録先のスコープを設定します.
	 */
	public static final void regFunctions(Scriptable scope) {
		scope.put("atob", scope, AtobFunction.getInstance());
		scope.put("btoa", scope, BtoaFunction.getInstance());
	}
}
