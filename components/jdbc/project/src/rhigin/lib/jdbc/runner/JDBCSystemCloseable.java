package rhigin.lib.jdbc.runner;

import rhigin.lib.jdbc.JDBCCore;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.RhiginEndScriptCall;
import rhigin.scripts.compile.CompileCache;

/**
 * システム終了時に呼び出す処理.
 */
public class JDBCSystemCloseable implements RhiginEndScriptCall {
	private JDBCCore core = null;
	
	public JDBCSystemCloseable(JDBCCore c) {
		core = c;
	}
	
	/**
	 * 今回のスクリプト実行で利用したJDBCオブジェクト関連のクローズ処理.
	 * @params cache
	 */
	@Override
	public final void call(CompileCache cache) {
		core.destroy();
	}
}
