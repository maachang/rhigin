package rhigin.lib;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginException;
import rhigin.lib.level.operator.QueueOperator;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginInstanceObject;
import rhigin.scripts.RhiginInstanceObject.ObjectFunction;
import rhigin.util.FixedSearchArray;

/**
 * [js]LevelJs Queueオペレータ.
 */
public class LevelJsQueueJs {
	// Queueオペレータメソッド名群.
	private static final String[] QUEUE_NAMES = new String[] {
		"name"
		,"operatorType"
		,"isAvailable"
		,"isEmpty"
		,"mode"
		,"push"
		,"pop"
		,"trancate"
	};
	
	// Queueオペレータ用メソッド生成処理.
	private static final ObjectFunction QUEUE_FUNCTIONS = new ObjectFunction() {
		private FixedSearchArray<String> word = new FixedSearchArray<String>(QUEUE_NAMES);
		public RhiginFunction create(int no, Object... params) {
			return new QueueFunctions(no, (QueueOperator)params[0]);
		}
		public FixedSearchArray<String> getWord() {
			return word;
		}
	};
	
	/**
	 * Queueオペレータ情報を生成.
	 * @param q
	 * @return
	 */
	public static final RhiginInstanceObject create(QueueOperator q) {
		return new RhiginInstanceObject("Queue", QUEUE_FUNCTIONS, q);
	}
	
	// Queueオペレータのメソッド群. 
	private static final class QueueFunctions extends RhiginFunction {
		private final int type;
		private final QueueOperator op;

		QueueFunctions(int t, QueueOperator q) {
			this.type = t;
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
				case 5: // push.
				{
					if(args != null && args.length > 0) {
						return op.offer(args[0]);
					}
					this.argsException("Queue");
				}
				case 6: // pop.
				{
					return op.pop();
				}
				case 7: // trancate.
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
		
		@Override
		public final String getName() {
			return QUEUE_NAMES[type];
		}
	};
}
