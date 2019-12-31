package rhigin.lib;

import org.mozilla.javascript.Scriptable;

import rhigin.scripts.JavaRequire;

/**
 * [js] redis クライアントアクセス用のコンポーネント.
 * 
 * js 上で、以下のようにして呼び出します.
 * 
 * var redis = require("@rhigin/lib/Redis");
 */
public class Redis implements JavaRequire {
	
	/** コンポーネント名. **/
	public static final String NAME = "Redis";
	
	/** コンポーネントバージョン. **/
	public static final String VERSION = "0.0.1";
	
	public Redis() {
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
