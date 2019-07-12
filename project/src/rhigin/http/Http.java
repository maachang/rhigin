package rhigin.http;

import java.nio.channels.ServerSocketChannel;

import rhigin.net.NioCore;
import rhigin.net.NioUtil;
import rhigin.scripts.compile.CompileCache;
import rhigin.scripts.function.RequireFunction;

/**
 * Httpオブジェクト.
 */
public final class Http {
    // net設定.
    protected static final boolean TCP_NO_DELAY = true; // Nagle アルゴリズムを有効にします.
    protected static final boolean KEEP_ALIVE = false; // TCP-KeepAliveを無効に設定します.

    /** Nio処理. **/
    private NioCore nio = null;
    
    /** Callback. **/
    private ExitCall call = null;

    /**
     * コンストラクタ.
     * @param info
     * @param mime
     * @throws Exception
     */
    public Http(HttpInfo info, MimeType mime) throws Exception {

        // nio:サーバーソケット作成.
        ServerSocketChannel ch = NioUtil.createServerSocketChannel(
            info.getSocketReceiveBuffer(), info.getLocalAddress(),
            info.getLocalPort(), info.getBacklog());
        
        // コンパイルキャッシュ生成.
        CompileCache cache = new CompileCache(info.getCompileCacheSize(), info.getCompileCacheBaseDir());
        
        // コンパイルキャッシュを require命令に設定.
        RequireFunction.getInstance().setCache(cache);
        
        // nio処理を生成.
        this.nio = new NioCore(info.getByteBufferLength(),
            info.getSocketSendBuffer(), info.getSocketReceiveBuffer(),
            KEEP_ALIVE, TCP_NO_DELAY, ch, new HttpCall(
                cache, mime, info.getWorkerThread()));
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
