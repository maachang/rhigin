package rhigin;

import rhigin.http.HttpInfo;
import rhigin.scripts.ExecuteScript;
import rhigin.scripts.RhiginContext;
import rhigin.scripts.compile.CompileCache;
import rhigin.scripts.function.RandomFunction;
import rhigin.scripts.function.RequireFunction;
import rhigin.util.Args;
import rhigin.util.FileUtil;

/**
 * rhigin用テスト.
 * 
 * jasmineを利用してテスト.
 * ${RHIGIN_HOME}/test/jasmine/jasmine-core.js を利用する.
 */
public class RhiginTest {
	
	// テスト用フォルダ名.
	private static final String SPEC_DIR = "./spec";
	
	public static final void main(String[] args) throws Exception {
		if(System.getenv("RHIGIN_HOME") == null || System.getenv("RHIGIN_HOME").isEmpty()) {
			System.err.println("'RHIGIN_HOME' environment variable is not set.");
			System.exit(1);
			return;
		}
		Args.set(args);
		Args params = Args.getInstance();
		RhiginTest test = new RhiginTest();
		if(params.isValue("-v", "--versin")) {
			System.out.println(RhiginConstants.VERSION);
			System.exit(0);
			return;
		} else if(params.isValue("-h", "--help")) {
			System.out.println("rtest [-i] [-e] {specFileName}");
			System.out.println(" Perform test execution for rhigin using 'jasmine'.");
			System.out.println("  [-i] [--init]");
			System.out.println("    Perform test initialization.");
			System.out.println("  [-e] [--env]");
			System.out.println("    Set the environment name for reading the configuration.");
			System.out.println("    For example, when `-e hoge` is specified, the configuration ");
			System.out.println("    information under `./conf/hoge/` is read.");
			System.out.println("  {specFileName}");
			System.out.println("    If you want to run tests individually, set the Spec file name.");
			System.exit(0);
			return;
		} else if(params.isValue("-i", "--init")) {
			int ret = 0;
			try {
				test.initTest();
			} catch(Throwable t) {
				t.printStackTrace();
				ret = 1;
			}
			System.exit(ret);
			return;
		}
		
		if(!FileUtil.isDir(SPEC_DIR)) {
			System.err.println("'" + SPEC_DIR + "' folder for test execution does not exist.");
			System.exit(1);
			return;
		}
		
		int ret = 0;
		try {
			RhiginConfig conf = RhiginStartup.initLogFactory(false);
			test.executeTest(conf);
		} catch(Throwable t) {
			t.printStackTrace();
			ret = 1;
		}
		System.exit(ret);
	}
	
	// テスト初期化.
	private void initTest() throws Exception {
		FileUtil.mkdirs(SPEC_DIR);
	}
	
	// テスト実行.
	private void executeTest(RhiginConfig conf) throws Exception {
		// 開始処理.
		HttpInfo httpInfo = RhiginStartup.startup(conf);

		// コンパイルキャッシュ生成.
		// コンパイルキャッシュを require命令に設定.
		CompileCache cache = new CompileCache(httpInfo.getCompileCacheSize(), httpInfo.getCompileCacheRootDir());
		RequireFunction.init(cache);

		// ランダムオブジェクトをセット.
		RandomFunction.init();
		
		// jasmineJSのファイル位置.
		String jusminCoreJsFile = System.getenv("RHIGIN_HOME") + "/test/jasmine/lib/jasmine-core.js";
		
		RhiginContext context = new RhiginContext();
		Object o = ExecuteScript.execute(context, FileUtil.getFileString(jusminCoreJsFile, "UTF8"));
		System.out.println(o);
	}
}
