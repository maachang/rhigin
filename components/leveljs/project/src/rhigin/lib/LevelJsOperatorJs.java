package rhigin.lib;

import org.maachang.leveldb.util.Converter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.lib.level.operator.OperateIterator;
import rhigin.lib.level.operator.SearchOperator;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginInstanceObject;
import rhigin.scripts.RhiginInstanceObject.ObjectFunction;
import rhigin.util.FixedSearchArray;

/**
 * [js]LevelJs オペレータ.
 */
public class LevelJsOperatorJs {
	// オペレータメソッド名群.
	private static final String[] OPERATOR_NAMES = new String[] {
		"name"
		,"operatorType"
		,"isAvailable"
		,"isEmpty"
		,"mode"
		,"createIndex"
		,"deleteIndex"
		,"isIndex"
		,"indexSize"
		,"indexNames"
		,"index"
		,"put"
		,"remove"
		,"get"
		,"contains"
		,"cursor"
		,"range"
		,"trancate"
	};
	
	// オペレータ用メソッド生成処理.
	private static final ObjectFunction OPERATOR_FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(OPERATOR_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new OperatorFunctions(no,(String)params[0], (SearchOperator)params[1]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};
	
	/**
	 * オペレータ情報を生成.
	 * @param q
	 * @return
	 */
	public static final RhiginInstanceObject create(SearchOperator o) {
		String operatorName = o.getOperatorType();
		operatorName = operatorName.substring(0, 1).toUpperCase() + operatorName.substring(1);
		return new RhiginInstanceObject(operatorName, OPERATOR_FUNCTIONS, operatorName, o);
	}
	
	// オペレータのメソッド群. 
	private static final class OperatorFunctions extends RhiginFunction {
		private final int type;
		private final String opName;
		private final SearchOperator op;

		OperatorFunctions(int t, String n, SearchOperator q) {
			this.type = t;
			this.opName = n;
			this.op = q;
		}
		
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // name.
				{
					return op.getName();
				}
				case 1: // operatorType.
				{
					return op.getOperatorType();
				}
				case 2: // isAvailable.
				{
					return op.isAvailable();
				}
				case 3: // isEmpty.
				{
					return op.isEmpty();
				}
				case 4: // mode.
				{
					return op.getMode().get();
				}
				case 5: // createIndex.
				{
					op.createIndex(getString(0, args), getParamsByString(1, args));
					break;
				}
				case 6: // deleteIndex.
				{
					op.deleteIndex(getParamsByString(0, args));
					break;
				}
				case 7: // isIndex.
				{
					return op.isIndex(getParamsByString(0, args));
				}
				case 8: // indexSize.
				{
					return op.indexSize();
				}
				case 9: // indexNames.
				{
					return op.indexs();
				}
				case 10: // index.
				{
					if(args == null || args.length == 0) {
						this.argsException(opName);
					}
					int off = 0;
					boolean desc = false;
					if(args[0] instanceof Boolean) {
						desc = (boolean)args[0];
						off = 1;
					}
					OperateIterator it = op.index(
						desc, getObject(off, args), getParamsByString(off + 1, args));
					return LevelJsCursorJs.create(it);
				}
				case 11: // put.
				{
					if(args == null || args.length == 0) {
						this.argsException(opName);
					}
					return op.put(args);
				}
				case 12: // remove.
				{
					if(args == null || args.length == 0) {
						this.argsException(opName);
					}
					op.remove(args);
					break;
				}
				case 13: // get.
				{
					if(args == null || args.length == 0) {
						this.argsException(opName);
					}
					return op.get(args);
				}
				case 14: // contains.
				{
					if(args == null || args.length == 0) {
						this.argsException(opName);
					}
					return op.contains(args);
				}
				case 15: // cursor.
				{
					if(args == null || args.length == 0) {
						return LevelJsCursorJs.create(op.cursor(false));
					}
					int off = 0;
					boolean desc = false;
					Object key = null;
					if(args[0] instanceof Boolean) {
						desc = (boolean)args[0];
						off = 1;
					} else if(args.length == 1) {
						key = args[0];
					}
					if(args.length >= off + 1) {
						key = getObject(off, args);
					}
					final OperateIterator it = op.cursor(desc, key);
					return LevelJsCursorJs.create(it);
				}
				case 16: // range.
				{
					if(args == null || args.length == 0) {
						this.argsException(opName);
					}
					boolean desc = false;
					Object[] keys = null;
					if(args.length >= 2) {
						if(args[0] instanceof Boolean) {
							desc = (boolean)args[0];
							keys = getParams(1, args);
						} else {
							keys = args;
						}
					}
					if(keys == null || keys.length < 2) {
						this.argsException(opName);
					}
					OperateIterator it = op.range(desc, keys);
					return LevelJsCursorJs.create(it);
				}
				case 17: // trancate.
				{
					return op.trancate();
				}
				
				}
			} catch (RhiginException re) {
				throw re;
			} catch (Exception e) {
				throw new RhiginException(500, e);
			}
			return Undefined.instance;
		}
		
		// オブジェクトを取得.
		private final Object getObject(int off, Object[] args) {
			if(args == null || args.length <= off) {
				this.argsException(opName);
			}
			return args[off];
		}
		
		// 文字列を取得.
		private final String getString(int off, Object[] args) {
			if(args == null || args.length <= off ||
				!(args[off] instanceof String)) {
				this.argsException(opName);
			}
			return (String)args[off];
		}
		
		// パラメタを取得.
		private final Object[] getParams(int off, Object[] args) {
			int argsLen = 0;
			if(args == null || (argsLen = args.length) < off) {
				this.argsException(opName);
			}
			Object[] ret = new Object[argsLen-off];
			System.arraycopy(args, off, ret, 0, argsLen-off);
			return ret;
		}

		// パラメタを取得.
		private final String[] getParamsByString(int off, Object[] args) {
			int argsLen = 0;
			if(args == null || (argsLen = args.length) < off) {
				this.argsException(opName);
			}
			int len = argsLen - off;
			String[] ret = new String[len];
			for(int i = off, j = 0; i < argsLen; i ++) {
				ret[j ++] = Converter.convertString(args[i]);
			}
			return ret;
		}

		
		@Override
		public final String getName() {
			return OPERATOR_NAMES[type];
		}
	};
}
