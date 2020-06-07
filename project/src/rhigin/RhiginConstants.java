package rhigin;

import rhigin.util.EnvCache;
import rhigin.util.FileUtil;

/**
 * Rhigin用定義.
 */
public class RhiginConstants {
	protected RhiginConstants() {
	}
	
	/** Rhigin環境変数名. **/
	public static final String ENV_HOME = "RHIGIN_HOME";
	
	/** RHIGIN_HOME 内容. **/
	public static final String RHIGIN_HOME;
	
	static {
		String rhigin_home = "";
		try {
			rhigin_home = EnvCache.get(ENV_HOME);
			rhigin_home = FileUtil.getFullPath(rhigin_home);
		} catch(Exception e) {}
		RHIGIN_HOME = rhigin_home;
	}

	/** プロジェクトバージョン. **/
	public static final String VERSION = "0.0.1";

	/** プロジェクト名. **/
	public static final String NAME = "rhigin";

	/** Rhigin実行環境変数名. **/
	public static final String ENV_ENV = "RHIGIN";
	
	/** コンフィグフォルダ名. **/
	public static final String CONFIG_NAME = "conf";

	/** コンフィグパス. **/
	public static final String DIR_CONFIG = "./" + CONFIG_NAME + "/";

	/** ログパス. **/
	public static final String DIR_LOG = "./log/";

	/** main.js **/
	public static final String MAIN_JS = "main.js";
}
