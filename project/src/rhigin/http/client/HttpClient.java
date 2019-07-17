package rhigin.http.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import rhigin.RhiginConstants;
import rhigin.net.ByteArrayIO;
import rhigin.scripts.Json;
import rhigin.util.Converter;

/**
 * HttpClient.
 */
@SuppressWarnings("rawtypes")
public class HttpClient {
    private static final int TIMEOUT = 30000;
    private static final int MAX_RETRY = 9;
    private static final String USER_AGENT = RhiginConstants.NAME;

    protected HttpClient() {
    }

    /**
     * [GET]HttpClient接続.
     * @param url 対象のURLを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public static final HttpResult get(String url) throws IOException {
        return connect("GET", url, null, null);
    }

    /**
     * [GET]HttpClient接続.
     * @param url 対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public static final HttpResult get(String url, Object params)
        throws IOException {
        return connect("GET", url, params, null);
    }

    /**
     * [GET]HttpClient接続.
     * @param url 対象のURLを設定します.
     * @param header 対象のヘッダを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public static final HttpResult get(String url, Map header)
        throws IOException {
        return connect("GET", url, null, header);
    }

    /**
     * [GET]HttpClient接続.
     * @param url 対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @param header 対象のヘッダを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public static final HttpResult get(String url, Object params, Map header)
        throws IOException {
        return connect("GET", url, params, header);
    }

    /**
     * [POST]HttpClient接続.
     * @param url 対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public static final HttpResult post(String url, Object params)
        throws IOException {
        return connect("POST", url, params, null);
    }

    /**
     * [POST]HttpClient接続.
     * @param url 対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @param header 対象のヘッダを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public static final HttpResult post(String url, Object params, Map header)
        throws IOException {
        return connect("POST", url, params, header);
    }

    /**
     * [JSON]HttpClient接続.
     * @param url 対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public static final HttpResult json(String url, Object params)
        throws IOException {
        return connect("JSON", url, params, null);
    }

    /**
     * [JSON]HttpClient接続.
     * @param url 対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @param header 対象のヘッダを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public static final HttpResult json(String url, Object params, Map header)
        throws IOException {
        return connect("JSON", url, params, header);
    }

    /**
     * HttpClient接続.
     * @param method 対象のMethodを設定します.
     * @param url 対象のURLを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public static final HttpResult connect(String method, String url)
        throws IOException {
        return connect(method, url, null, null);
    }

    /**
     * HttpClient接続.
     * @param method 対象のMethodを設定します.
     * @param url 対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public static final HttpResult connect(String method, String url, Object params)
        throws IOException {
        return connect(method, url, params, null);
    }

    /**
     * HttpClient接続.
     * @param method 対象のMethodを設定します.
     * @param url 対象のURLを設定します.
     * @param header 対象のヘッダを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public static final HttpResult connect(String method, String url, Map header)
        throws IOException {
        return connect(method, url, null, header);
    }

    /**
     * HttpClient接続.
     * @param method 対象のMethodを設定します.
     * @param url 対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @param header 対象のヘッダを設定します.
     * @return HttpResult 返却データが返されます.
     */
    @SuppressWarnings("unchecked")
	public static final HttpResult connect(String method, String url, Object params, Map header)
        throws IOException {
        HttpResult ret = null;
        method = method.toUpperCase();
        // methodがJSONの場合は、POSTでJSON送信用の処理に変換する.
        if("JSON".equals(method)) {
            method = "POST";
            params = Json.encode(params);
            header.put("Content-Type", "application/json");
        }
        int cnt = 0;
        while (true) {
            ret = _connect(method, url, params, header);
            int status = ret.getStatus();
            if (!(status == 301 || status == 302 || status == 303
                    || status == 307 || status == 308)) {
                break;
            }
            String location = ret.getHeader("Location");
            if (location == null) {
                break;
            }
            if (status == 301 || status == 302 || status == 303) {
                method = "GET";
                params = null;
            }
            url = location;
            cnt++;
            if (cnt > MAX_RETRY) {
                throw new IOException("リトライ回数の制限を越えました");
            }
        }
        return ret;
    }

