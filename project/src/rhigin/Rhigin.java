package rhigin;

import rhigin.downs.CallbackShutdown;
import rhigin.downs.ShutdownHook;
import rhigin.downs.WaitShutdown;
import rhigin.http.ExitCall;
import rhigin.http.Http;
import rhigin.http.HttpInfo;
import rhigin.http.MimeType;
import rhigin.logs.Log;
import rhigin.logs.LogFactory;
import rhigin.net.NioUtil;
import rhigin.scripts.ExecuteScript;
import rhigin.util.Args;
import rhigin.util.Converter;

/**
 * rhiginメインオブジェクト.
 */
public class Rhigin {
	static {
		// ネットワーク関連の初期設定.
		NioUtil.initNet();
	}
	
	// ロガー.
	private static Log LOG = null;

	/** Main. **/
	public static final void main(String[] args) {
		// プログラム引数による命令が完了した場合.
		Args.set(args);
		if(viewArgs()) {
			System.exit(0);
			return;
		}
		int ret = 0;
		RhiginConfig conf = RhiginStartup.initLogFactory(true);
		LOG = LogFactory.create();
		try {
			LOG.info("start rhigin version (" + RhiginConstants.VERSION + ").");
			Rhigin rhigin = new Rhigin();
			rhigin.execute(conf);
		} catch (Throwable e) {
			LOG.error("rhiginError", e);
			ret = 1;
		} finally {
			LOG.info("exit rhigin.");
		}
		System.exit(ret);
	}

	// RhiginHttpオブジェクトをシャットダウン.
	public static final class ShutdownHttp extends CallbackShutdown {
		protected Log log;
		protected Http http;

		public ShutdownHttp(Log log, Http http) {
			this.log = log;
			this.http = http;
		}

		/**
		 * シャットダウンフック：Http 終了コールを呼び出す.
		 */
		public final void execution() {
			log.info("start shutdown Rhigin.");

			// 各サービス停止.
			http.stop();
			while (!http.isExit()) {
				try {
					Thread.sleep(5);
				} catch (Exception e) {
				}
			}
			// シャットダウン時の処理終了コールバック.
			if (http.exitCall() != null) {
				http.exitCall().call();
			}
			// システム終了時に呼び出す処理.
			ExecuteScript.callEndScripts(true, null);
			
			// 終了ログ.
			log.info("end shutdown Rhigin version (" + RhiginConstants.VERSION + ").");
		}
	}
	
	// プログラム引数による命令.
	private static final boolean viewArgs() {
		Args params = Args.getInstance();
		if(params.isValue("-v", "--version")) {
			System.out.println(RhiginConstants.VERSION);
			return true;
		} else if(params.isValue("-h", "--help")) {
			System.out.println("rhigin [-e]");
			System.out.println(" Start the rhigin server.");
			System.out.println("  [-e] [--env]");
			System.out.println("    Set the environment name for reading the configuration.");
			System.out.println("    For example, when `-e hoge` is specified, the configuration ");
			System.out.println("    information under `./conf/hoge/` is read.");
			return true;
		}
		return false;
	}

	/** http. **/
	protected Http http;

	/** 起動処理. **/
	protected final void execute(RhiginConfig conf) throws Exception {
		// 開始処理.
		HttpInfo httpInfo = RhiginStartup.startup(conf);

		// 生成されたMimeTypeを取得.
		MimeType mime = (MimeType) ExecuteScript.getOriginal().get("mime");

		// Httpの生成.
		http = new Http(httpInfo, mime);

		// 終了コールバック処理をセット.
		http.exitCall(exitCall());

		// HTTP開始.
		http.start();

		// シャットダウンまで待つ処理を生成.
		int shutdownPort = -1;
		if (Converter.isNumeric(conf.get("http", "shutdownPort"))) {
			shutdownPort = conf.getInt("http", "shutdownPort");
		}

		// シャットダウンフックセット.
		ShutdownHttp sd = new ShutdownHttp(LOG, http);
		ShutdownHook.registHook(sd);

		// サーバーシャットダウン待ち.
		WaitShutdown.waitSignal(shutdownPort, 0);
	}

	/** エラー出力. **/
	protected final void error(String errMessage) {
		LOG.error(errMessage);
		System.exit(-1);
	}

	/** サーバ終了処理. **/
	private final ExitCall exitCall() {
		return new ExitCall() {
			public void call() {
				// 現状処理なし.
			}
		};
	}
}
