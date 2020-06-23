package rhigin.lib.level.js;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhigin.RhiginConfig;
import rhigin.RhiginException;
import rhigin.lib.Level;
import rhigin.lib.level.LevelJsBackup;
import rhigin.lib.level.LevelJsCore;
import rhigin.lib.level.LevelJsCsv;
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
	
	// オブジェクト名.
	protected static final String OBJECT_NAME = "Level";
	
	// LevelJsマネージャインスタンス.
	protected static final RhiginObject LEVEL_JS_INSTANCE = new RhiginObject(OBJECT_NAME, new RhiginFunction[] {
		new LevelJsManFunctions(0), new LevelJsManFunctions(1), new LevelJsManFunctions(2), new LevelJsManFunctions(3),
		new LevelJsManFunctions(4), new LevelJsManFunctions(5), new LevelJsManFunctions(6), new LevelJsManFunctions(7),
		new LevelJsManFunctions(8), new LevelJsManFunctions(9), new LevelJsManFunctions(10), new LevelJsManFunctions(11),
		new LevelJsManFunctions(12), new LevelJsManFunctions(13), new LevelJsManFunctions(14), new LevelJsManFunctions(15),
		new LevelJsManFunctions(16), new LevelJsManFunctions(17), new LevelJsManFunctions(18), new LevelJsManFunctions(19),
		new LevelJsManFunctions(20), new LevelJsManFunctions(21), new LevelJsManFunctions(22)
	});
	
	/**
	 * LevelJsManagerJs オブジェクトを取得.
	 * @return
	 */
	public static final RhiginObject getLevelJsManagerJs() {
		return LEVEL_JS_INSTANCE;
	}
	
	/**
	 * LevelJsマネージャのメソッド群. 
	 */
	private static final class LevelJsManFunctions extends RhiginFunction {
		private final int type;

		LevelJsManFunctions(int t) {
			this.type = t;
		}
		
		@Override
		public final Object jcall(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			LevelJsCore core = LevelJsCore.getInstance();
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
					if(!core.isStartup()) {
						RhiginEndScriptCall[] es = null;
						final RhiginConfig conf = RhiginConfig.getMainConfig();
						if(args.length > 0) {
							es = core.startup(conf, "" + args[0]);
						} else {
							es = core.startup(conf, null);
						}
						ExecuteScript.addEndScripts(es[0]);
						ExecuteScript.addExitSystemScripts(es[1]);
						return true;
					}
					return false;
				}
				case 3: // isStartup.
				{
					return core.isStartup();
				}
				case 4: // config.
				{
					return core.getConfig().getMap();
				}
				case 5: // machineId.
				{
					return core.getMachineId();
				}
				case 6: // createObject.
				{
					String name = getOperatorName(0, args);
					OperatorMode mode = getOperatorMode(args);
					// オペレータキータイプが設定されて無い場合は、文字列のキータイプとする.
					if(mode.getOperatorType() == OperatorKeyType.KEY_NONE) {
						mode.set("type", OperatorKeyType.KEY_STRING);
					}
					return core.createObject(name, mode);
				}
				case 7: // createLatLon.
				{
					String name = getOperatorName(0, args);
					OperatorMode mode = getOperatorMode(args);
					return core.createLatLon(name, mode);
				}
				case 8: // createSequence.
				{
					String name = getOperatorName(0, args);
					OperatorMode mode = getOperatorMode(args);
					return core.createSequence(name, mode);
				}
				case 9: // createQueue.
				{
					String name = getOperatorName(0, args);
					OperatorMode mode = getOperatorMode(args);
					// 必ずオペレータキータイプは「なし」.
					mode.set("type", OperatorKeyType.KEY_NONE);
					return core.createQueue(name, mode);
				}
				case 10: // delete.
				{
					return core.delete(
							getOperatorName(0, args));
				}
				case 11: // rename.
				{
					return core.rename(
							getOperatorName(0, args),
							getOperatorName(1, args));
				}
				case 12: // contains.
				{
					return core.contains(
							getOperatorName(0, args));
				}
				case 13: // get.
				{
					// commit / rollback が利用出来ないモードで取得.
					final Operator op = core.get(
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
					final Operator op = core.getWriteBatch(
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
					return core.getOperatorType(
							getOperatorName(0, args));
				}
				case 16: // mode.
				{
					OperatorMode ret = core.getMode(
							getOperatorName(0, args));
					if(ret == null) {
						return null;
					}
					return ret.get();
				}
				case 17: // names.
				{
					return core.names();
				}
				case 18: // length.
				{
					return core.size();
				}
				case 19: // csvImport.
				{
					if(args == null || args.length == 0) {
						this.argsException(OBJECT_NAME);
					}
					if(args.length == 1) {
						return LevelJsCsv.execute(core, false, null, "" + args[0]);
					}
					return LevelJsCsv.execute(core, false, "" + args[0], "" + args[1]);
				}
				case 20: // csvDirect.
				{
					if(args == null || args.length < 2) {
						this.argsException(OBJECT_NAME);
					}
					BufferedReader br = new BufferedReader(new StringReader("" + args[1]));
					try {
						return LevelJsCsv.execute(core, "" + args[0], false, br);
					} finally {
						try {
							br.close();
						} catch(Exception e) {}
					}
				}
				case 21: // backup.
				{
					if(args == null || args.length < 2) {
						this.argsException(OBJECT_NAME);
					}
					String fileName = "" + args[0];
					String operatorName = "" + args[1];
					if(!core.contains(operatorName)) {
						throw new RhiginException("The backup operator '"
								+ operatorName + "' does not exist.");
					}
					BufferedOutputStream bo = null;
					try {
						bo = new BufferedOutputStream(new FileOutputStream(fileName));
						return LevelJsBackup.backup(bo, core, operatorName);
					} finally {
						try {
							bo.close();
						} catch(Exception e) {}
					}
				}
				case 22: // restore.
				{
					if(args == null || args.length < 1) {
						this.argsException(OBJECT_NAME);
					}
					String fileName = "" + args[0];
					BufferedInputStream bi = null;
					try {
						bi = new BufferedInputStream(new FileInputStream(fileName));
						return LevelJsBackup.restore(core, bi);
					} finally {
						try {
							bi.close();
						} catch(Exception e) {}
						}
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
				this.argsException(OBJECT_NAME);
			}
			return (String)args[off];
		}
		
		// オペレータモードを取得.
		private final OperatorMode getOperatorMode(Object[] args) {
			int argsLen = 0;
			if(args == null || (argsLen = args.length) <= 1) {
				//this.argsException(OBJECT_NAME);
				return new OperatorMode();
			}
			if(args[1] instanceof Map) {
				return new OperatorMode(args[1]);
			}
			if(argsLen <= 2) {
				//this.argsException(OBJECT_NAME);
				return new OperatorMode();
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
			case 19: return "csvImport";
			case 20: return "csvDirect";
			case 21: return "backup";
			case 22: return "restore";
			}
			return "unknown";
		}

	}
}