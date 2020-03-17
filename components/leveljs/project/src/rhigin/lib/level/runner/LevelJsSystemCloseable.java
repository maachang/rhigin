package rhigin.lib.level.runner;

import rhigin.lib.level.LevelJsCore;
import rhigin.scripts.RhiginEndScriptCall;

/**
 * システム終了時に呼び出す処理.
 */
public class LevelJsSystemCloseable implements RhiginEndScriptCall {
	private LevelJsCore core = null;
	
	public LevelJsSystemCloseable(LevelJsCore c) {
		core = c;
	}
	
	/**
	 * 今回のスクリプト実行で利用したJDBCオブジェクト関連のクローズ処理.
	 */
	@Override
	public final void call() {
		core.destroy();
	}
}
