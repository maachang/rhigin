package rhigin.http.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import objectpack.ObjectPack;
import objectpack.SerializableCore;
import rhigin.http.MimeType;
import rhigin.keys.RhiginAccessKeyByFCipher;
import rhigin.keys.RhiginAccessKeyClient;
import rhigin.keys.RhiginAccessKeyConstants;
import rhigin.keys.RhiginAccessKeyUtil;
import rhigin.scripts.Json;
import rhigin.scripts.ObjectPackOriginCode;
import rhigin.util.ArrayMap;
import rhigin.util.ByteArrayIO;
import rhigin.util.Converter;
import rhigin.util.FCipher;

/**
 * HttpClient.
 */
@SuppressWarnings("rawtypes")
public class HttpClient {
	// ObjectPackのRhigin拡張.
	static {
		if(!SerializableCore.isOriginCode()) {
			SerializableCore.setOriginCode(new ObjectPackOriginCode());
		}
	}
	private static final int MAX_BINARY_BODY_LENGTH = 0x00100000 * 5; // 5Mbyte.
	private static final int TIMEOUT = 30000;
	private static final int MAX_RETRY = 9;
	private static final String DEF_USER_AGENT = "rhigin";
	private static final String DEF_MIN_USER_AGENT = "rhigin_m";
	private static final String BLOWSER_ACCESS_HEADER = "X-Blowser";

	protected HttpClient() {
	}

	/**
	 * [GET]HttpClient接続.
	 * 
	 * @param url
	 *            対象のURLを設定します.
	 * @param params GET送信対象のパラメータを設定します.
	 * @param option 対象のオプションを設定します.
	 *               header: 追加のHTTPヘッダ情報を設定する場合は、この名前でMapで設定します.
	 *               bodyFile: HTTPレスポンスのデータをファイルで格納させたい場合は[true]を設定します.
	 *               minHeader: rhiginサーバに最小のヘッダで通信をする場合は true を設定します.
	 *               blowser: rhiginサーバにアクセスする場合、falseをセットすることで、HTTPレスポンスヘッダ
	 *                        の量を少し減らせます.
	 *               accessKey: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 *               authCode: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 * @return HttpResult 返却データが返されます.
	 */
	@SuppressWarnings("unchecked")
	public static final HttpResult get(String url, Object params, Map option) {
		option.put("params", params);
		return get(url, option);
	}
	
	/**
	 * [GET]HttpClient接続.
	 * 
	 * @param url
	 *            対象のURLを設定します.
	 * @oaram option 対象のオプションを設定します.
	 *               params: パラメータを設定する場合は、この名前で設定します.
	 *               header: 追加のHTTPヘッダ情報を設定する場合は、この名前でMapで設定します.
	 *               bodyFile: HTTPレスポンスのデータをファイルで格納させたい場合は[true]を設定します.
	 *               minHeader: rhiginサーバに最小のヘッダで通信をする場合は true を設定します.
	 *               blowser: rhiginサーバにアクセスする場合、falseをセットすることで、HTTPレスポンスヘッダ
	 *                        の量を少し減らせます.
	 *               accessKey: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 *               authCode: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 * @return HttpResult 返却データが返されます.
	 */
	public static final HttpResult get(String url, Map option) {
		return connect("GET", url, option);
	}

	/**
	 * [POST]HttpClient接続.
	 * 
	 * @param url
	 *            対象のURLを設定します.
	 * @param params POST送信するパラメータを設定します.
	 * @oaram option 対象のオプションを設定します.
	 *               header: 追加のHTTPヘッダ情報を設定する場合は、この名前でMapで設定します.
	 *               bodyFile: HTTPレスポンスのデータをファイルで格納させたい場合は[true]を設定します.
	 *               minHeader: rhiginサーバに最小のヘッダで通信をする場合は true を設定します.
	 *               blowser: rhiginサーバにアクセスする場合、falseをセットすることで、HTTPレスポンスヘッダ
	 *                        の量を少し減らせます.
	 *               accessKey: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 *               authCode: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 * @return HttpResult 返却データが返されます.
	 */
	@SuppressWarnings("unchecked")
	public static final HttpResult post(String url, Object params, Map option) {
		option.put("params", params);
		return post(url, option);
	}
	
