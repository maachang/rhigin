package rhigin.scripts.objects;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginInstanceObject;
import rhigin.scripts.RhiginInstanceObject.ObjectFunction;
import rhigin.util.ColorConsoleOut;
import rhigin.util.Converter;
import rhigin.util.FixedSearchArray;

/**
 * [js]カラーコンソール出力用オブジェクト.
 */
public class ColorOutObject {
	public static final String OBJECT_NAME = "ColorOut";
	
	// カーソル情報メソッド名群.
	private static final String[] COLOR_OUT_NAMES = new String[] {
		"out"
		,"setMode"
		,"isMode"
		,"useMode"
		,"print"
		,"println"
		,"errPrint"
		,"errPrintln"
	};
	
	// カーソル情報メソッド生成処理.
	private static final ObjectFunction COLOR_OUT_FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(COLOR_OUT_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new ColorOutFunctions(no, (ColorConsoleOut)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};
	
	/**
	 * カーソル情報を生成.
	 * @param c
	 * @return
	 */
	public static final RhiginInstanceObject create(ColorConsoleOut c) {
		return new RhiginInstanceObject(OBJECT_NAME, COLOR_OUT_FUNCTIONS, c);
	}
	
	// カーソルのメソッド群. 
	private static final class ColorOutFunctions extends RhiginFunction {
		private final int type;
		private final ColorConsoleOut out;

		ColorOutFunctions(int t, ColorConsoleOut o) {
			this.type = t;
			this.out = o;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // out.
					{
						if(args.length > 0 && args[0] instanceof String) {
							return out.toString((String)args[0]);
						}
					}
					break;
				case 1: // setMode.
					{
						if(args.length > 0) {
							out.setColorMode(Converter.convertBool(args[0]));
						}
					}
					break;
				case 2: // isMode.
					{
						return out.isColorMode();
					}
				case 3: // useMode.
					{
						return out.useColorMode();
					}
				case 4: // print.
					{
						if(args.length > 0 && args[0] instanceof String) {
							out.print((String)args[0]);
						}
					}
					break;
				case 5: // println.
					{
						if(args.length > 0 && args[0] instanceof String) {
							out.println((String)args[0]);
						} else {
							System.out.println();
						}
					}
					break;
				case 6: // errPrint.
					{
						if(args.length > 0 && args[0] instanceof String) {
							out.errPrint((String)args[0]);
						}
					}
					break;
				case 7: // errPrintln.
					{
						if(args.length > 0 && args[0] instanceof String) {
							out.errPrintln((String)args[0]);
						} else {
							System.err.println();
						}
					}
					break;
				}
			} catch (RhiginException re) {
				throw re;
			} catch (Exception e) {
				throw new RhiginException(500, e);
			}
			return Undefined.instance;
		}
		
		@Override
		public final String getName() {
			return COLOR_OUT_NAMES[type];
		}
	};
	
	// インスタンス生成用オブジェクト.
	private static final class Instance extends RhiginFunction {
		@Override
		public Scriptable jconstruct(Context ctx, Scriptable thisObj, Object[] args) {
			ColorConsoleOut co;
			if(args.length > 0 && args[0] instanceof Boolean) {
				co = new ColorConsoleOut((Boolean)args[0]);
			} else {
				co = new ColorConsoleOut();
			}
			return ColorOutObject.create(co);
		}

		@Override
		public final String getName() {
			return OBJECT_NAME;
		}

		@Override
		public Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return Undefined.instance;
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
		scope.put(OBJECT_NAME, scope, ColorOutObject.getInstance());
	}

}
