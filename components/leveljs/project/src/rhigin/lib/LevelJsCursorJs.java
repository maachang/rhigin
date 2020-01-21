package rhigin.lib;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.lib.level.operator.OperateIterator;
import rhigin.scripts.JsonOut;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginInstanceObject;
import rhigin.scripts.RhiginInstanceObject.ObjectFunction;
import rhigin.util.FixedSearchArray;

/**
 * [js]LevelJs カーソル.
 */
public class LevelJsCursorJs {
	// カーソル情報メソッド名群.
	private static final String[] CURSOR_NAMES = new String[] {
		"close"
		,"isClose"
		,"isDesc"
		,"key"
		,"hasNext"
		,"next"
		,"toString"
	};
	
	// カーソル情報メソッド生成処理.
	private static final ObjectFunction CURSOR_FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(CURSOR_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new CursorFunctions(no, (OperateIterator)params[0]);
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
	public static final RhiginInstanceObject create(String o, OperateIterator c) {
		return new RhiginInstanceObject(o + "Cursor", CURSOR_FUNCTIONS, c);
	}
	
	// カーソルのメソッド群. 
	private static final class CursorFunctions extends RhiginFunction {
		private final int type;
		private final OperateIterator cursor;

		CursorFunctions(int t, OperateIterator c) {
			this.type = t;
			this.cursor = c;
		}

		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // close.
					{
						cursor.close();
					}
					break;
				case 1: // isClose.
					{
						return cursor.isClose();
					}
				case 2: // isDesc.
					{
						return cursor.isDesc();
					}
				case 3: // key.
					{
						return cursor.key();
					}
				case 4: // hasNext.
					{
						return cursor.hasNext();
					}
				case 5: // next.
					{
						return cursor.next();
					}
				case 6: // toString.
					{
						return JsonOut.toString(cursor);
					}
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
			return CURSOR_NAMES[type];
		}
	};
}
