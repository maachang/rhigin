package rhigin.lib.level.runner;

import java.util.Map;

import org.maachang.leveldb.operator.LevelOperatorConstants;
import org.maachang.leveldb.util.Converter;

import rhigin.scripts.JsMap;
import rhigin.scripts.JsonOut;

/**
 * Leveldbコンフィグ.
 */
public class LevelJsConfig {
	protected String path;
	protected Integer machineId;
	
	/**
	 * コンストラクタ.
	 */
	protected LevelJsConfig() {
		
	}
	
	/**
	 * コンフィグ情報を読み込む.
	 * @param conf
	 * @return
	 */
	public static final LevelJsConfig create(Map<String, Object> json) {
		LevelJsConfig ret = new LevelJsConfig();
		ret.path = Converter.convertString(json.get("path"));
		if(ret.path == null || ret.path.isEmpty()) {
			ret.path = LevelOperatorConstants.DEFAULT_LEVEL_DB_FOLDER;
		}
		ret.machineId = Converter.convertInt(json.get("machineId"));
		if(ret.machineId == null || ret.machineId < 0) {
			ret.machineId = 0;
		}
		return ret;
	}
	
	/**
	 * 直接設定.
	 * @param path
	 * @param machineId
	 * @return
	 */
	public static final LevelJsConfig create(String path, Integer machineId) {
		LevelJsConfig ret = new LevelJsConfig();
		ret.path = path;
		ret.machineId = machineId;
		if(ret.path == null || ret.path.isEmpty()) {
			ret.path = LevelOperatorConstants.DEFAULT_LEVEL_DB_FOLDER;
		}
		if(ret.machineId == null || ret.machineId < 0) {
			ret.machineId = 0;
		}
		return ret;
	}
	
	/**
	 * Leveldbの基本パスを取得.
	 * @return
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * マシンIDを取得.
	 * @return
	 */
	public int getMachineId() {
		return machineId;
	}
	
	/**
	 * Kind設定内容をMapで取得.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<Object, Object> getMap() {
		return new JsMap("path", path, "machineId", machineId);
	}
	
	/**
	 * 文字列変換.
	 * @return
	 */
	@Override
	public String toString() {
		return JsonOut.toString(getMap());
	}

}
