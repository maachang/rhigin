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
import rhigin.util.Converter;

public class Rhigin {
	
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
	        if(http.exitCall() != null) {
	            http.exitCall().call();
	        }
	        log.info("end shutdown Rhigin.");
	    }
	}

	// ロガー.
	private static Log LOG = null;

    /** Main. **/
    public static final void main(String[] args) {
        RhiginConfig conf = RhiginStartup.initLogFactory(true, args);
        LOG = LogFactory.create();
        try {
            LOG.info("start rhigin.");
            NioUtil.initNet();
            Rhigin rhigin = new Rhigin();
            rhigin.execute(conf);
        } catch (Throwable e) {
            LOG.error("rhiginError", e);
        } finally {
            LOG.info("exit rhigin.");
        }
    }

    /** http. **/
    protected Http http;

    /** 起動処理. **/
    protected final void execute(RhiginConfig conf) throws Exception {
        // 開始処理.
        HttpInfo httpInfo = RhiginStartup.startup(true, conf);
        
        // 生成されたMimeTypeを取得.
        MimeType mime = (MimeType)ExecuteScript.getOriginal().get("mime");

        // Httpの生成.
        http = new Http(httpInfo, mime);
        
        // 終了コールバック処理をセット.
        http.exitCall(exitCall());

        // HTTP開始.
        http.start();

        // シャットダウンまで待つ処理を生成.
        int shutdownPort = -1;
        if(Converter.isNumeric(conf.get("http", "shutdownPort"))) {
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
