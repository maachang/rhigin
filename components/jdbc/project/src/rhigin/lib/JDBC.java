package rhigin.lib;

import org.mozilla.javascript.Scriptable;

import rhigin.lib.jdbc.js.JDBCOperatorJs;
import rhigin.scripts.JavaRequire;

/**
 * [js]JDBCコンポーネント.
 * 
 * js 上で、以下のようにして呼び出します.
 * 
 * var jdbc = require("@rhigin/lib/JDBC");
 */
public class JDBC implements JavaRequire {
	
	/** コンポーネント名. **/
	public static final String NAME = "JDBC";
	
	/** コンポーネントバージョン. **/
	public static final String VERSION = "0.0.1";
	
	/**
	 * コンストラクタ.
	 */
	public JDBC() {
		// [JavaRequire]の場合は、public な空のコンストラクタは必須.
	}
	
	/**
	 * require呼び出しの返却処理.
	 */
	@Override
	public Scriptable load() {
		return JDBCOperatorJs.getJDBCOperatorJs();
	}
}
