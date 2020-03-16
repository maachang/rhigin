package rhigin.scripts;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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
	 * @param context スクリプトコンテキストを設定します.
	 * @param cache スクリプトコンパイルキャッシュを設定します.
	 */
	@Override
	public void call(CompileCache cache) {
		Reader r = null;
		try {
			if(cache == null) {
				r = new BufferedReader(new InputStreamReader(new FileInputStream(name), "UTF8"));
				ExecuteScript.eval(r, name, ScriptConstants.HEADER, ScriptConstants.FOOTER, 0);
				r.close();
				r = null;
			} else {
				ExecuteScript.eval(
					cache.get(name, ScriptConstants.HEADER, ScriptConstants.FOOTER).getScript());
			}
		} catch(RhiginException re) {
			throw re;
		} catch(Exception e) {
			throw new RhiginException(e);
		} finally {
			if(r != null) {
				try {
					r.close();
				} catch(Exception e) {}
			}
		}
	}
}