	/**
	 * [POST]HttpClient接続.
	 * 
	 * @param url
	 *            対象のURLを設定します.
	 * @oaram option 対象のオプションを設定します.
	 *               params: パラメータを設定する場合は、この名前で設定します.
	 *               header: 追加のHTTPヘッダ情報を設定する場合は、この名前でMapで設定します.
	 *               bodyFile: HTTPレスポンスのデータをファイルで格納させたい場合は[true]を設定します.
	 *               minHeader: rhiginサーバに最小のヘッダで通信をする場合は true を設定します.
	 *               blowser: rhiginサーバにアクセスする場合、falseをセットすることで、HTTPレスポンスヘッダ
	 *                        の量を少し減らせます.
	 *               accessKey: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 *               authCode: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 * @return HttpResult 返却データが返されます.
	 */
	public static final HttpResult post(String url, Map option) {
		return connect("POST", url, option);
	}

	/**
	 * [JSON]HttpClient接続.
	 * 
	 * @param url
	 *            対象のURLを設定します.
	 * @param json 送信対象のJSONオブジェクトを設定します.
	 * @oaram option 対象のオプションを設定します.
	 *               header: 追加のHTTPヘッダ情報を設定する場合は、この名前でMapで設定します.
	 *               bodyFile: HTTPレスポンスのデータをファイルで格納させたい場合は[true]を設定します.
	 *               minHeader: rhiginサーバに最小のヘッダで通信をする場合は true を設定します.
	 *               blowser: rhiginサーバにアクセスする場合、falseをセットすることで、HTTPレスポンスヘッダ
	 *                        の量を少し減らせます.
	 *               accessKey: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 *               authCode: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 * @return HttpResult 返却データが返されます.
	 */
	@SuppressWarnings("unchecked")
	public static final HttpResult json(String url, Object json, Map option) {
		option.put("params", Json.encode(json));
		return json(url, option);

	}
	
	/**
	 * [JSON]HttpClient接続.
	 * 
	 * @param url
	 *            対象のURLを設定します.
	 * @oaram option 対象のオプションを設定します.
	 *               params: パラメータを設定する場合は、この名前で設定します.
	 *               header: 追加のHTTPヘッダ情報を設定する場合は、この名前でMapで設定します.
	 *               bodyFile: HTTPレスポンスのデータをファイルで格納させたい場合は[true]を設定します.
	 *               minHeader: rhiginサーバに最小のヘッダで通信をする場合は true を設定します.
	 *               blowser: rhiginサーバにアクセスする場合、falseをセットすることで、HTTPレスポンスヘッダ
	 *                        の量を少し減らせます.
	 *               accessKey: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 *               authCode: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 * @return HttpResult 返却データが返されます.
	 */
	public static final HttpResult json(String url, Map option) {
		return connect("JSON", url, option);
	}

