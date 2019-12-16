package rhigin.lib;

import org.mozilla.javascript.Scriptable;

import rhigin.scripts.JavaRequire;

/**
 * [js]mongo dbクライアントアクセス用のコンポーネント.
 * 
 * js 上で、以下のようにして呼び出します.
 * 
 * var jdbc = require("@rhigin/lib/Mongo");
 */
public class Mongo implements JavaRequire {
	
	/** コンポーネント名. **/
	public static final String NAME = "Mongo";
	
	/** コンポーネントバージョン. **/
	public static final String VERSION = "0.0.1";
	
	public Mongo() {
		// [JavaRequire]の場合は、public な空のコンストラクタは必須.
	}

	/**
	 * require呼び出しの返却処理.
	 */
	@Override
	public Scriptable load() {
		return null;
	}
	
	
}