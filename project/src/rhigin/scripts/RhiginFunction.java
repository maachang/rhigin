package rhigin.scripts;

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
		if("apply".equals(arg0)) {
			return new ApplyFunction(this);
		} else if("call".equals(arg0)) {
			return new CallFunction(this);
		}
		return Undefined.instance;
	}
	
	@Override
	public boolean has(String arg0, Scriptable arg1) {
		return "apply".equals(arg0) || "call".equals(arg0);
	}
	
	@Override
	public Object[] getIds() {
		return new Object[] {
			"apply", "call"
		};
	}
	
	// xxx.apply(scope, argsArray);
	private static final class ApplyFunction extends AbstractRhiginFunction {
		AbstractRhiginFunction src;
		ApplyFunction(AbstractRhiginFunction s) {
			src = s;
		}
		@Override
		public String getName() {
			return "apply";
		}
		
		@SuppressWarnings("rawtypes")
		@Override
		public Object jcall(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
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
			return src.jcall(cx, scope, sc, params);
		}
	}
	
	// xxx.call(scope, args ...);
	private static final class CallFunction extends AbstractRhiginFunction {
		AbstractRhiginFunction src;
		CallFunction(AbstractRhiginFunction s) {
			src = s;
		}
		@Override
		public String getName() {
			return "call";
		}
		
		@Override
		public Object jcall(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
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
			return src.jcall(cx, scope, sc, params);
		}
	}
}
