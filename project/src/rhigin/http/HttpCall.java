package rhigin.http;

import java.io.IOException;
import java.nio.ByteBuffer;

import rhigin.logs.Log;
import rhigin.logs.LogFactory;
import rhigin.net.NioCall;
import rhigin.net.NioElement;
import rhigin.scripts.compile.CompileCache;
import rhigin.util.AtomicNumber;

/**
 * Httpコール処理.
 */
public final class HttpCall extends NioCall {
    private static final Log LOG = LogFactory.create();
    private int workerLength = -1;
    private MimeType mime = null;
    private CompileCache compileCache = null;
    private HttpWorkerThread[] worker = null;
    private final AtomicNumber counter = new AtomicNumber(0);

    /**
     * コンストラクタ.
     * 
     * @param compileManage
     *            コンパイルマネージャを設定します.
     * @param mime
     *            MimeTypeオブジェクトを設定します.
     * @param wprkerLength
     *            ワーカースレッド長を設定します.
     */
    public HttpCall(CompileCache compileCache, MimeType mime, int workerLength) {
        this.compileCache = compileCache;
        this.workerLength = workerLength;
        this.mime = mime;
    }

    /**
     * 新しい通信要素を生成.
     * 
     * @return BaseNioElement 新しい通信要素が返却されます.
     */
    public NioElement createElement() {
        return new HttpElement();
    }

    /**
     * 開始処理.
     * 
     * @return boolean [true]の場合、正常に処理されました.
     */
    public boolean startNio() {
        LOG.info(" start Http nio");

        // ワーカースレッドを生成.
        HttpWorkerThread[] w = new HttpWorkerThread[workerLength];
        for (int i = 0; i < workerLength; i++) {
            w[i] = new HttpWorkerThread(compileCache, mime, i);
            w[i].startThread();
        }
        worker = w;
        return true;
    }

    /**
     * 終了処理.
     */
    public void endNio() {
        LOG.info(" stop Http nio");

        // ワーカースレッドを破棄.
        HttpWorkerThread[] w = worker;
        worker = null;
        for (int i = 0; i < workerLength; i++) {
            w[i].stopThread();
        }

        // ワーカースレッド停止待ち.
        boolean allEndFlag = false;
        while (!allEndFlag) {
            allEndFlag = true;
            for (int i = 0; i < workerLength; i++) {
                if (!w[i].isEndThread()) {
                    allEndFlag = false;
                    break;
                }
            }
            if (!allEndFlag) {
                try {
                    Thread.sleep(5);
                } catch (Exception e) {
                }
            }
        }

        LOG.info(" exit Http nio");
    }

    /**
     * エラーハンドリング.
     */
    public void error(Throwable e) {
        LOG.error(" error Http nio", e);
    }

    /**
     * Accept処理.
     * 
     * @param em
     *            対象のBaseNioElementオブジェクトが設定されます.
     * @return boolean [true]の場合、正常に処理されました.
     * @exception IOException
     *                IO例外.
     */
    public boolean accept(NioElement em) throws IOException {
        //LOG.debug(" accept Http nio");
        return true;
    }

    /**
     * Receive処理.
     * 
     * @param em
     *            対象のBaseNioElementオブジェクトが設定されます.
     * @param buf
     *            対象のByteBufferを設定します.
     * @return boolean [true]の場合、正常に処理されました.
     * @exception IOException
     *                IO例外.
     */
    public boolean receive(NioElement em, ByteBuffer buf) throws IOException {
        //LOG.debug(" recv Http nio:" + buf);

        HttpElement rem = (HttpElement) em;

        // 受信バッファに今回分の情報をセット.
        rem.getBuffer().write(buf);

        int no = rem.getWorkerNo();
        if (no == -1) {
            no = counter.inc() % workerLength;
            counter.set(no);
        }
        worker[no].register(rem);
        return true;
    }

}

