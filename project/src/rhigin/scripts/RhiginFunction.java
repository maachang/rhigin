package rhigin.scripts;

import java.util.Arrays;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

/**
 * Rhigin用Function.
 * 
 * rhigin用のjsに組み込みたいオリジナルのFunctionを作成したい場合に、継承して実装します.
 */
public abstract class RhiginFunction extends AbstractRhiginFunction {
	/** 親オブジェクト. **/
	protected Scriptable PARENT = null;
	
	/**
	 * 親となるrhiginオブジェクトをセット.
	 * @param p
	 */
	public void setRhiginObject(Scriptable p) {
		PARENT = p;
	}
	
	@Override
	public Object get(String arg0, Scriptable arg1) {
		int n = JsBaseFunction.name(arg0);
		if(n == -1) {
			return Undefined.instance;
		}
		JsBaseFunction f = new JsBaseFunction();
		return f.type(this, n);
	}
	
	@Override
	public boolean has(String arg0, Scriptable arg1) {
		return JsBaseFunction.name(arg0) != -1;
	}
	
	@Override
	public Object[] getIds() {
		return JsBaseFunction.NAMES;
	}
	
	// xxx.apply(scope, argsArray);
	// xxx.bind(scope, args ...);
	// xxx.call(scope, args ...);
	private static final class JsBaseFunction extends AbstractRhiginFunction {
		protected int type;
		protected AbstractRhiginFunction src;
		
		private static final String[] NAMES = new String[] {
			"apply"
			,"bind"
			,"call"
		};
		
		JsBaseFunction() {
		}
		
		@Override
		public void clear() {
			type = -1;
			src = null;
		}
		
		public final JsBaseFunction type(AbstractRhiginFunction s, int t) {
			this.src = s;
			type = t;
			return this;
		}
		
		public static final int name(String name) {
			if(name == null || name.isEmpty()) {
				return -1;
			}
			return Arrays.binarySearch(NAMES, name);
		}
		
		@Override
		public final String getName() {
			return NAMES[type];
		}
		
		@SuppressWarnings("rawtypes")
		public static final Object _apply(AbstractRhiginFunction s, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				return Undefined.instance;
			}
			Scriptable sc;
			if(args[0] == null || !(args[0] instanceof Scriptable)) {
				sc = thisObj;
			} else {
				sc = (Scriptable)args[0];
			}
			Object[] params = null;
			if(args.length >= 2 && args[1] instanceof List) {
				List list = (List)args[1];
				int len = list.size();
				params = new Object[len];
				for(int i = 0; i < len; i ++) {
					params[i] = list.get(i);
				}
			} else {
				params = ScriptConstants.BLANK_ARGS;
			}
			return s.jcall(cx, scope, sc, params);
		}
		
		public static final Object _bind(AbstractRhiginFunction s, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				return Undefined.instance;
			}
			Scriptable sc;
			if(args[0] == null || !(args[0] instanceof Scriptable)) {
				sc = null;
			} else {
				sc = (Scriptable)args[0];
			}
			Object[] params = null;
			if(args.length >= 2) {
				int len = args.length - 1;
				params = new Object[len];
				System.arraycopy(args, 1, params, 0, len);
			} else {
				params = ScriptConstants.BLANK_ARGS;
			}
			return new JsBindFunction(s, sc, params);
		}
		
		public static final Object _call(AbstractRhiginFunction s, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args == null || args.length == 0) {
				return Undefined.instance;
			}
			Scriptable sc;
			if(args[0] == null || !(args[0] instanceof Scriptable)) {
				sc = thisObj;
			} else {
				sc = (Scriptable)args[0];
			}
			Object[] params = null;
			if(args.length >= 2) {
				int len = args.length - 1;
				params = new Object[len];
				System.arraycopy(args, 1, params, 0, len);
			} else {
				params = ScriptConstants.BLANK_ARGS;
			}
			return s.jcall(cx, scope, sc, params);
		}

		@Override
		public final Object jcall(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch(type) {
			case 0: // apply.
				return _apply(src, cx, scope, thisObj, args);
			case 1: // bind.
				return _bind(src, cx, scope, thisObj, args);
			case 2: // call.
				return _call(src, cx, scope, thisObj, args);
			}
			return Undefined.instance;
		}
		
		@Override
		public final String toString() {
			return super.toString();
		}
	}
	
	// [js]bindされたFunction.
	private static final class JsBindFunction extends AbstractRhiginFunction {
		protected AbstractRhiginFunction src;
		protected Scriptable bindThis;
		protected Object[] bindArgs;
		
		JsBindFunction(AbstractRhiginFunction s, Scriptable b, Object[] a) {
			src = s;
			bindThis = b;
			bindArgs = a;
		}
		
		@Override
		public final String getName() {
			return src.getName();
		}
		
		@Override
		public final Object jcall(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(bindThis != null) {
				thisObj = bindThis;
			}
			if(args == null || args.length == 0) {
				args = bindArgs;
			}
			return src.call(cx, scope, thisObj, args);
		}
		
	}
}