	/**
	 * HttpClient接続.
	 * 
	 * @param method
	 *            対象のMethodを設定します.
	 * @param url
	 *            対象のURLを設定します.
	 * @oaram option 対象のオプションを設定します.
	 *               params: パラメータを設定する場合は、この名前で設定します.
	 *               header: 追加のHTTPヘッダ情報を設定する場合は、この名前でMapで設定します.
	 *               bodyFile: HTTPレスポンスのデータをファイルで格納させたい場合は true を設定します.
	 *               minHeader: rhiginサーバに最小のヘッダで通信をする場合は true を設定します.
	 *               blowser: rhiginサーバにアクセスする場合、falseをセットすることで、HTTPレスポンスヘッダ
	 *                        のAjaxドメイン超え関連のHttpヘッダを減らせます.
	 *               accessKey: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 *               authCode: アクセスキーを用いたHttp or Https通信を行う場合に利用します.
	 *               accessKeyClient: RhiginAccessKeyClientで管理されているAccessKeyを利用する場合は true を設定します.
	 * @return HttpResult 返却データが返されます.
	 */
	@SuppressWarnings("unchecked")
	public static final HttpResult connect(String method, String url, Map option) {
		Object params = null;
		Map header = null;
		boolean bodyFile = false;
		boolean minHeader = false;
		boolean blowser = true;
		String accessKey = null;
		String authCode = null;
		if (option != null) {
			// パラメータ定義.
			params = option.get("params");
			if(params == null) {
				params = option.get("param");
			}
			// header定義.
			Object h = option.get("header");
			if(h == null) {
				h = option.get("headers");
			}
			if(h != null && h instanceof Map) {
				header = (Map)h;
			}
			h = null;
			// 外部ファイルにResponse情報を保持.
			bodyFile = Boolean.TRUE.equals(option.get("bodyFile"));
			if(!bodyFile) {
				bodyFile = Boolean.TRUE.equals(option.get("body"));
			}
			// 最小ヘッダ、ObjectPackageを使って送受信(rhiginサーバのみ)
			minHeader = Boolean.TRUE.equals(option.get("minHeader"));
			if(!minHeader) {
				minHeader = Boolean.TRUE.equals(option.get("min"));
			}
			// ブラウザじゃないアクセス定義(rhiginサーバのみ)
			blowser = Boolean.TRUE.equals(option.get("blowser"));
			// accessKey, authCode での通信(rhiginサーバのみ)
			Object akey = null;
			Object acode = null;
			akey = option.get("accessKey");
			if(akey == null) {
				akey = option.get("akey");
			}
			acode = (String) option.get("authCode");
			if(acode == null) {
				acode = option.get("acode");
			}
			// アクセスキーが存在する場合.
			if(akey != null && acode != null && akey instanceof String && acode instanceof String) {
				accessKey = (String)akey;
				authCode = (String)acode;
				akey = null; acode = null;
				if(!RhiginAccessKeyUtil.isAccessKey(accessKey)
					|| !RhiginAccessKeyUtil.isAuthCode(authCode)) {
					throw new HttpClientException("The specified accessKey or authCode are invalid codes.");
				}
			// RhiginAccessKeyClientで管理されている定義を利用する場合.
			} else if(Boolean.TRUE.equals(option.get("accessKeyClient")) || Boolean.TRUE.equals(option.get("akClient"))) {
				RhiginAccessKeyClient ac = RhiginAccessKeyClient.getInstance();
				String[] keys = ac.get(url);
				if(keys == null) {
					throw new HttpClientException("The specified accessKey or authCode are invalid codes.");
				}
				accessKey = keys[0];
				authCode = keys[1];
			}
		}
		// bodyファイル要求に対して組み合わせの設定ができないものをエラー判別
		if(bodyFile) {
			if(minHeader) {
				throw new HttpClientException("Minimum header mode cannot be used when receiving Body file.");
			} else if(accessKey != null) {
				throw new HttpClientException("If you receive a body file, you cannot access it with accessKey.");
			}
		}
		// headerが存在しない場合は空の情報を生成.
		if (header == null) {
			header = new ArrayMap();
		}
		HttpResult ret = null;
		// methodがJSONの場合は、POSTでJSON送信用の処理に変換する.
		if ("JSON".equals(method = method.toUpperCase())) {
			method = "POST";
			params = Json.encode(params);
			header.put("Content-Type", "application/json;charset=utf-8");
		}
		int status;
		String location;
		int cnt = 0;
		while (true) {
			ret = _connect(minHeader, blowser, bodyFile, accessKey, authCode, method, url, params, header);
			if (!((status = ret.getStatus()) == 301 || status == 302 || status == 303 || status == 307 || status == 308) ||
				(location = ret.getHeader("location")) == null) {
				break;
			} else if (status == 301 || status == 302 || status == 303) {
				method = "GET";
				params = null;
			}
			url = location;
			cnt++;
			if (cnt > MAX_RETRY) {
				throw new HttpClientException(500, "Retry limit exceeded.");
			}
		}
		return ret;
	}

