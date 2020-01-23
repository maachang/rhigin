package rhigin.http;

import java.nio.channels.ServerSocketChannel;

import rhigin.net.NioCore;
import rhigin.net.NioUtil;
import rhigin.util.FileUtil;

/**
 * Httpオブジェクト.
 */
public final class Http {
	// net設定.
	protected static final boolean TCP_NO_DELAY = false; // Nagle アルゴリズムを有効にします.
	protected static final boolean KEEP_ALIVE = false; // TCP-KeepAliveを無効に設定します.

	/** Nio処理. **/
	private NioCore nio = null;

	/** Callback. **/
	private ExitCall call = null;

	/** HttpInfo. **/
	private static HttpInfo httpInfo = null;

	/** webサーバモード. **/
	private static boolean webServer = false;
	
	/** コンソールモード. **/
	private static boolean consoleMode = false;
	
	/** 同期. **/
	private static final Object sync = new Object();

	/**
	 * HttpInfoを取得.
	 * 
	 * @return
	 */
	public static final HttpInfo getHttpInfo() {
		return httpInfo;
	}

	/**
	 * 起動モードを設定.
	 * 
	 * @param web [true]の場合、WebServerモードです.
	 * @param console [true]の場合、コンソールモードです.
	 */
	public static final void setMode(boolean web, boolean console) {
		synchronized(sync) {
			webServer = web;
			consoleMode = console;
		}
	}

	/**
	 * Webサーバモードを取得.
	 * 
	 * @return boolean [true]の場合Webサーバです.
	 */
	public static final boolean isWebServerMode() {
		synchronized(sync) {
			return webServer;
		}
	}
	
	/**
	 * コンソールモードを取得.
	 * 
	 * @return boolean [true]の場合コンソールモードです.
	 */
	public static final boolean isConsoleMode() {
		synchronized(sync) {
			return consoleMode;
		}
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param info
	 * @param mime
	 * @throws Exception
	 */
	public Http(HttpInfo info, MimeType mime) throws Exception {
		// httpInfoをシングルトンとして登録.
		if (httpInfo == null) {
			httpInfo = info;
		}

		// bodyファイル格納先のフォルダを作成.
		if (!FileUtil.isDir(HttpConstants.POST_FILE_OUT_ROOT_DIR)) {
			FileUtil.mkdirs(HttpConstants.POST_FILE_OUT_ROOT_DIR);
		}

		// nio:サーバーソケット作成.
		ServerSocketChannel ch = NioUtil.createServerSocketChannel(info.getSocketReceiveBuffer(),
				info.getLocalAddress(), info.getLocalPort(), info.getBacklog());

		// nio処理を生成.
		this.nio = new NioCore(info.getByteBufferLength(), info.getSocketSendBuffer(), info.getSocketReceiveBuffer(),
				KEEP_ALIVE, TCP_NO_DELAY, ch, new HttpCall(info, mime, info.getWorkerThread()));
	}

	public void start() {
		nio.startThread();
	}

	public void stop() {
		nio.stopThread();
	}

	public boolean isStop() {
		return nio.isStopThread();
	}

	public boolean isExit() {
		return nio.isExitThread();
	}

	public void exitCall(ExitCall call) {
		this.call = call;
	}

	public ExitCall exitCall() {
		return this.call;
	}
}
