package rhigin.lib;

import org.mozilla.javascript.Scriptable;

import rhigin.scripts.JavaRequire;

/**
 * [js] Level js 用コンポーネント.
 * 
 * js 上で、以下のようにして呼び出します.
 * 
 * var level = require("@rhigin/lib/Level");
 */
public class Level implements JavaRequire {
	
	/** コンポーネント名. **/
	public static final String NAME = "LevelJs";
	
	/** コンポーネントバージョン. **/
	public static final String VERSION = "0.0.1";
	
	public Level() {
		// [JavaRequire]の場合は、public な空のコンストラクタは必須.
	}

	/**
	 * require呼び出しの返却処理.
	 */
	@Override
	public Scriptable load() {
		return LevelJsManagerJs.LEVEL_JS_INSTANCE;
	}
}
