package rhigin.scripts;

import org.mozilla.javascript.IdScriptableObject;

/**
 * Rhinoでのjsからの例外をJavaのThrowableで理解できる例外に変換.
 * 
 * たとえば、以下のjavascriptのように
 * 
 * ＜js内容＞
 * try {
 *   a.b = c;
 * } catch(e) {
 *   ToJavaFunction(e);
 * }
 * ＜Java内容＞
 * public class ToJavaFunction implements Function {
 *   public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
 *     RhinoNativeErrorByJavaException.convert(args[0]).printStackTrace();
 *     return Undefined.instance;
 *   }
 * }
 * と言う感じの例外が必ず出る場合においてorg.mozilla.javascript.Function をラッパーした
 * ToJavaFunctionと言うJavaで受けるメソッドコールの引数にJsの例外がセットされた場合の
 * JavaのExceptionに変換する処理を提供します。
 */
public class RhinoNativeErrorByJavaException {
	
	/**
	 * Jsの例外を 直接 RhinoFunctionなどの引数に渡された場合の処理.
	 * @param t js の例外 NativeErrorなどの例外を示すオブジェクトを設定します.
	 * @return Throwable 例外が返却されます.
	 */
	public static final Throwable convert(Object t) {
		if (t instanceof IdScriptableObject &&
			t.getClass().getName().equals("org.mozilla.javascript.NativeError")) {
			IdScriptableObject o = (IdScriptableObject)t;
			
			/*
			// この処理で.
			Object[] ids = o.getAllIds();
			for(int i = 0; i < ids.length; i ++) {
				System.out.println("id:" + ids[i] + " value:" + o.get(ids[i]));
				if(o.get(ids[i]) != null) {
					System.out.println(" class:" + o.get(ids[i]).getClass());
				}
			}
			*/
			// 以下の内容が取得できる.
//			id:message value:rhigin.RhiginException: java.lang.NullPointerException
//			 class:class java.lang.String
//			id:fileName value:<script>
//			 class:class java.lang.String
//			id:lineNumber value:41
//			 class:class java.lang.Integer
//			id:stack value:	at <script>:41
//				at /home/maachang/project/rhigin/bin/test/rtest/lib/rtest-core.js:377
//				at <script>:38
//				at /home/maachang/project/rhigin/bin/test/rtest/lib/rtest-core.js:317
//				at <script>:20
//				at <script>:1
//				at ${RHIGIN_HOME}/test/rtest/rtest.js:118
//				at ${RHIGIN_HOME}/test/rtest/rtest.js:143
//				at ${RHIGIN_HOME}/test/rtest/rtest.js:4
//				
//			 class:class java.lang.String
//			id:javaException value:rhigin.RhiginException: java.lang.NullPointerException
//			 class:class rhigin.RhiginException
//			id:rhinoException value:rhigin.scripts.RhiginWrapException: Wrapped rhigin.RhiginException: java.lang.NullPointerException (<script>#41)
//			 class:class rhigin.scripts.RhiginWrapException
			
			// javaExceptionかrhinoExceptionの内容を取得して返却.
			Object ret = o.get("javaException");
			if(ret == null) {
				ret = o.get("rhinoException");
			}
			t = ret;
		}
		if(t instanceof Throwable) {
			return (Throwable)t;
		} else {
			return null;
		}
	}
}
