package rhigin.lib.level.runner;

import rhigin.lib.level.LevelCore;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.scripts.compile.CompileCache;

/**
 * システム終了時に呼び出す処理.
 */
public class LevelSystemCloseable implements RhiginEndScriptCall {
	private LevelCore core = null;
	
	public LevelSystemCloseable(LevelCore c) {
		core = c;
	}
	
	/**
	 * 今回のスクリプト実行で利用したJDBCオブジェクト関連のクローズ処理.
	 * @params context
	 * @params cache
	 */
	@Override
	public final void call(RhiginContext context, CompileCache cache) {
		core.destroy();
	}
}
