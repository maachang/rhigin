package rhigin.scripts;

/**
 * スクリプト定義.
 */
public class ScriptConstants {
	/** デフォルトキャッシュFunction数. **/
	public static final int DEF_CACHE_FUNCTION_LENGTH = 15;
	
	/** パラメーターなし. **/
	public static final Object[] BLANK_ARGS = new Object[0];

	/** js呼び出しのヘッダスクリプト. **/
	public static final String HEADER = "'use strict';(function(_g){\n";

	/** js呼び出しのフッタスクリプト. **/
	public static final String FOOTER = "\n})(this);";
}
