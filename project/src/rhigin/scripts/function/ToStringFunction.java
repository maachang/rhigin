package rhigin.scripts.function;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.scripts.RhiginFunction;
import rhigin.util.BlankScriptable;

/**
 * js内のToStringを呼び出す処理.
 * 
 * それぞれ オブジェクトの先頭で protected final ToStringFunction.Execute toStringFunction =
 * new ToStringFunction.Execute(this); を宣言します。 Scriptable.get(String,
 * Scriptable); Scriptable.has(String, Scriptable); に "toString".equals
 * の場合に、getで「toStringFunction」を返却させます. hasの場合は[true]を返却されます.
 * 
 */
public abstract class ToStringFunction {
	// jsで呼び出すtoStringファンクション.
	public static class Execute extends RhiginFunction {
		final BlankScriptable o;

		public Execute(BlankScriptable o) {
			this.o = o;
		}

		@Override
		public String getName() {
			return "toString";
		}

		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return o.toString();
		}

		@Override
		public final String toString() {
			return o.toString();
		}

		@Override
		public Object getDefaultValue(Class<?> clazz) {
			return (clazz == null || String.class.equals(clazz)) ? o.toString() : Undefined.instance;
		}
	}
}
