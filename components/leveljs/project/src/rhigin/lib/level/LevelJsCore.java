package rhigin.lib.level;

import java.util.List;
import java.util.Map;

import org.maachang.leveldb.LevelOption;
import org.maachang.leveldb.LevelValues;
import org.maachang.leveldb.operator.LevelLatLon;
import org.maachang.leveldb.operator.LevelMap;
import org.maachang.leveldb.operator.LevelOperator;
import org.maachang.leveldb.operator.LevelOperatorManager;
import org.maachang.leveldb.operator.LevelQueue;
import org.maachang.leveldb.operator.LevelSequence;

import rhigin.RhiginConfig;
import rhigin.lib.level.operator.LatLonOperator;
import rhigin.lib.level.operator.ObjectOperator;
import rhigin.lib.level.operator.Operator;
import rhigin.lib.level.operator.OperatorKeyType;
import rhigin.lib.level.operator.OperatorMode;
import rhigin.lib.level.operator.OperatorUtil;
import rhigin.lib.level.operator.QueueOperator;
import rhigin.lib.level.operator.SequenceOperator;
import rhigin.lib.level.runner.LevelJsCloseable;
import rhigin.lib.level.runner.LevelJsConfig;
import rhigin.lib.level.runner.LevelJsException;
import rhigin.lib.level.runner.LevelJsSystemCloseable;
import rhigin.lib.level.runner.RhiginOriginCode;
import rhigin.scripts.JavaScriptable;
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.util.Flag;

/**
 * Leveldb コアオブジェクト.
 */
public class LevelJsCore {
	/** デフォルトのLEVELコンフィグ名. **/
	public static final String DEF_LEVEL_JS_JSON_CONFIG_NAME = "level";
	
	protected final Flag startup = new Flag(false);
	protected final Flag end = new Flag(false);
	protected LevelJsConfig config = null;
	protected LevelOperatorManager manager = null;
	protected LevelJsCloseable closeable = null;
	
	/**
	 * コンストラクタ.
	 */
	public LevelJsCore() {
	}
	
	/**
	 * コアオブジェクト破棄.
	 */
	public void destroy() {
		if(end.setToGetBefore(true)) {
			if(closeable != null) {
				closeable.call();
			}
			closeable = null;
			if(manager != null) {
				manager.close();
			}
			manager = null;
		}
	}
	
	// オブジェクト破棄チェック.
	protected void checkDestroy() {
		if(end.get()) {
			throw new LevelJsException("The object has been destroyed.");
		}
	}
	
	// オブジェクト破棄チェック.
	// スタートアップしていない場合もチェック.
	protected void check() {
		if(!startup.get() || end.get()) {
			if(!startup.get()) {
				throw new LevelJsException("startup has not been performed.");
			}
			checkDestroy();
		}
	}
	
	/**
	 * オブジェクト破棄チェック.
	 * 
	 * @return
	 */
	public boolean isDestroy() {
		return end.get();
	}
	
	/**
	 * 初期化処理.
	 * 
	 * @param configName
	 * @param argsc
	 * @return
	 */
	public RhiginEndScriptCall[] startup(String configName, String[] args) {
		checkDestroy();
		if(!startup.get()) {
			final RhiginConfig conf = RhiginConfig.getMainConfig();
			return startup(conf, configName);
		}
		return new RhiginEndScriptCall[] {
			closeable,
			new LevelJsSystemCloseable(this)
		};
	}
	
	/**
	 * 初期化処理.
	 * 
	 * @param conf
	 * @param configName
	 * @return
	 */
	public RhiginEndScriptCall[] startup(RhiginConfig conf, String configName) {
		checkDestroy();
		if(!startup.get()) {
			String jsonConfigName = DEF_LEVEL_JS_JSON_CONFIG_NAME;
			if(configName != null && !configName.isEmpty()) {
				jsonConfigName = configName;
			}
			startup(conf.get(jsonConfigName));
		}
		return new RhiginEndScriptCall[] {
			closeable,
			new LevelJsSystemCloseable(this)
		};
	}
	
	/**
	 * 初期化処理.
	 * 
	 * @param conf
	 * @return
	 */
	public RhiginEndScriptCall[] startup(Map<String, Object> conf) {
		checkDestroy();
		if(conf == null || conf.size() == 0) {
			throw new LevelJsException("level connection definition config object is not set.");
		}
		if(!startup.get()) {
			config = LevelJsConfig.create(conf);
			// Leveldbのマネージャ初期化.
			manager = new LevelOperatorManager(config.getPath(), config.getMachineId());
			// Rhigin向けvalue変換条件をセット.
			LevelValues.setOriginCode(new RhiginOriginCode());
			closeable = new LevelJsCloseable();
			startup.set(true);
		}
		return new RhiginEndScriptCall[] {
			closeable,
			new LevelJsSystemCloseable(this)
		};
	}
	
	/**
	 * 初期化処理が行われているかチェック.
	 * 
	 * @return
	 */
	public boolean isStartup() {
		checkDestroy();
		return startup.get();
	}
	
	/**
	 * LevelConfigを取得.
	 * 
	 * @return
	 */
	public LevelJsConfig getConfig() {
		check();
		return config;
	}
	
	/**
	 * マシンIDを取得.
	 * 
	 * @return
	 */
	public int getMachineId() {
		check();
		return manager.getMachineId();
	}
	
	/**
	 * 通常オペレータを生成.
	 * 
	 * @param name オペレータ名を設定します.
	 * @param mode LevelModeを設定します.
	 * @return [true]の場合、生成成功です.
	 */
	public boolean createObject(String name, OperatorMode mode) {
		check();
		// オペレータキーが設定されていない場合はエラーを返却.
		if(mode.getOperatorType() == OperatorKeyType.KEY_NONE) {
			throw new LevelJsException(
				"When creating an Object Operator, setting the Operator key Type is mandatory.");
		}
		return manager.createMap(name, mode.getOption());
	}
	