	// 接続処理.
	private static final HttpResult _connect(boolean minHeader, boolean blowser, boolean bodyFile, String akey, String acode,
			String method, String url, Object params, Map header) {
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
			createHttpRequest(minHeader, blowser, akey, acode, out, method, urlArray, params, header);
			out.flush();
			// レスポンス受信.
			in = new BufferedInputStream(socket.getInputStream());
			HttpResult ret = receive(bodyFile, url, akey, acode, in);
			in.close();
			in = null;
			out.close();
			out = null;
			socket.close();
			socket = null;
			return ret;
		} catch(Exception e) {
			throw new HttpClientException(500, e);
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
			port = url.substring(p + 1);
			path = "/";
		} else if (p < pp) {
			domain = url.substring(b, p);
			port = url.substring(p + 1, pp);
			path = url.substring(pp);
		} else {
			domain = url.substring(b, p);
			path = url.substring(p);
		}
		if (!Converter.isNumeric(port)) {
			throw new IOException("Port number is not a number: " + port);
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
	private static final Socket createSocket(String[] urlArray) throws IOException {
		return CreateSocket.create("https".equals(urlArray[0]), urlArray[1], Integer.parseInt("" + urlArray[2]),
				TIMEOUT);
	}

	// HTTPリクエストを作成.
	private static final void createHttpRequest(boolean minHeader, boolean blowser,
			String akey, String acode, OutputStream out, String method,
			String[] urlArray, Object params, Map header)
		throws IOException {
		byte[] b = null;
		boolean contentTypeFlag = false;
		String url = urlArray[3];
		if (NoULCode.eqs(method, "get", "delete", "options") != -1) {
			if (params instanceof byte[]) {
				if (((byte[]) params).length > 0) {
					params = new String((byte[]) params, "UTF8");
				} else {
					params = "";
				}
			}
			if (params instanceof String && ((String) params).length() != 0) {
				// アクセスキーが設定されている場合は、アクセスキーと認証コードで文字列エンコード.
				if(akey != null) {
					params = RhiginAccessKeyByFCipher.encode(akey, acode, ((String)params).getBytes("UTF8"));
					params = "params=" + FCipher.cb64_enc((byte[])params);
				}
				url += ((url.indexOf("?") != -1) ? "&" : "?") + params;
				params = "";
			}
		} else if(NoULCode.eqs(method, "post") != -1) {
			// アクセスキーが設定されている場合は、アクセスキーと認証コードでバイナリエンコード.
			if(akey != null) {
				// 文字列の場合.
				if (params instanceof String) {
					params = RhiginAccessKeyByFCipher.encode(akey, acode, ((String)params).getBytes("UTF8"));
				// バイナリの場合.
				} else if (params instanceof byte[]) {
					params = RhiginAccessKeyByFCipher.encode(akey, acode, (byte[])params);
				// InputStreamの場合は処理出来ない.
				} else if (params instanceof InputStream) {
					throw new HttpClientException("InputStream transmission with AccessKey cannot be performed for POST transmission.");
				}
			}
		}
		StringBuilder buf = new StringBuilder();
		buf.append(method.toUpperCase()).append(" ");
		buf.append(url).append(" HTTP/1.1\r\n");
		// 最小限ヘッダの場合.
		if(minHeader) {
			if (header == null || !header.containsKey("User-Agent")) {
				buf.append("User-Agent:").append(DEF_MIN_USER_AGENT).append("\r\n");
			}
			buf.append("Accept-Encoding:gzip\r\n");
		// 最小限ヘッダでない場合.
		} else {
			buf.append("Host:").append(urlArray[1]);
			if (("http".equals(urlArray[0]) && !"80".equals(urlArray[2]))
					|| ("https".equals(urlArray[0]) && !"443".equals(urlArray[2]))) {

				buf.append(":").append(urlArray[2]);
			}
			buf.append("\r\n");
			// ブラウザでのアクセスヘッダのI/Oを除外したい場合.
			if(!blowser) {
				buf.append(BLOWSER_ACCESS_HEADER).append(":false\r\n");
			// ブラウザでのアクセスヘッダと同等の条件をセット.
			} else {
				if (header == null || !header.containsKey("Accept")) {
					buf.append("Accept:text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n");
				}
		
				if (header == null || !header.containsKey("Accept-Language")) {
					buf.append("Accept-Language:ja,en-US;q=0.7,en;q=0.3\r\n");
				}
			}
			if (header == null || !header.containsKey("User-Agent")) {
				buf.append("User-Agent:").append(DEF_USER_AGENT).append("\r\n");
			}
			buf.append("Accept-Encoding:gzip,deflate\r\n");
		}
		buf.append("Connection:close\r\n");
		
		// accessKeyが設定されている場合.
		if(akey != null) {
			// アクセスキーをセット.
			buf.append(RhiginAccessKeyConstants.RHIGIN_ACCESSKEY_HTTP_HEADER)
				.append(":").append(akey).append("\r\n");
		}
		
		// ヘッダユーザ定義.
		if (header != null && header.size() > 0) {
			// ヘッダにアクセスキーが設定されている場合は削除.
			if(header.containsKey(RhiginAccessKeyConstants.RHIGIN_ACCESSKEY_HTTP_HEADER)) {
				header.remove(RhiginAccessKeyConstants.RHIGIN_ACCESSKEY_HTTP_HEADER);
			}
			String h;
			Object k, v;
			Iterator it = header.keySet().iterator();
			// 最小限ヘッダでない場合.
			if(!minHeader) {
				while (it.hasNext()) {
					// 登録できない内容を削除.
					if ((k = it.next()) == null ||
						NoULCode.eqs((h = "" + k),
							"host", "accept-encoding", "connection", "content-length", "transfer-encoding") != -1 ||
						(v = header.get(k)) == null) {
						continue;
					} else if(NoULCode.eqs(h, "content-type") != -1) {
						contentTypeFlag = true;
					}
					buf.append(h).append(":").append(v).append("\r\n");
				}
			// 最小限ヘッダの場合.
			} else {
				int no;
				while (it.hasNext()) {
					// 登録できない内容を削除.
					if ((k = it.next()) == null || (v = header.get(k)) == null) {
						continue;
					} else if((no = NoULCode.eqs((h = "" + k),
						"content-type", "user-agent")) != -1) {
						if(no == 0) {
							contentTypeFlag = true;
						}
						buf.append(h).append(":").append(v).append("\r\n");
					}
				}
			}
		}
		// post系の場合.
		boolean chunked = false;
		if (NoULCode.eqs(method, "post", "put", "patch") != -1) {
			if (!contentTypeFlag) {
				buf.append("Content-Type:").append("application/x-www-form-urlencoded").append("\r\n");
			}
			if (params instanceof String) {
				String pms = (String) params;
				if (pms.length() > 0) {
					b = ((String) params).getBytes("UTF8");
					buf.append("Content-Length:").append(b.length).append("\r\n");
				} else {
					b = null;
					buf.append("Content-Length:0\r\n");
				}
			} else if (params instanceof byte[]) {
				b = (byte[]) params;
				if (b.length > 0) {
					buf.append("Content-Length:").append(b.length).append("\r\n");
				} else {
					b = null;
					buf.append("Content-Length:0\r\n");
				}
			} else if (params instanceof InputStream) {
				if (params instanceof FileInputStream) {
					buf.append("Content-Length:").append(((InputStream) params).available()).append("\r\n");
				} else {
					buf.append("Transfer-Encoding:chunked\r\n");
					chunked = true;
				}
			}
		}
		// ヘッダ終端.
		buf.append("\r\n");

		// binary 変換.
		String s = buf.toString();
		buf = null;
		byte[] h = s.getBytes("UTF8");
		s = null;

		// ヘッダ出力.
		out.write(h);
		h = null;

		// バイナリ出力.
		if (b != null) {
			// body出力.
			out.write(b);
			return;
		}

		// パラメータがinputStreamの場合.
		if (params instanceof InputStream) {
			int len;
			InputStream in = (InputStream) params;

			// 送信タイプがchunkedの場合.
			if (chunked) {
				final byte[] head = new byte[3];
				b = new byte[1024 - 7];
				while ((len = in.read(b)) != -1) {
					chunkedWrite(head, out, len);
					out.write(CFLF, 0, 2);
					out.write(b, 0, len);
					out.write(CFLF, 0, 2);
				}
				chunkedWrite(head, out, 0);
				out.write(END_HEADER, 0, 4);
				// 送信タイプがContent-Lengthの場合.
			} else {
				b = new byte[1024];
				while ((len = in.read(b)) != -1) {
					out.write(b, 0, len);
				}
			}
			b = null;
			out.flush();
		}
	}

	// chunked出力.
	private static final void chunkedWrite(byte[] head, OutputStream out, int len) throws IOException {
		int bufLen = 0;
		while (true) {
			switch (len & 0x0f) {
			case 0:
				head[bufLen++] = (byte) ('0');
				break;
			case 1:
				head[bufLen++] = (byte) ('1');
				break;
			case 2:
				head[bufLen++] = (byte) ('2');
				break;
			case 3:
				head[bufLen++] = (byte) ('3');
				break;
			case 4:
				head[bufLen++] = (byte) ('4');
				break;
			case 5:
				head[bufLen++] = (byte) ('5');
				break;
			case 6:
				head[bufLen++] = (byte) ('6');
				break;
			case 7:
				head[bufLen++] = (byte) ('7');
				break;
			case 8:
				head[bufLen++] = (byte) ('8');
				break;
			case 9:
				head[bufLen++] = (byte) ('9');
				break;
			case 10:
				head[bufLen++] = (byte) ('a');
				break;
			case 11:
				head[bufLen++] = (byte) ('b');
				break;
			case 12:
				head[bufLen++] = (byte) ('c');
				break;
			case 13:
				head[bufLen++] = (byte) ('d');
				break;
			case 14:
				head[bufLen++] = (byte) ('e');
				break;
			case 15:
				head[bufLen++] = (byte) ('f');
				break;
			}
			if ((len = len >> 4) == 0) {
				break;
			}
		}
		out.write(head, 0, bufLen);
	}

	private static final byte[] CFLF = ("\r\n").getBytes();
	private static final byte[] END_HEADER = ("\r\n\r\n").getBytes();

	// データ受信.
	private static final HttpResult receive(boolean bodyFileFlg, String url, String akey, String acode, InputStream in)
		throws IOException {
		int len;
		final byte[] binary = new byte[4096];
		ByteArrayIO buffer = new ByteArrayIO();
		int p;
		int bodyLength = 0;
		boolean gzip = false;
		byte[] b = null;
		int status = -1;
		String message = "";
		HttpBodyFile bodyFile = null;
		HttpResult result = null;
		// chunked用.
		int chunkedLength = -1;
		ByteArrayIO chunkedBuffer = null;
		try {
			while ((len = in.read(binary)) != -1) {
				// Content-Lengthが設定されていて、bodyFileが有効な場合.
				if (bodyLength != -1 && bodyFile != null) {
					bodyFile.write(binary, len);
				} else {
					buffer.write(binary, 0, len);
				}
				// データ生成が行われていない場合.
				if (result == null) {
					// ステータス取得が行われていない.
					if (status == -1) {
						// ステータスが格納されている１行目の情報を取得.
						if ((p = buffer.indexOf(CFLF)) != -1) {
							// HTTP/1.1 {Status} {MESSAGE}\r\n
							int pp, ppp;
							b = new byte[p + 2];
							buffer.read(b);
							String top = new String(b, "UTF8");
							b = null;
							pp = top.indexOf(" ");
							if(pp == -1) {
								ppp = -1;
							} else {
								ppp = top.indexOf(" ", pp + 1);
							}
							if(pp == -1|| ppp == -1) {
								status = 200;
								message = "OK";
							} else {
								status = Integer.parseInt(top.substring(pp + 1, ppp));
								message = top.substring(ppp + 1).trim();
							}
						} else {
							continue;
						}
					}
					// ヘッダ終端が存在.
					if ((p = buffer.indexOf(END_HEADER)) != -1) {
						b = new byte[p + 2];
						buffer.read(b);
						buffer.skip(2);
						result = new HttpResult(url, status, message, b);
						b = null;
						// content-length.
						String value = result.getHeader("content-length");
						if (Converter.isNumeric(value)) {
							bodyLength = Integer.parseInt(value);
						}
						// chunked.
						else {
							value = result.getHeader("transfer-encoding");
							if (NoULCode.eqs("chunked", value) != -1) {
								bodyLength = -1;
								chunkedBuffer = new ByteArrayIO();
							}
						}
						// gzip.
						gzip = result.isResponseGzip();
					} else {
						continue;
					}
				}
				// バッファに受信中のデータが、一定量を超える場合.
				// または recvFileFlag が onの場合.
				// ただし、bodyファイルが生成されていない場合のみ対象.
				if (bodyFile == null && (bodyFileFlg || (bodyLength != -1 && MAX_BINARY_BODY_LENGTH <= buffer.size())
						|| (bodyLength == -1 && MAX_BINARY_BODY_LENGTH <= chunkedBuffer.size()))) {
					// bodyデータが空の場合は処理しない.
					if (bodyLength == 0) {
						// 0byteデータ.
						result.setResponseBody(new byte[0]);
						return result;
					}

					// バッファに受信中のデータを出力.
					int bufLen;
					bodyFile = new HttpBodyFile();
					byte[] buf = binary;
					ByteArrayIO io = (bodyLength != -1) ? buffer : chunkedBuffer;
					while ((bufLen = io.read(buf)) != 0) {
						bodyFile.write(buf, bufLen);
					}
					if (chunkedBuffer != null) {
						chunkedBuffer.close();
						chunkedBuffer = null;
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
					if (bodyFile != null) {
						result.setReponseBodyFile(bodyFile);
						bodyFile = null;
						return result;
					} else if (buffer.size() >= bodyLength) {
						b = new byte[bodyLength];
						buffer.read(b);
						if (gzip) {
							b = ungzip(b, binary, buffer);
						}
						resultBinarySet(result, akey, acode, b);
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
								chunkedLength = getChunkedLength(p, buffer, binary);
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
								if (bodyFile != null) {
									result.setReponseBodyFile(bodyFile);
									bodyFile = null;
								} else {
									b = chunkedBuffer.toByteArray();
									if (gzip) {
										b = ungzip(b, binary, buffer);
									}
									resultBinarySet(result, akey, acode, b);
									b = null;
								}
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
							if (bodyFile != null) {
								bodyFile.write(b, chunkedLength);
							} else {
								chunkedBuffer.write(b, 0, chunkedLength);
							}
							chunkedLength = -1;
							b = null;
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
			if (bodyFile != null) {
				bodyFile.close();
			}
		}
	}
	
	// resultBinaryをセット.
	private static final void resultBinarySet(HttpResult result, String akey, String acode, byte[] b) {
		// accessKeyがレスポンスヘッダに存在する場合.
		String resultAkey = result.getHeader(RhiginAccessKeyConstants.RHIGIN_ACCESSKEY_HTTP_HEADER);
		if(resultAkey != null && akey != null && akey.equals(resultAkey)) {
			b = RhiginAccessKeyByFCipher.decode(akey, acode, b);
		}
		// ObjectPackの受信の場合.
		if(MimeType.RHIGIN_OBJECT_PACK_MIME_TYPE.equals(result.getHeader("Content-Type"))) {
			try {
				result.setResponseBodyJson(ObjectPack.unpackB(b));
			} catch(Exception e) {
				throw new HttpClientException(e);
			}
		}
		result.setResponseBody(b);
	}

	// gzip解凍.
	private static final byte[] ungzip(byte[] b, byte[] o, ByteArrayIO io) throws IOException {
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
	private static final int getChunkedLength(int p, ByteArrayIO io, byte[] o) throws IOException {
		io.read(o, 0, p);
		io.skip(2);
		// String s = new String(o, 0, p, "UTF8");
		// return Integer.parseInt(s, 16);
		int ret = 0;
		int shift = 0;
		for (int i = p - 1; i >= 0; i--, shift += 4) {
			switch ((char) o[i]) {
			case '0':
				break;
			case '1':
				ret += (1 << shift);
				break;
			case '2':
				ret += (2 << shift);
				break;
			case '3':
				ret += (3 << shift);
				break;
			case '4':
				ret += (4 << shift);
				break;
			case '5':
				ret += (5 << shift);
				break;
			case '6':
				ret += (6 << shift);
				break;
			case '7':
				ret += (7 << shift);
				break;
			case '8':
				ret += (8 << shift);
				break;
			case '9':
				ret += (9 << shift);
				break;
			case 'a':
				ret += (10 << shift);
				break;
			case 'b':
				ret += (11 << shift);
				break;
			case 'c':
				ret += (12 << shift);
				break;
			case 'd':
				ret += (13 << shift);
				break;
			case 'e':
				ret += (14 << shift);
				break;
			case 'f':
				ret += (15 << shift);
				break;
			case 'A':
				ret += (10 << shift);
				break;
			case 'B':
				ret += (11 << shift);
				break;
			case 'C':
				ret += (12 << shift);
				break;
			case 'D':
				ret += (13 << shift);
				break;
			case 'E':
				ret += (14 << shift);
				break;
			case 'F':
				ret += (15 << shift);
				break;
			}
		}
		return ret;
	}
	
	// 大文字小文字区別なしの判別.
	protected static final class NoULCode {
		
		/** アルファベットの半角全角変換値. **/
		private static final char[] _mM = new char[65536];
		static {
			int len = _mM.length;
			for (int i = 0; i < len; i++) {
				_mM[i] = (char) i;
			}
			int code = (int) 'a';
			int alpha = (int) ('z' - 'a') + 1;
			for (int i = 0; i < alpha; i++) {
				_mM[i + code] = (char) (code + i);
			}
			int target = (int) 'A';
			for (int i = 0; i < alpha; i++) {
				_mM[i + target] = (char) (code + i);
			}
		}
		
		// 大文字小文字区別なしの判別.
		protected static final int eqs(String src, String... dests) {
			int len;
			if (src == null || dests == null || (len = dests.length) == 0) {
				return -1;
			}
			int j;
			String n;
			boolean eq;
			int lenJ = src.length();
			for(int i = 0; i < len; i ++) {
				if (lenJ == (n = dests[i]).length()) {
					eq = true;
					for (j = 0; j < lenJ; j++) {
						if (_mM[src.charAt(j)] != _mM[n.charAt(j)]) {
							eq = false;
							break;
						}
					}
					if(eq) {
						return i;
					}
				}
			}
			return -1;
		}
		
		// 文字コードのチェック.
		protected static final boolean oneEq(char s, char d) {
			return _mM[s] == _mM[d];
		}
		
		// indexOf.
		protected static final int indexOf(final String buf, final String chk, final int off) {
			final int len = chk.length();
			// 単数文字検索.
			if (len == 1) {
				final int vLen = buf.length();
				if(vLen > off) {
					int i = off;
					final char first = chk.charAt(0);
					if (!oneEq(first, buf.charAt(i))) {
						while (++i < vLen && !oneEq(first, buf.charAt(i)))
							;
						if (vLen != i) {
							return i;
						}
					} else {
						return i;
					}
				}
			}
			// 複数文字検索.
			else {
				int j, k, next;
				final char first = chk.charAt(0);
				final int vLen = buf.length() - (len - 1);
				for (int i = off; i < vLen; i++) {
					if (!oneEq(first, buf.charAt(i))) {
						while (++i < vLen && !oneEq(first, buf.charAt(i)))
							;
					}
					if (i < vLen) {
						for (next = i + len, j = i + 1, k = 1; j < next &&
								oneEq(buf.charAt(j), chk.charAt(k)); j++, k++)
							;
						if (j == next) {
							return i;
						}
					}
				}
			}
			return -1;
		}
	}
}
