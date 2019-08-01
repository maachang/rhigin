package rhigin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.mozilla.javascript.Script;

import rhigin.http.HttpInfo;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.ScriptConstants;
import rhigin.scripts.compile.CompileCache;
import rhigin.scripts.function.RandomFunction;
import rhigin.scripts.function.RequireFunction;
import rhigin.util.FileUtil;

/**
 * Rhiginバッチ実行.
 */
public class RhiginBatch {
	public static final void main(String[] args) throws Exception {
		// 引数が存在しない場合.
		if(args == null || args.length == 0 || args[0].length() == 0) {
			System.out.println("The file to be executed has not been set.");
			System.exit(1); return;
		} else if(!FileUtil.isFile(args[0])) {
			System.out.println("Target file does not exist:" + args[0]);
			System.exit(1); return;
		}
		RhiginConfig conf = RhiginStartup.initLogFactory(false, args);
		RhiginBatch o = new RhiginBatch();
		
		// バッチ実行.
		if(o.batch(conf, args)) {
			System.exit(0);
		} else {
			System.exit(2);
		}
	}
	
	public boolean batch(RhiginConfig conf, String[] args) throws Exception {
		// 開始処理.
		HttpInfo httpInfo = RhiginStartup.startup(conf);
		
		// コンパイルキャッシュ生成.
		// コンパイルキャッシュを require命令に設定.
		final CompileCache cache = new CompileCache(
			httpInfo.getCompileCacheSize(), httpInfo.getCompileCacheRootDir());
		RequireFunction.init(cache);
		
		// ランダムオブジェクトをセット.
		RandomFunction.init();
		
		Reader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF8"));
			final Script script = ExecuteScript.compile(r, args[0], ScriptConstants.HEADER, ScriptConstants.FOOTER, 0);
			r.close(); r = null;
			ExecuteScript.execute(new RhiginContext(), script);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if(r != null) {
				try { r.close(); } catch(Exception e) {}
			}
		}
	}
}
