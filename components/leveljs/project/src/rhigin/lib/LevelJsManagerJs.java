package rhigin.lib;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginConfig;
import rhigin.RhiginException;
import rhigin.lib.level.LevelJsCore;
import rhigin.lib.level.operator.Operator;
import rhigin.lib.level.operator.OperatorKeyType;
import rhigin.lib.level.operator.OperatorMode;
import rhigin.lib.level.operator.QueueOperator;
import rhigin.lib.level.operator.SearchOperator;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.scripts.RhiginFunction;
import rhigin.scripts.RhiginObject;

/**
 * [js]LevelJs マネージャ.
 */
public class LevelJsManagerJs {
	// コアオブジェクト.
	protected static final LevelJsCore CORE = new LevelJsCore();
	
	// LevelJsマネージャインスタンス.
	protected static final RhiginObject LEVEL_JS_INSTANCE = new RhiginObject("Level", new RhiginFunction[] {
		new LevelJsManFunctions(0), new LevelJsManFunctions(1), new LevelJsManFunctions(2), new LevelJsManFunctions(3),
		new LevelJsManFunctions(4), new LevelJsManFunctions(5), new LevelJsManFunctions(6), new LevelJsManFunctions(7),
		new LevelJsManFunctions(8), new LevelJsManFunctions(9), new LevelJsManFunctions(10), new LevelJsManFunctions(11),
		new LevelJsManFunctions(12), new LevelJsManFunctions(13), new LevelJsManFunctions(14), new LevelJsManFunctions(15),
		new LevelJsManFunctions(16), new LevelJsManFunctions(17), new LevelJsManFunctions(18)
	});
	
	/**
	 * LevelJsマネージャのメソッド群. 
	 */
	private static final class LevelJsManFunctions extends RhiginFunction {
		private final int type;

		LevelJsManFunctions(int t) {
			this.type = t;
		}
		
		@Override
		public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			try {
				switch (type) {
				case 0: // version.
				{
					return Level.VERSION;
				}
				case 1: // name.
				{
					return Level.NAME;
				}
				case 2: // startup.
				{
					// スタートアップ登録されていない場合のみ実行.
					if(!CORE.isStartup()) {
						RhiginEndScriptCall[] es = null;
						final RhiginConfig conf = ExecuteScript.getConfig();
						if(args.length > 0) {
							es = CORE.startup(conf, "" + args[0]);
						} else {
							es = CORE.startup(conf, null);
						}
						ExecuteScript.addEndScripts(es[0]);
						ExecuteScript.addExitSystemScripts(es[1]);
						return true;
					}
					return false;
				}
				case 3: // isStartup.
				{
					return CORE.isStartup();
				}
				case 4: // config.
				{
					return CORE.getConfig().getMap();
				}
				case 5: // machineId.
				{
					return CORE.getMachineId();
				}
				case 6: // createObject.
				{
					String name = getOperatorName(0, args);
					OperatorMode mode = getOperatorMode(args);
					// オペレータキータイプが設定されて無い場合は、文字列のキータイプとする.
					if(mode.getOperatorType() == OperatorKeyType.KEY_NONE) {
						mode.set("type", OperatorKeyType.KEY_STRING);
					}
					return CORE.createObject(name, mode);
				}
				case 7: // createLatLon.
				{
					String name = getOperatorName(0, args);
					OperatorMode mode = getOperatorMode(args);
					return CORE.createLatLon(name, mode);
				}
				case 8: // createSequence.
				{
					String name = getOperatorName(0, args);
					OperatorMode mode = getOperatorMode(args);
					return CORE.createSequence(name, mode);
				}
				case 9: // createQueue.
				{
					String name = getOperatorName(0, args);
					OperatorMode mode = getOperatorMode(args);
					// 必ずオペレータキータイプは「なし」.
					mode.set("type", OperatorKeyType.KEY_NONE);
					return CORE.createQueue(name, mode);
				}
				case 10: // delete.
				{
					return CORE.delete(
							getOperatorName(0, args));
				}
				case 11: // rename.
				{
					return CORE.rename(
							getOperatorName(0, args),
							getOperatorName(1, args));
				}
				case 12: // contains.
				{
					return CORE.contains(
							getOperatorName(0, args));
				}
				case 13: // get.
				{
					// commit / rollback が利用出来ないモードで取得.
					final Operator op = CORE.get(
							getOperatorName(0, args));
					if(op != null) {
						if(op instanceof SearchOperator) {
							return LevelJsOperatorJs.create((SearchOperator)op);
						} else if(op instanceof QueueOperator) {
							return LevelJsQueueJs.create((QueueOperator)op);
						}
					}
					return Undefined.instance;
				}
				case 14: // writeBatch
				{
					// commit / rollback が利用出来るモードで取得.
					final Operator op = CORE.getWriteBatch(
							getOperatorName(0, args));
					if(op != null) {
						if(op instanceof SearchOperator) {
							return LevelJsOperatorJs.create((SearchOperator)op);
						} else if(op instanceof QueueOperator) {
							return LevelJsQueueJs.create((QueueOperator)op);
						}
					}
					return Undefined.instance;
				}
				case 15: // operatorType.
				{
					return CORE.getOperatorType(
							getOperatorName(0, args));
				}
				case 16: // mode.
				{
					OperatorMode ret = CORE.getMode(
							getOperatorName(0, args));
					if(ret == null) {
						return null;
					}
					return ret.get();
				}
				case 17: // names.
				{
					return CORE.names();
				}
				case 18: // length.
				{
					return CORE.size();
				}
				
				}
			} catch (RhiginException re) {
				throw re;
			} catch (Exception e) {
				throw new RhiginException(500, e);
			}
			return Undefined.instance;
		}
		
		// オペレータ名を取得.
		private final String getOperatorName(int off, Object[] args) {
			if(args == null || args.length <= off||
				!(args[off] instanceof String)) {
				this.argsException("Level");
			}
			return (String)args[off];
		}
		
		// オペレータモードを取得.
		private final OperatorMode getOperatorMode(Object[] args) {
			int argsLen = 0;
			if(args == null || (argsLen = args.length) <= 1) {
				this.argsException("Level");
			}
			if(args[1] instanceof Map) {
				return new OperatorMode(args[1]);
			}
			if(argsLen <= 2) {
				this.argsException("Level");
			}
			Object[] params = new Object[argsLen-1];
			System.arraycopy(args, 1, params, 0, argsLen-1);
			return new OperatorMode(params);
		}
		
		@Override
		public final String getName() {
			switch (type) {
			case 0: return "version";
			case 1: return "name";
			case 2: return "startup";
			case 3: return "isStartup";
			case 4: return "config";
			case 5: return "machineId";
			case 6: return "createObject";
			case 7: return "createLatLon";
			case 8: return "createSequence";
			case 9: return "createQueue";
			case 10: return "delete";
			case 11: return "rename";
			case 12: return "contains";
			case 13: return "get";
			case 14: return "writeBatch";
			case 15: return "operatorType";
			case 16: return "mode";
			case 17: return "names";
			case 18: return "length";
			}
			return "unknown";
		}

	}
}