    // 接続処理.
    private static final HttpResult _connect(String method, String url, Object params, Map header)
        throws IOException {
        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            // 解析.
            method = method.toUpperCase();
            String[] urlArray = parseUrl(url);
            if (params == null) {
                params = "";
            } else if (params instanceof Map) {
                params = convertParams((Map) params);
            }

            // リクエスト送信.
            socket = createSocket(urlArray);
            out = new BufferedOutputStream(socket.getOutputStream());
            out.write(createHttpRequest(method, urlArray, (String) params,
                    header));
            out.flush();

            // レスポンス受信.
            in = new BufferedInputStream(socket.getInputStream());
            HttpResult ret = receive(url, in);
            in.close();
            in = null;
            out.close();
            out = null;
            socket.close();
            socket = null;

            return ret;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ee) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ee) {
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ee) {
                }
            }
        }
    }

    // URLをパース.
    private static final String[] parseUrl(String url) throws IOException {
        int b = 0;
        int p = url.indexOf("://");
        if (p == -1) {
            return null;
        }
        String protocol = url.substring(0, p);
        String domain = null;
        String path = null;
        String port = "http".equals(protocol) ? "80" : "443";
        b = p + 3;
        p = url.indexOf(":", b);
        int pp = url.indexOf("/", b);
        if (p == -1) {
            if (pp == -1) {
                domain = url.substring(b);
                path = "/";
            } else {
                domain = url.substring(b, pp);
                path = url.substring(pp);
            }
        } else if (pp == -1) {
            domain = url.substring(b, p);
            port = url.substring(p);
            path = "/";
        } else if (p < pp) {
            domain = url.substring(b, p);
            port = url.substring(p, pp);
            path = url.substring(pp);
        } else {
            domain = url.substring(b, p);
            path = url.substring(p);
        }
        if (!Converter.isNumeric(port)) {
            throw new IOException("ポート番号が数字ではありません:" + port);
        }
        return new String[] { protocol, domain, port, path };
    }

    // Mapパラメータを文字列変換.
    private static final String convertParams(Map params) throws IOException {
        if (params == null || params.size() == 0) {
            return "";
        }
        int cnt = 0;
        Object k, v;
        StringBuilder buf = new StringBuilder();
        Iterator it = params.keySet().iterator();
        while (it.hasNext()) {
            k = it.next();
            if (k == null) {
                continue;
            }
            k = k.toString();
            v = params.get(k);
            if (cnt++ != 0)
                buf.append("&");
            buf.append(k).append("=");
            if (v != null && ((String) (v = v.toString())).length() > 0) {
                buf.append(URLEncoder.encode((String) v, "UTF8"));
            }
        }
        return buf.toString();
    }

    // ソケット生成.
    private static final Socket createSocket(String[] urlArray)
        throws IOException {
        return CreateSocket.create("https".equals(urlArray[0]), urlArray[1],
                Integer.parseInt(""+urlArray[2]), TIMEOUT);
    }

    // HTTPリクエストを作成.
    private static final byte[] createHttpRequest(String method, String[] urlArray, String params, Map header)
        throws IOException {
        byte[] b = null;
        String url = urlArray[3];
        if ("GET".equals(method) || "DELETE".equals(method)
                || "OPTIONS".equals(method)) {
            if (params != null && params.length() != 0) {
                url += ((url.indexOf("?") != -1) ? "&" : "?") + params;
                params = "";
            }
        }
        StringBuilder buf = new StringBuilder();
        buf.append(method).append(" ");
        buf.append(url).append(" HTTP/1.1\r\n");
        buf.append("Host: ").append(urlArray[1]);
        if (("http".equals(urlArray[0]) && !"80".equals(urlArray[2]))
                || ("https".equals(urlArray[0]) && !"443".equals(urlArray[2]))) {

            buf.append(":").append(urlArray[2]);
        }
        buf.append("\r\n");
        if (header == null || !header.containsKey("Accept")) {
            buf.append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n");
        }

        if (header == null || !header.containsKey("Accept-Language")) {
            buf.append("Accept-Language: ja,en-US;q=0.7,en;q=0.3\r\n");
        }
        if (header == null || !header.containsKey("User-Agent")) {
            buf.append("User-Agent: ").append(USER_AGENT).append("\r\n");
        }
        buf.append("Accept-Encoding: gzip, deflate\r\n");
        buf.append("Connection: close\r\n");

        // ヘッダユーザ定義.
        if (header != null && header.size() > 0) {

            // 登録できない内容を削除.
            header.remove("Host");
            header.remove("Accept-Encoding");
            header.remove("Connection");
            header.remove("Content-Length");
            header.remove("Transfer-Encoding");

            Object k, v;
            Iterator it = header.keySet().iterator();
            while (it.hasNext()) {
                k = it.next();
                if (k == null) {
                    continue;
                }
                v = header.get(k);
                if (v == null) {
                    continue;
                }
                buf.append(k).append(": ").append(v).append("\r\n");
            }
        }

        if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
            if (header != null && !header.containsKey("Content-Type")) {
                buf.append("Content-Type: ")
                        .append("application/x-www-form-urlencoded")
                        .append("\r\n");
            }
            b = params.getBytes("UTF8");
            buf.append("Content-Length: ").append(b.length).append("\r\n");
        }
        buf.append("\r\n");

        // binary 変換.
        String s = buf.toString();

        buf = null;
        byte[] n = s.getBytes("UTF8");
        s = null;

        if (b == null) {
            return n;
        }

        byte[] ret = new byte[n.length + b.length];
        System.arraycopy(n, 0, ret, 0, n.length);
        System.arraycopy(b, 0, ret, n.length, b.length);
        return ret;
    }

    private static final byte[] CFLF = ("\r\n").getBytes();
    private static final byte[] END_HEADER = ("\r\n\r\n").getBytes();

    // データ受信.
    private static final HttpResult receive(String url, InputStream in)
            throws IOException {
        int len;
        final byte[] binary = new byte[4096];
        ByteArrayIO buffer = new ByteArrayIO();

        int p;
        int bodyLength = 0;
        boolean gzip = false;
        byte[] b = null;
        int status = -1;
        HttpResult result = null;

        // chunked用.
        int chunkedLength = -1;
        ByteArrayIO chunkedBuffer = null;

        try {
            while ((len = in.read(binary)) != -1) {
                buffer.write(binary, 0, len);

                // データ生成が行われていない場合.
                if (result == null) {

                    // ステータス取得が行われていない.
                    if (status == -1) {

                        // ステータスが格納されている１行目の情報を取得.
                        if ((p = buffer.indexOf(CFLF)) != -1) {
                            b = new byte[p + 2];
                            buffer.read(b);
                            String top = new String(b, "UTF8");
                            b = null;
                            status = Integer.parseInt(""+top.substring(9, 12));
                        } else {
                            continue;
                        }
                    }

                    // ヘッダ終端が存在.
                    if ((p = buffer.indexOf(END_HEADER)) != -1) {
                        b = new byte[p + 2];
                        buffer.read(b);
                        buffer.skip(2);
                        result = new HttpResult(url, status, b);
                        b = null;

                        // content-length.
                        String value = result.getHeader("Content-Length");
                        if (Converter.isNumeric(value)) {
                            bodyLength = Integer.parseInt(value);
                        }
                        // chunked.
                        else {
                            value = result.getHeader("Transfer-Encoding");
                            if ("chunked".equals(value)) {
                                bodyLength = -1;
                                chunkedBuffer = new ByteArrayIO();
                            }
                        }

                        // gzip.
                        value = result.getHeader("Content-Encoding");
                        if ("gzip".equals(value)) {
                            gzip = true;
                        }

                    } else {
                        continue;
                    }
                }

                // ヘッダ受信が完了し、Body情報の取得中.
                if (bodyLength == 0) {

                    // 0byteデータ.
                    result.setResponseBody(new byte[0]);
                    return result;
                }
                // bodyサイズが設定されている.
                else if (bodyLength > 0) {

                    if (buffer.size() >= bodyLength) {
                        b = new byte[bodyLength];
                        buffer.read(b);
                        if (gzip) {
                            result.setResponseBody(ungzip(b, binary, buffer));
                        } else {
                            result.setResponseBody(b);
                        }
                        b = null;
                        return result;
                    }

                }
                // chunked受信.
                else if (bodyLength == -1) {
                    boolean breakFlag = false;
                    while (!breakFlag) {

                        // chunkedLengthを取得.
                        if (chunkedLength == -1) {

                            // chunkedLengthデータ長が存在する場合.
                            if ((p = buffer.indexOf(CFLF)) != -1) {
                                chunkedLength = getChunkedLength(p, buffer,
                                        binary);
                            } else {

                                // 次の受信待ち.
                                breakFlag = true;
                                continue;
                            }
                        }

                        // chunkedデータ分受信した場合.
                        if (buffer.size() >= chunkedLength + 2) {

                            // データの終端.
                            if (chunkedLength == 0) {
                                b = chunkedBuffer.toByteArray();
                                if (gzip) {
                                    result.setResponseBody(ungzip(b, binary,
                                            buffer));
                                } else {
                                    result.setResponseBody(b);
                                }
                                b = null;
                                return result;
                            }
                            // chunkedデータが受信されている.
                            else if (chunkedLength > binary.length) {
                                b = new byte[chunkedLength];
                            } else {
                                b = binary;
                            }
                            // データ取得.
                            buffer.read(b, 0, chunkedLength);
                            buffer.skip(2);
                            chunkedBuffer.write(b, 0, chunkedLength);
                            chunkedLength = -1;
                        } else {

                            // 次の受信待ち.
                            breakFlag = true;
                            continue;
                        }
                    }
                }
            }
            return null;
        } finally {
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (Exception e) {
                }
            }
            if (chunkedBuffer != null) {
                try {
                    chunkedBuffer.close();
                } catch (Exception e) {
                }
            }
        }
    }

    // gzip解凍.
    private static final byte[] ungzip(byte[] b, byte[] o, ByteArrayIO io)
            throws IOException {
        int len;
        InputStream in = new GZIPInputStream(new ByteArrayInputStream(b));
        try {
            io.clear();
            while ((len = in.read(o)) != -1) {
                io.write(o, 0, len);
            }
            in.close();
            in = null;
            byte[] ret = io.toByteArray();
            io.clear();
            return ret;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    // chunkedデータ長を取得.
    private static final int getChunkedLength(int p, ByteArrayIO io, byte[] o)
            throws IOException {
        io.read(o, 0, p);
        io.skip(2);
        String s = new String(o, 0, p, "UTF8");
        return Integer.parseInt(s, 16);
    }
}
