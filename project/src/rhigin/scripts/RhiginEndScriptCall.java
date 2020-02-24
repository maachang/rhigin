package rhigin.scripts;

import rhigin.scripts.compile.CompileCache;

/**
 * スクリプト終了時のコール処理.
 */
public interface RhiginEndScriptCall {
	
	/**
	 * スクリプト終了時に処理を実行.
	 * @param cache スクリプトコンパイルキャッシュを設定します.
	 */
	public void call(CompileCache cache);
}
