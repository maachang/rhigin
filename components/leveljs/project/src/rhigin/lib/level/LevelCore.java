package rhigin.lib.level;

import java.util.Map;

import org.maachang.leveldb.LevelValues;
import org.maachang.leveldb.operator.LevelOperatorManager;

import rhigin.RhiginConfig;
import rhigin.RhiginStartup;
import rhigin.lib.level.runner.LevelConfig;
import rhigin.lib.level.runner.LevelException;
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
	 * @return
	 */
	public boolean isDestroy() {
		return end.get();
	}
	
	/**
	 * 初期化処理.
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
	 * @return
	 */
	public boolean isStartup() {
		checkDestroy();
		return startup.get();
	}
	
	/**
	 * LevelConfigを取得.
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
	
	
	
}
