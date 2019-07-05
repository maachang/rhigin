package rhigin.http;

import java.io.IOException;
import java.util.Map;

import rhigin.net.ByteArrayIO;
import rhigin.util.ArrayMap;
import rhigin.util.Converter;

/**
 * Httpリクエスト解析.
 */
public final class Analysis {
    public static final byte[] ONE_LINE = "\r\n".getBytes();
    public static final byte[] END_LINE = "\r\n\r\n".getBytes();
    public static final int ONE_LINE_LENGTH = ONE_LINE.length;
    public static final int END_LINE_LENGTH = END_LINE.length;

    /**
     * Httpヘッダの終端が存在するかチェック.
     * @param buffer
     */
    public static final int endPoint(ByteArrayIO buffer) {
      return buffer.indexOf(END_LINE);
    }

    /**
     * HttpHeaderオブジェクトの生成.
     * @param buffer
     * @param endPoint
     * @return HttpHeader
     * @exception IOException
     */
    public static final Header getHeader(ByteArrayIO buffer, int endPoint)
      throws IOException {
      return new Header(buffer, endPoint);
    }

    /**
     * HttpRequestオブジェクトの生成.
     * @param buffer
     * @param endPoint
     * @return HttpRequest
     * @exception IOException
     */
    public static final Request getRequest(ByteArrayIO buffer, int endPoint)
      throws IOException {
      return new Request(buffer, endPoint);
    }

    /**
     * パラメータ変換処理. POSTのデータおよび、GETのデータを解析します.
     * @param body 対象のBody情報を設定します.
     * @param cset 対象のキャラクタセットを設定します.
     * @param pos 対象のポジションを設定します.
     * @return Map<String,String> 変換結果を返却します.
     * @exception IOException IO例外.
     */
    public static final Map<String, Object> paramsAnalysis(String body, int pos)
      throws IOException {
      return paramsAnalysis(body, "UTF8", pos);
    }

    /**
     * パラメータ変換処理. POSTのデータおよび、GETのデータを解析します.
     * @param body 対象のBody情報を設定します.
     * @param cset 対象のキャラクタセットを設定します.
     * @param pos 対象のポジションを設定します.
     * @return ListMap 変換結果を返却します.
     * @exception IOException IO例外.
     */
    public static final Map<String, Object> paramsAnalysis(String body, String cset, int pos)
      throws IOException {
      // パラメータバイナリを解析.
      int p, n;
      String k;
      int b = pos;
      Map<String, Object> ret = new ArrayMap();
      while (true) {
        if ((n = body.indexOf("&", b)) == -1) {
          k = body.substring(b);
          if ((p = k.indexOf("=")) == -1) {
            break;
          }
          if (k.indexOf("%") != -1) {
            ret.put(Converter.urlDecode(k.substring(0, p), cset),
                    Converter.urlDecode(k.substring(p + 1), cset));
          } else {
            ret.put(k.substring(0, p), k.substring(p + 1));
          }
          break;
        }
        k = body.substring(b, n);
        if ((p = k.indexOf("=")) == -1) {
          b = n + 1;
          continue;
        }
        if (k.indexOf("%") != -1) {
          ret.put(Converter.urlDecode(k.substring(0, p), cset),
                  Converter.urlDecode(k.substring(p + 1), cset));
        } else {
          ret.put(k.substring(0, p), k.substring(p + 1));
        }
        b = n + 1;
      }
      return ret;
    }
}
