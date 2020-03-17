package rhigin.scripts;

import rhigin.RhiginException;
import rhigin.scripts.compile.CompileCache;
import rhigin.util.FileUtil;

/**
 * スクリプト実行後にjsファイルを実行.
 */
public class ExecuteJsByEndScriptCall implements RhiginEndScriptCall {
	private String name = null;
	
	/**
	 * コンストラクタ.
	 * @param name スクリプトファイル名を設定します.
	 */
	public ExecuteJsByEndScriptCall(String name) {
		if(!name.toLowerCase().endsWith(".js")) {
			name += ".js";
		}
		if(!FileUtil.isFile(name)) {
			throw new RhiginException("The specified js file does not exist: " + name);
		}
		this.name = name;
	}
	
	/**
	 * スクリプト終了時にjsファイル処理を実行.
	 */
	@Override
	public void call() {
		CompileCache.eval(name, ScriptConstants.HEADER, ScriptConstants.FOOTER);
	}
}