	/**
	 * 緯度経度用オペレータオペレータを生成.
	 * 
	 * @param name オペレータ名を設定します.
	 * @param mode LevelModeを設定します.
	 * @return [true]の場合、生成成功です.
	 */
	public boolean createLatLon(String name, OperatorMode mode) {
		check();
		// オペレータキーが設定されていない場合は、完全に無設定として設定する.
		if(mode.getOperatorType() == OperatorKeyType.KEY_NONE) {
			LevelOption opt = mode.getOption();
			opt.setType(LevelOption.TYPE_NONE);
			opt.setExpansion(OperatorKeyType.KEY_NONE);
		}
		return manager.createLatLon(name, mode.getOption());
	}
	
	/**
	 * シーケンスオペレータオペレータを生成.
	 * 
	 * @param name オペレータ名を設定します.
	 * @param mode LevelModeを設定します.
	 * @return [true]の場合、生成成功です.
	 */
	public boolean createSequence(String name, OperatorMode mode) {
		check();
		// キーはシーケンスIDとなるので、オペレータキータイプは設定できない.
		LevelOption opt = mode.getOption();
		opt.setType(LevelOption.TYPE_NONE);
		opt.setExpansion(OperatorKeyType.KEY_NONE);
		return manager.createSequence(name, opt);
	}

	/**
	 * キューオペレータオペレータを生成.
	 * 
	 * @param name オペレータ名を設定します.
	 * @param mode LevelModeを設定します.
	 * @return [true]の場合、生成成功です.
	 */
	public boolean createQueue(String name, OperatorMode mode) {
		check();
		// キーはシーケンスIDとなるので、オペレータキータイプは設定できない.
		LevelOption opt = mode.getOption();
		opt.setType(LevelOption.TYPE_NONE);
		opt.setExpansion(OperatorKeyType.KEY_NONE);
		return manager.createQueue(name, mode.getOption());
	}
	
	/**
	 * 指定オペレータを削除.
	 * 
	 * @param name
	 * @return
	 */
	public boolean delete(String name) {
		check();
		return manager.delete(name);
	}
	
	/**
	 * 指定オペレータの名前変更.
	 * 
	 * @param src
	 * @param dest
	 * @return
	 */
	public boolean rename(String src, String dest) {
		check();
		return manager.rename(src, dest);
	}
	
	/**
	 * オペレータが登録されてるかチェック.
	 * 
	 * @param name
	 * @return
	 */
	public boolean contains(String name) {
		check();
		return manager.contains(name);
	}
	
	/**
	 * 登録オペレータを取得.
	 * 
	 * @param name
	 * @return
	 */
	public Operator get(String name) {
		check();
		final LevelOperator op = manager.get(name);
		if(op != null) {
			switch(op.getOperatorType()) {
			case LevelOperator.LEVEL_MAP:
				return new ObjectOperator(closeable, name, (LevelMap)op);
			case LevelOperator.LEVEL_LAT_LON:
				return new LatLonOperator(closeable, name, (LevelLatLon)op);
			case LevelOperator.LEVEL_SEQUENCE:
				return new SequenceOperator(closeable, name, (LevelSequence)op);
			case LevelOperator.LEVEL_QUEUE:
				return new QueueOperator(closeable, name, (LevelQueue)op);
			}
		}
		return null;
	}
	
	/**
	 * 登録オペレータをWriteBatchモードで取得.
	 * 
	 * @param name
	 * @return
	 */
	public Operator getWriteBatch(String name) {
		check();
		final LevelOperator op = manager.get(name);
		if(op != null) {
			switch(op.getOperatorType()) {
			case LevelOperator.LEVEL_MAP:
				return new ObjectOperator(closeable, name, new LevelMap((LevelMap)op));
			case LevelOperator.LEVEL_LAT_LON:
				return new LatLonOperator(closeable, name, new LevelLatLon((LevelLatLon)op));
			case LevelOperator.LEVEL_SEQUENCE:
				return new SequenceOperator(closeable, name, new LevelSequence((LevelSequence)op));
			case LevelOperator.LEVEL_QUEUE:
				return new QueueOperator(closeable, name, new LevelQueue((LevelQueue)op));
			}
		}
		return null;
	}
	
	/**
	 * 登録オペレータのオペレータタイプを取得.
	 * 
	 * @param name 登録オペレータ名を設定します.
	 * @return String オペレータタイプが返却されます.
	 *                "object" の場合は ObjectOperatorです.
	 *                "latlon" の場合は LatLonOperatorです.
	 *                "sequence" の場合は SequenceOperatorです.
	 *                "queue" の場合は QueueOperatorです.
	 */
	public String getOperatorType(String name) {
		check();
		return OperatorUtil.getOperatorType(manager.get(name));
	}
	
	/**
	 * 登録オペレータのモードを取得.
	 * 
	 * @param name
	 * @return
	 */
	public OperatorMode getMode(String name) {
		check();
		LevelOperator op = manager.get(name);
		if(op != null) {
			return new OperatorMode(op.getOption());
		}
		return null;
	}
	
	/**
	 * 登録オペレータ名群を取得.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> names() {
		check();
		return new JavaScriptable.GetList(manager.names());
	}
	
	/**
	 * 登録オペレータ数を取得.
	 * 
	 * @return
	 */
	public int size() {
		check();
		return manager.size();
	}
	
	/**
	 * LevelOperatorManagerを取得.
	 * 
	 * @return
	 */
	public LevelOperatorManager getManager() {
		check();
		return manager;
	}
}
