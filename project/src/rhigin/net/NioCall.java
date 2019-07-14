package rhigin.net;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Nioコールバック.
 */
public abstract class NioCall {
  /**
   * 新しい通信要素を生成.
   * @return BaseNioElement 新しい通信要素が返却されます.
   */
  public abstract NioElement createElement();

  /**
   * nio開始処理.
   * @return boolean [true]の場合、正常に処理されました.
   */
  public boolean startNio() {
    return true;
  }

  /**
   * nio終了処理.
   */
  public void endNio() {
  }

  /**
   * エラーハンドリング.
   */
  public void error(Throwable e) {
  }

  /**
   * Accept処理.
   * @param em 対象のBaseNioElementオブジェクトが設定されます.
   * @return boolean [true]の場合、正常に処理されました.
   * @exception IOException IO例外.
   */
  public boolean accept(NioElement em) throws IOException {
    return true;
  }

  /**
   * Send処理.
   * @param em 対象のBaseNioElementオブジェクトが設定されます.
   * @param buf 対象のByteBufferを設定します.
   * @return boolean [true]の場合、正常に処理されました.
   * @exception IOException IO例外.
   */
  public boolean send(NioElement em, ByteBuffer buf) throws IOException {
    return sendInputStream(em, buf);
  }

  /**
   * SendData内容を送信する.
   */
  @SuppressWarnings("resource")
  protected boolean sendInputStream(NioElement em, ByteBuffer buf) throws IOException {
    InputStream in = em.getSendData();
    byte[] sendTempBinary = em.getSendTempBinary(buf.capacity());
    while(true) {
      // FileChannelで処理できる場合.
      if(in instanceof FileInputStream) {
        // dataBinaryを使わず、直接FileにDirectByteBufferの読み込みを行う.
        FileChannel ch = ((FileInputStream)in).getChannel();
        // データ終端.
        if(ch.read(buf) == -1) {
          // 現在の inputStream を破棄.
          InputStream endInputStream = em.removeSendData();
          if(endInputStream != null) {
            try {
              endInputStream.close();
            } catch(Exception e) {}
          }
          // バッファのデータ設定先に空きがありデータが設定可能な場合.
          if(buf.position() != buf.limit()) {
            in = em.getSendData();
            if(in != null) {
              continue;
            }
          }
          // 送信データが存在しない.
          if (buf.position() == 0) {
            // 処理終了.
            return false;
          }
        }
      // 通常のInputStreamで処理する場合.
      } else {
        // 一旦バイナリデータにセット.
        int len = buf.limit() - buf.position();
        len = in.read(sendTempBinary, 0, len);
        if(len == -1) {
          // 現在の inputStream を破棄.
          InputStream endInputStream = em.removeSendData();
          if(endInputStream != null) {
            try {
              endInputStream.close();
            } catch(Exception e) {}
          }
          // バッファのデータ設定先に空きがありデータが設定可能な場合.
          if(buf.position() != buf.limit()) {
            in = em.getSendData();
            if(in != null) {
              continue;
            }
          }
          // 送信データが存在しない.
          if (buf.position() == 0) {
            // 処理終了.
            return false;
          }
        } else {
          // 送信データをセット.
          buf.put(sendTempBinary, 0, len);
        }
      }
      return true;
    }
  }

  /**
   * Receive処理.
   * @param em 対象のBaseNioElementオブジェクトが設定されます.
   * @param buf 対象のByteBufferを設定します.
   * @return boolean [true]の場合、正常に処理されました.
   * @exception IOException IO例外.
   */
  public boolean receive(NioElement em, ByteBuffer buf) throws IOException {
    return true;
  }
}
