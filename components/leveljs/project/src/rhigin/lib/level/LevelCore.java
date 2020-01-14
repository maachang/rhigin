package rhigin.lib.level;

import java.util.List;
import java.util.Map;

import org.maachang.leveldb.LevelValues;
import org.maachang.leveldb.operator.LevelOperator;
import org.maachang.leveldb.operator.LevelOperatorManager;

import rhigin.RhiginConfig;
import rhigin.RhiginStartup;
import rhigin.lib.level.runner.LevelConfig;
import rhigin.lib.level.runner.LevelException;
import rhigin.lib.level.runner.LevelMode;
import rhigin.lib.level.runner.LevelSystemCloseable;
import rhigin.lib.level.runner.RhiginOriginCode;
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.util.Flag;

/**
 * Leveldb コアオブジェクト.
 */
public class LevelCore {
	/** デフォルトのLEVELコンフィグ名. **/
	public static final String DEF_LEVEL_JSON_CONFIG_NAME = "level";

	
	protected final Flag startup = new Flag(false);
	protected final Flag end = new Flag(false);
	private LevelConfig config = null;
	private LevelOperatorManager manager = null;
	
	/**
	 * コンストラクタ.
	 */
	public LevelCore() {
	}
	
	/**
	 * コアオブジェクト破棄.
	 */
	public void destroy() {
		if(end.setToGetBefore(true)) {
			if(manager != null) {
				manager.close();
			}
			manager = null;
		}
	}
	
	// オブジェクト破棄チェック.
	protected void checkDestroy() {
		if(end.get()) {
			throw new LevelException("The object has been destroyed.");
		}
	}
	
	// オブジェクト破棄チェック.
	// スタートアップしていない場合もチェック.
	protected void check() {
		if(!startup.get() || end.get()) {
			if(!startup.get()) {
				throw new LevelException("startup has not been performed.");
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
	public RhiginEndScriptCall startup(String configName, String[] args) {
		checkDestroy();
		if(!startup.get()) {
			final RhiginConfig conf = RhiginStartup.initLogFactory(false, true, args);
			return startup(conf, configName);
		}
		return new LevelSystemCloseable(this);
	}
	
	/**
	 * 初期化処理.
	 * 
	 * @param conf
	 * @param configName
	 * @return
	 */
	public RhiginEndScriptCall startup(RhiginConfig conf, String configName) {
		checkDestroy();
		if(!startup.get()) {
			String jsonConfigName = DEF_LEVEL_JSON_CONFIG_NAME;
			if(configName != null && !configName.isEmpty()) {
				jsonConfigName = configName;
			}
			startup(conf.get(jsonConfigName));
		}
		return new LevelSystemCloseable(this);
	}
	
	/**
	 * 初期化処理.
	 * 
	 * @param conf
	 * @return
	 */
	public RhiginEndScriptCall startup(Map<String, Object> conf) {
		checkDestroy();
		if(conf == null || conf.size() == 0) {
			throw new LevelException("level connection definition config object is not set.");
		}
		if(!startup.get()) {
			config = LevelConfig.create(conf);
			// Leveldbのマネージャ初期化.
			manager = new LevelOperatorManager(config.getPath(), config.getMachineId());
			// Rhigin向けvalue変換条件をセット.
			LevelValues.setOriginCode(new RhiginOriginCode());
			startup.set(true);
		}
		return new LevelSystemCloseable(this);
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
	public LevelConfig getConfig() {
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
	public boolean create(String name, LevelMode mode) {
		check();
		return manager.createMap(name, mode.getOption());
	}
	
	/**
	 * 緯度経度用オペレータオペレータを生成.
	 * 
	 * @param name オペレータ名を設定します.
	 * @param mode LevelModeを設定します.
	 * @return [true]の場合、生成成功です.
	 */
	public boolean createLatLon(String name, LevelMode mode) {
		check();
		return manager.createLatLon(name, mode.getOption());
	}
	
	/**
	 * シーケンスオペレータオペレータを生成.
	 * 
	 * @param name オペレータ名を設定します.
	 * @param mode LevelModeを設定します.
	 * @return [true]の場合、生成成功です.
	 */
	public boolean createSequence(String name, LevelMode mode) {
		check();
		return manager.createSequence(name, mode.getOption());
	}

	/**
	 * キューオペレータオペレータを生成.
	 * 
	 * @param name オペレータ名を設定します.
	 * @param mode LevelModeを設定します.
	 * @return [true]の場合、生成成功です.
	 */
	public boolean createQueue(String name, LevelMode mode) {
		check();
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
	 * データの中身を全クリア.
	 * @param name
	 * @return
	 */
	public boolean trancate(String name) {
		check();
		return manager.trancate(name);
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
	public LevelOperator get(String name) {
		check();
		return manager.get(name);
	}
	
	/**
	 * 登録オペレータのオペレータタイプを取得.
	 * 
	 * @param name
	 * @return
	 */
	public String getOperatorType(String name) {
		check();
		LevelOperator op = manager.get(name);
		if(op != null) {
			switch(op.getOperatorType()) {
			case LevelOperator.LEVEL_LAT_LON: return "latLon";
			case LevelOperator.LEVEL_MAP: return "normal";
			case LevelOperator.LEVEL_QUEUE: return "queue";
			case LevelOperator.LEVEL_SEQUENCE: return "sequence";
			}
		}
		return "none";
	}
	
	/**
	 * 登録オペレータのモードを取得.
	 * 
	 * @param name
	 * @return
	 */
	public LevelMode getMode(String name) {
		check();
		LevelOperator op = manager.get(name);
		if(op != null) {
			return new LevelMode(op.getOption());
		}
		return null;
	}
	
	/**
	 * 登録オペレータ名群を取得.
	 * 
	 * @return
	 */
	public List<String> names() {
		check();
		return manager.names();
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
}
