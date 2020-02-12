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
import rhigin.util.Args;
import rhigin.util.FileUtil;

/**
 * Rhiginバッチ実行.
 */
public class RhiginBatch {
	public static final void main(String[] args) throws Exception {
		Args params = Args.set(args);
		if(viewArgs()) {
			System.exit(0);
			return;
		}
		int ret = 0;
		try {
			String fileName = params.get("-f", "--file");
			// 引数が存在しない場合.
			if (fileName == null || fileName.isEmpty()) {
				System.err.println("The file to be executed has not been set.");
				System.exit(1);
				return;
			} else if (!FileUtil.isFile(fileName)) {
				System.err.println("Target file does not exist: " + fileName);
				System.exit(1);
				return;
			}
			RhiginConfig conf = RhiginStartup.init(false, true);
			RhiginBatch o = new RhiginBatch();
			// バッチ実行.
			if (!o.batch(conf, fileName)) {
				ret = 1;
			}
		} catch(Throwable t) {
			t.printStackTrace();
			ret = 1;
		}
		System.exit(ret);
	}
	
	// プログラム引数による命令.
	private static final boolean viewArgs() {
		Args params = Args.getInstance();
		if(params.isValue("-v", "--version")) {
			System.out.println(RhiginConstants.VERSION);
			return true;
		} else if(params.isValue("-h", "--help")) {
			System.out.println("rbatch [-e] [-f]");
			System.out.println(" Perform rhigin batch execution.");
			System.out.println("  [-e] [--env]");
			System.out.println("    Set the environment name for reading the configuration.");
			System.out.println("    For example, when `-e hoge` is specified, the configuration ");
			System.out.println("    information under `./conf/hoge/` is read.");
			System.out.println("  [-f] [--file] {fileName}");
			System.out.println("    Set the Javascript file to be executed.");
			System.out.println("    This setting is required.");
			return true;
		}
		return false;
	}

	public boolean batch(RhiginConfig conf, String fileName) throws Exception {
		// 開始処理.
		HttpInfo httpInfo = RhiginStartup.startup(conf);

		// コンパイルキャッシュ生成.
		// コンパイルキャッシュを require命令に設定.
		final CompileCache cache = new CompileCache(httpInfo.getCompileCacheSize(), httpInfo.getCompileCacheRootDir());
		RequireFunction.init(cache);

		// ランダムオブジェクトをセット.
		RandomFunction.init();

		Reader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));
			final Script script = ExecuteScript.compile(r, fileName, ScriptConstants.HEADER, ScriptConstants.FOOTER, 0);
			r.close();
			r = null;
			try {
				ExecuteScript.execute(new RhiginContext(), script);
			} finally {
				ExecuteScript.callEndScripts(false, cache);
				ExecuteScript.callEndScripts(true, cache);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (Exception e) {
				}
			}
		}
	}
}
