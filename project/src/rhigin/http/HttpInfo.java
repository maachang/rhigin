package rhigin.http;

import java.util.Map;

import rhigin.util.Converter;

/**
 * Httpサーバ設定.
 */
public class HttpInfo {

    /** HTTP同時接続数. **/
    private int backlog = Integer.MAX_VALUE;

    /** Nioバッファ長. **/
    private int byteBufferLength = 1024;

    /** ソケット送信バッファ長. **/
    private int socketSendBuffer = 1024;

    /** ソケット受信バッファ長. **/
    private int socketReceiveBuffer = 2048;

    /** サーバーバインドアドレス. **/
    private String localAddress = null;

    /** サーバーバインドポート. **/
    private int localPort = 3120;

    /** ワーカースレッド数. **/
    private int workerThread = 5;

    /** コンパイルキャッシュサイズ. **/
    private int compileCacheSize = 128;
    
    /** コンパイルキャッシュ基本ディレクトリ. **/
    private String compileCacheBaseDir = ".";

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public int getByteBufferLength() {
        return byteBufferLength;
    }

    public void setByteBufferLength(int byteBufferLength) {
        this.byteBufferLength = byteBufferLength;
    }

    public int getSocketSendBuffer() {
        return socketSendBuffer;
    }

    public void setSocketSendBuffer(int socketSendBuffer) {
        this.socketSendBuffer = socketSendBuffer;
    }

    public int getSocketReceiveBuffer() {
        return socketReceiveBuffer;
    }

    public void setSocketReceiveBuffer(int socketReceiveBuffer) {
        this.socketReceiveBuffer = socketReceiveBuffer;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getWorkerThread() {
        return workerThread;
    }

    public void setWorkerThread(int workerThread) {
        this.workerThread = workerThread;
    }

    public int getCompileCacheSize() {
        return compileCacheSize;
    }

    public void setCompileCacheSize(int compileCacheSize) {
        this.compileCacheSize = compileCacheSize;
    }

    public String getCompileCacheBaseDir() {
        return compileCacheBaseDir;
    }

    public void setCompileCacheBaseDir(String compileCacheBaseDir) {
        this.compileCacheBaseDir = compileCacheBaseDir;
    }

    /**
     * Http設定データを取得.
     * 
     * @param info
     *            データセット先のHttpInfoオブジェクトを設定します.
     * @param conf
     *            対象のコンフィグファイルを設定します.
     */
    public static final void load(HttpInfo info, Map<String,Object> conf) throws Exception {
        if(conf == null) {
            return;
        }
        Object o = null;

        o = conf.get("backlog");
        if (o != null && Converter.isNumeric(o)) {
            info.setBacklog(Converter.convertInt(o));
        }

        o = conf.get("byteBufferLength");
        if (o != null && Converter.isNumeric(o)) {
            info.setByteBufferLength(Converter.convertInt(o));
        }

        o = conf.get("socketSendBuffer");
        if (o != null && Converter.isNumeric(o)) {
            info.setSocketSendBuffer(Converter.convertInt(o));
        }

        o = conf.get("socketReceiveBuffer");
        if (o != null && Converter.isNumeric(o)) {
            info.setSocketReceiveBuffer(Converter.convertInt(o));
        }

        o = conf.get("localAddress");
        if (o != null) {
            info.setLocalAddress(""+ o);
        }

        o = conf.get("localPort");
        if (o != null && Converter.isNumeric(o)) {
            info.setLocalPort(Converter.convertInt(o));
        }

        o = conf.get("workerThread");
        if (o != null && Converter.isNumeric(o)) {
            info.setWorkerThread(Converter.convertInt(o));
        }

        o = conf.get("compileCacheSize");
        if (o != null && Converter.isNumeric(o)) {
            info.setCompileCacheSize(Converter.convertInt(o));
        }

        o = conf.get("compileCacheBaseDir");
        if (o != null) {
            info.setCompileCacheBaseDir(""+ o);
        }
    }
}
